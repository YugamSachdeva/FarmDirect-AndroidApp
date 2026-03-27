package com.example.farmermarketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NewOrdersFragment extends Fragment {

    RecyclerView recyclerView;
    TextView tvEmpty;
    List<FarmerOrderModel> orderList;
    FarmerOrderAdapter adapter;
    FirebaseFirestore db;
    String farmerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_new_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db       = FirebaseFirestore.getInstance();
        farmerId = FirebaseAuth.getInstance()
                .getCurrentUser().getUid();

        recyclerView = view.findViewById(R.id.recyclerNewOrders);
        tvEmpty      = view.findViewById(R.id.tvEmpty);

        orderList = new ArrayList<>();
        adapter   = new FarmerOrderAdapter(
                getContext(), orderList, "new", this::onAction);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        db.collection("orders")
                .whereEqualTo("farmerId", farmerId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snapshot -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        orderList.add(parseOrder(doc));
                    }
                    // Latest pehle sort karo
                    orderList.sort((a, b) ->
                            Long.compare(b.getTimestamp(),
                                    a.getTimestamp()));
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(
                            orderList.isEmpty()
                                    ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(
                            orderList.isEmpty()
                                    ? View.GONE : View.VISIBLE);
                });
    }

    private void onAction(FarmerOrderModel order, String action) {
        if ("accepted".equals(action)) {
            db.collection("orders").document(order.getDocId())
                    .update("status", "accepted")
                    .addOnSuccessListener(u -> {
                        updateProductQuantity(order);
                        orderList.remove(order);
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(
                                orderList.isEmpty()
                                        ? View.VISIBLE : View.GONE);
                        Toast.makeText(getContext(),
                                "Order Accepted ✅",
                                Toast.LENGTH_SHORT).show();
                    });
        } else if ("rejected".equals(action)) {
            db.collection("orders").document(order.getDocId())
                    .update("status", "rejected")
                    .addOnSuccessListener(u -> {
                        orderList.remove(order);
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(
                                orderList.isEmpty()
                                        ? View.VISIBLE : View.GONE);
                        Toast.makeText(getContext(),
                                "Order Rejected ❌",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateProductQuantity(FarmerOrderModel order) {
        if (order.getProductId() == null
                || order.getProductId().isEmpty()) return;

        db.collection("products")
                .document(order.getProductId()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        long currentQty = 0;
                        Object qObj = doc.get("quantity");
                        if (qObj instanceof Long)
                            currentQty = (Long) qObj;

                        long newQty = currentQty - order.getQuantity();

                        if (newQty <= 0) {
                            db.collection("products")
                                    .document(order.getProductId())
                                    .delete();
                        } else {
                            db.collection("products")
                                    .document(order.getProductId())
                                    .update("quantity", newQty);
                        }
                    }
                });
    }

    private FarmerOrderModel parseOrder(QueryDocumentSnapshot doc) {
        FarmerOrderModel order = new FarmerOrderModel();
        order.setDocId(doc.getId());
        order.setName(doc.getString("name") != null
                ? doc.getString("name") : "");
        order.setStatus(doc.getString("status") != null
                ? doc.getString("status") : "pending");
        order.setCustomerId(doc.getString("customerId") != null
                ? doc.getString("customerId") : "");
        order.setProductId(doc.getString("productId") != null
                ? doc.getString("productId") : "");

        long ts = doc.getLong("timestamp") != null
                ? doc.getLong("timestamp") : 0;
        order.setTimestamp(ts);

        double price = 0;
        Object pObj = doc.get("price");
        if (pObj instanceof Double) price = (Double) pObj;
        else if (pObj instanceof Long)
            price = ((Long) pObj).doubleValue();
        order.setPrice(price);

        int qty = 0;
        Object qObj = doc.get("quantity");
        if (qObj instanceof Long) qty = ((Long) qObj).intValue();
        order.setQuantity(qty);

        return order;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }
}
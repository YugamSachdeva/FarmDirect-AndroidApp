package com.example.farmermarketapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomerOrdersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CustomerOrderAdapter adapter;
    List<CustomerOrderModel> orderList;

    TextView tvEmpty, tvOrderCount;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_orders);

        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerCustomerOrders);
        tvEmpty      = findViewById(R.id.tvEmpty);
        tvOrderCount = findViewById(R.id.tvOrderCount);

        orderList = new ArrayList<>();
        adapter   = new CustomerOrderAdapter(this, orderList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        listenOrders();
    }

    private void listenOrders() {

        String userId = mAuth.getCurrentUser().getUid();
        tvOrderCount.setText("Loading...");

        listenerRegistration = db.collection("orders")
                .whereEqualTo("customerId", userId)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null) {
                        tvOrderCount.setText("Failed to load");
                        return;
                    }

                    orderList.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        CustomerOrderModel order =
                                new CustomerOrderModel();
                        order.setDocId(doc.getId());
                        order.setName(doc.getString("name") != null
                                ? doc.getString("name") : "");
                        order.setStatus(doc.getString("status") != null
                                ? doc.getString("status") : "pending");
                        order.setFarmerId(
                                doc.getString("farmerId") != null
                                        ? doc.getString("farmerId") : "");

                        // Timestamp
                        long timestamp = doc.getLong("timestamp") != null
                                ? doc.getLong("timestamp") : 0;
                        order.setTimestamp(timestamp);

                        double price = 0;
                        Object pObj = doc.get("price");
                        if (pObj instanceof Double)
                            price = (Double) pObj;
                        else if (pObj instanceof Long)
                            price = ((Long) pObj).doubleValue();
                        order.setPrice(price);

                        int quantity = 0;
                        Object qObj = doc.get("quantity");
                        if (qObj instanceof Long)
                            quantity = ((Long) qObj).intValue();
                        else if (qObj instanceof String) {
                            try {
                                quantity = Integer.parseInt(
                                        (String) qObj);
                            } catch (NumberFormatException e) {
                                quantity = 0;
                            }
                        }
                        order.setQuantity(quantity);

                        orderList.add(order);
                    }

                    adapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tvOrderCount.setText("0 orders");
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        tvOrderCount.setText(
                                orderList.size() + " order(s)");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
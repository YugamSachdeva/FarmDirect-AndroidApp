package com.example.farmermarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CartAdapter adapter;
    List<CartItem> cartList;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String userId;

    Button placeOrderBtn, btnGoShopping;
    TextView tvTotal;
    LinearLayout layoutEmpty, layoutBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db     = FirebaseFirestore.getInstance();
        mAuth  = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        recyclerView  = findViewById(R.id.cartRecyclerView);
        placeOrderBtn = findViewById(R.id.placeOrderBtn);
        tvTotal       = findViewById(R.id.tvTotal);
        layoutEmpty   = findViewById(R.id.layoutEmpty);
        layoutBottom  = findViewById(R.id.layoutBottom);
        btnGoShopping = findViewById(R.id.btnGoShopping);

        cartList = new ArrayList<>();

        adapter = new CartAdapter(
                this,
                cartList,
                docId -> {
                    db.collection("cart").document(docId)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this,
                                        "Item removed 🗑️",
                                        Toast.LENGTH_SHORT).show();
                                loadCart();
                            });
                },
                () -> updateTotal()
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadCart();

        // Go Shopping button
        btnGoShopping.setOnClickListener(v -> {
            finish();
        });

        // Place Order button
        placeOrderBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("🛒 Place Order")
                    .setMessage("Are you sure you want to place this order?")
                    .setPositiveButton("Yes, Place Order",
                            (dialog, which) -> placeOrder())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadCart() {
        db.collection("cart")
                .whereEqualTo("customerId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    cartList.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        CartItem item = new CartItem();
                        item.setDocId(doc.getId());
                        item.setName(doc.getString("name") != null
                                ? doc.getString("name") : "");
                        item.setCategory(
                                doc.getString("category") != null
                                        ? doc.getString("category") : "");
                        item.setFarmerId(
                                doc.getString("farmerId") != null
                                        ? doc.getString("farmerId") : "");
                        item.setProductId(
                                doc.getString("productId") != null
                                        ? doc.getString("productId") : "");

                        double price = 0;
                        Object pObj = doc.get("price");
                        if (pObj instanceof Double)
                            price = (Double) pObj;
                        else if (pObj instanceof Long)
                            price = ((Long) pObj).doubleValue();
                        item.setPrice(price);

                        int quantity = 1;
                        Object qObj = doc.get("quantity");
                        if (qObj instanceof Long)
                            quantity = ((Long) qObj).intValue();
                        item.setQuantity(quantity);

                        cartList.add(item);
                    }

                    adapter.notifyDataSetChanged();
                    updateUI();
                });
    }

    private void updateUI() {
        if (cartList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            layoutBottom.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            layoutBottom.setVisibility(View.VISIBLE);
            updateTotal();
        }
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartList) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotal.setText("Total: ₹" + (int) total);
    }

    private void placeOrder() {

        if (cartList.isEmpty()) {
            Toast.makeText(this,
                    "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        placeOrderBtn.setEnabled(false);

        final int[] successCount = {0};
        int total = cartList.size();

        for (CartItem item : cartList) {
            Map<String, Object> order = new HashMap<>();
            order.put("name", item.getName());
            order.put("price", item.getPrice());
            order.put("quantity", item.getQuantity());
            order.put("customerId", userId);
            order.put("farmerId", item.getFarmerId());
            order.put("productId", item.getProductId());
            order.put("status", "pending");
            order.put("timestamp", System.currentTimeMillis());

            db.collection("orders").add(order)
                    .addOnSuccessListener(unused -> {
                        successCount[0]++;
                        if (successCount[0] == total) {
                            clearCart();
                        }
                    });
        }
    }

    private void clearCart() {
        db.collection("cart")
                .whereEqualTo("customerId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        doc.getReference().delete();
                    }
                    Toast.makeText(this,
                            "Order placed successfully! 🎉",
                            Toast.LENGTH_LONG).show();
                    loadCart();
                });
    }
}
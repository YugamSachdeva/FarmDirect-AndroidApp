package com.example.farmermarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FarmerDashboardActivity extends AppCompatActivity {

    LinearLayout btnAddProduct, btnMyProducts, btnOrders;
    TextView tvTotalProducts, tvPendingOrders,
            tvEarnings, tvFarmerName, tvFarmerInitial, tvOrderBadge;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String farmerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        farmerId = user.getUid();

        tvFarmerName    = findViewById(R.id.tvFarmerName);
        tvFarmerInitial = findViewById(R.id.tvFarmerInitial);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvEarnings      = findViewById(R.id.tvEarnings);
        tvOrderBadge    = findViewById(R.id.tvOrderBadge);
        btnAddProduct   = findViewById(R.id.btnAddProduct);
        btnMyProducts   = findViewById(R.id.btnMyProducts);
        btnOrders       = findViewById(R.id.btnOrders);

        loadFarmerName();
        loadStats();

        tvFarmerInitial.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnAddProduct.setOnClickListener(v ->
                startActivity(new Intent(this, AddProductActivity.class)));

        btnMyProducts.setOnClickListener(v ->
                startActivity(new Intent(this, MyProductsActivity.class)));

        btnOrders.setOnClickListener(v ->
                startActivity(new Intent(this, FarmerOrdersActivity.class)));
    }

    private void loadFarmerName() {
        db.collection("users").document(farmerId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvFarmerName.setText(
                                    "Welcome, " + name + "! 👋");
                            tvFarmerInitial.setText(
                                    String.valueOf(name.charAt(0))
                                            .toUpperCase());
                        }
                    }
                });
    }

    private void loadStats() {

        // Products count
        db.collection("products")
                .whereEqualTo("farmerId", farmerId)
                .get()
                .addOnSuccessListener(snapshot ->
                        tvTotalProducts.setText(
                                String.valueOf(snapshot.size())));

        // Orders stats
        db.collection("orders")
                .whereEqualTo("farmerId", farmerId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    int pendingCount     = 0;
                    double totalEarnings = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String status = doc.getString("status");

                        if ("pending".equalsIgnoreCase(status)) {
                            pendingCount++;
                        }

                        if ("accepted".equalsIgnoreCase(status)
                                || "delivered".equalsIgnoreCase(status)) {
                            Double price = doc.getDouble("price");
                            Object qObj  = doc.get("quantity");
                            int qty = 0;
                            if (qObj instanceof Long)
                                qty = ((Long) qObj).intValue();
                            if (price != null) {
                                totalEarnings += price * qty;
                            }
                        }
                    }

                    tvPendingOrders.setText(
                            String.valueOf(pendingCount));
                    tvEarnings.setText("₹" + (int) totalEarnings);

                    // Badge show karo
                    if (pendingCount > 0) {
                        tvOrderBadge.setVisibility(View.VISIBLE);
                        tvOrderBadge.setText(
                                String.valueOf(pendingCount));
                    } else {
                        tvOrderBadge.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFarmerName();
        loadStats();
    }
}
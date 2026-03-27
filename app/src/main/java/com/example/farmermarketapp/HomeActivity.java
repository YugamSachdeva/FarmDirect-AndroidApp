package com.example.farmermarketapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProductAdapter adapter;
    List<Product> productList;
    List<Product> filteredList;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    LinearLayout btnCart, btnMyOrders, btnProfile;
    EditText etSearch;
    TextView tvEmpty, tvCustomerName, tvNavInitial, tvCartBadge;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView   = findViewById(R.id.productRecyclerView);
        btnCart        = findViewById(R.id.btnCart);
        btnMyOrders    = findViewById(R.id.btnMyOrders);
        btnProfile     = findViewById(R.id.btnProfile);
        etSearch       = findViewById(R.id.etSearch);
        tvEmpty        = findViewById(R.id.tvEmpty);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvNavInitial   = findViewById(R.id.tvNavInitial);
        tvCartBadge    = findViewById(R.id.tvCartBadge);
        progressBar    = findViewById(R.id.progressBar);

        productList  = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new ProductAdapter(this, filteredList,
                product -> addToCart(product));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Internet check
        if (!isInternetAvailable()) {
            showNoInternet();
            return;
        }

        loadCustomerName();
        loadProducts();
        loadCartCount();

        btnCart.setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));

        btnMyOrders.setOnClickListener(v ->
                startActivity(new Intent(this,
                        CustomerOrdersActivity.class)));

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                filterProducts(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Internet check
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null
                    && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void showNoInternet() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText("📵 No Internet Connection\n\nPlease check your network and try again.");
    }

    private void loadCustomerName() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvCustomerName.setText(
                                    "Welcome, " + name + "! 👋");
                            tvNavInitial.setText(
                                    String.valueOf(name.charAt(0))
                                            .toUpperCase());
                        }
                    }
                });
    }

    private void loadCartCount() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("cart")
                .whereEqualTo("customerId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    int count = snapshot.size();
                    if (count > 0) {
                        tvCartBadge.setVisibility(View.VISIBLE);
                        tvCartBadge.setText(String.valueOf(count));
                    } else {
                        tvCartBadge.setVisibility(View.GONE);
                    }
                });
    }

    private void loadProducts() {

        // Spinner show
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("products")
                .get()
                .addOnSuccessListener(snapshot -> {

                    progressBar.setVisibility(View.GONE);
                    productList.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String id       = doc.getId();
                        String name     = doc.getString("name") != null
                                ? doc.getString("name") : "";
                        String category = doc.getString("category") != null
                                ? doc.getString("category") : "";
                        String desc     = doc.getString("description") != null
                                ? doc.getString("description") : "";
                        String farmerId = doc.getString("farmerId") != null
                                ? doc.getString("farmerId") : "";

                        int quantity = 0;
                        Object qObj = doc.get("quantity");
                        if (qObj instanceof Long)
                            quantity = ((Long) qObj).intValue();
                        else if (qObj instanceof String) {
                            try { quantity = Integer.parseInt(
                                    (String) qObj); }
                            catch (NumberFormatException e) { quantity = 0; }
                        }

                        double price = 0;
                        Object pObj = doc.get("price");
                        if (pObj instanceof Double)
                            price = (Double) pObj;
                        else if (pObj instanceof Long)
                            price = ((Long) pObj).doubleValue();
                        else if (pObj instanceof String) {
                            try { price = Double.parseDouble(
                                    (String) pObj); }
                            catch (NumberFormatException e) { price = 0; }
                        }

                        if (quantity > 0) {
                            productList.add(new Product(
                                    id, name, category,
                                    desc, price, quantity, farmerId));
                        }
                    }

                    filteredList.clear();
                    filteredList.addAll(productList);
                    adapter.notifyDataSetChanged();

                    if (filteredList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No products available yet.");
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Failed to load products.");
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (Product p : productList) {
                if (p.getName().toLowerCase()
                        .contains(query.toLowerCase())
                        || p.getCategory().toLowerCase()
                        .contains(query.toLowerCase())) {
                    filteredList.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No products found 🔍");
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void addToCart(Product product) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("cart")
                .whereEqualTo("customerId", userId)
                .whereEqualTo("productId", product.getId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Toast.makeText(this,
                                "⚠️ " + product.getName()
                                        + " already in cart!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, Object> cartItem = new HashMap<>();
                        cartItem.put("productId", product.getId());
                        cartItem.put("name", product.getName());
                        cartItem.put("price", product.getPrice());
                        cartItem.put("category", product.getCategory());
                        cartItem.put("farmerId", product.getFarmerId());
                        cartItem.put("customerId", userId);
                        cartItem.put("quantity", 1);

                        db.collection("cart").add(cartItem)
                                .addOnSuccessListener(ref -> {
                                    Toast.makeText(this,
                                            "✅ " + product.getName()
                                                    + " added to cart!",
                                            Toast.LENGTH_SHORT).show();
                                    loadCartCount();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,
                                                "Failed: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInternetAvailable()) {
            showNoInternet();
            return;
        }
        loadProducts();
        loadCustomerName();
        loadCartCount();
    }
}
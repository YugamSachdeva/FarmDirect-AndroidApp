package com.example.farmermarketapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyProductsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView tvEmpty, tvProductCount;
    EditText etSearch;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    List<Product> productList;
    List<Product> filteredList;
    MyProductsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);

        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView   = findViewById(R.id.recyclerMyProducts);
        tvEmpty        = findViewById(R.id.tvEmpty);
        tvProductCount = findViewById(R.id.tvProductCount);
        etSearch       = findViewById(R.id.etSearch);

        productList  = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter      = new MyProductsAdapter(this, filteredList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadMyProducts();

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

    private void loadMyProducts() {
        String farmerId = mAuth.getCurrentUser().getUid();
        tvProductCount.setText("Loading...");

        db.collection("products")
                .whereEqualTo("farmerId", farmerId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    productList.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String id       = doc.getId();
                        String name     = doc.getString("name") != null
                                ? doc.getString("name") : "";
                        String category = doc.getString("category") != null
                                ? doc.getString("category") : "";
                        String desc     = doc.getString("description") != null
                                ? doc.getString("description") : "";
                        double price    = doc.getDouble("price") != null
                                ? doc.getDouble("price") : 0;
                        int quantity    = doc.getLong("quantity") != null
                                ? doc.getLong("quantity").intValue() : 0;
                        String fid      = doc.getString("farmerId") != null
                                ? doc.getString("farmerId") : "";

                        productList.add(new Product(
                                id, name, category,
                                desc, price, quantity, fid));
                    }

                    filteredList.clear();
                    filteredList.addAll(productList);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e ->
                        tvProductCount.setText("Failed to load"));
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
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvProductCount.setText("0 products");
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            tvProductCount.setText(filteredList.size() + " product(s)");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyProducts();
    }
}
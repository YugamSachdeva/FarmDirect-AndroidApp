package com.example.farmermarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    EditText etProductName, etCategory, etPrice, etQuantity, etDescription;
    Button btnAddProduct;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etProductName  = findViewById(R.id.etProductName);
        etCategory     = findViewById(R.id.etCategory);
        etPrice        = findViewById(R.id.etPrice);
        etQuantity     = findViewById(R.id.etQuantity);
        etDescription  = findViewById(R.id.etDescription);
        btnAddProduct  = findViewById(R.id.btnAddProduct);
        progressBar    = findViewById(R.id.progressBar);

        btnAddProduct.setOnClickListener(v -> {

            String name        = etProductName.getText().toString().trim();
            String category    = etCategory.getText().toString().trim();
            String priceStr    = etPrice.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            // Validation
            if (name.isEmpty()) {
                etProductName.setError("Product name required");
                etProductName.requestFocus();
                return;
            }
            if (category.isEmpty()) {
                etCategory.setError("Category required");
                etCategory.requestFocus();
                return;
            }
            if (priceStr.isEmpty()) {
                etPrice.setError("Price required");
                etPrice.requestFocus();
                return;
            }
            if (quantityStr.isEmpty()) {
                etQuantity.setError("Quantity required");
                etQuantity.requestFocus();
                return;
            }

            double price    = Double.parseDouble(priceStr);
            int quantity    = Integer.parseInt(quantityStr);

            if (price <= 0) {
                etPrice.setError("Enter valid price");
                etPrice.requestFocus();
                return;
            }
            if (quantity <= 0) {
                etQuantity.setError("Enter valid quantity");
                etQuantity.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnAddProduct.setEnabled(false);

            String farmerId = mAuth.getCurrentUser().getUid();

            // Firestore mein save karo
            Map<String, Object> product = new HashMap<>();
            product.put("name", name);
            product.put("category", category);
            product.put("price", price);
            product.put("quantity", quantity);
            product.put("description", description);
            product.put("farmerId", farmerId);
            product.put("timestamp", System.currentTimeMillis());

            db.collection("products")
                    .add(product)
                    .addOnSuccessListener(docRef -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Product added successfully! 🎉",
                                Toast.LENGTH_SHORT).show();
                        finish(); // Dashboard pe wapas jao
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        btnAddProduct.setEnabled(true);
                        Toast.makeText(this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }
}
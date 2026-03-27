package com.example.farmermarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RoleActivity extends AppCompatActivity {

    Button farmerBtn, customerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        farmerBtn = findViewById(R.id.farmerBtn);
        customerBtn = findViewById(R.id.customerBtn);

        // 👨‍🌾 Farmer → Farmer Dashboard
        farmerBtn.setOnClickListener(v -> {
            startActivity(new Intent(RoleActivity.this, FarmerDashboardActivity.class));
            finish();
        });

        // 🛒 Customer → Home Activity
        customerBtn.setOnClickListener(v -> {
            startActivity(new Intent(RoleActivity.this, HomeActivity.class));
            finish();
        });
    }
}
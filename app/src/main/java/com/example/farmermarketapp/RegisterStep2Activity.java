package com.example.farmermarketapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterStep2Activity extends AppCompatActivity {

    LinearLayout cardFarmer, cardCustomer;
    RadioButton rbFarmer, rbCustomer;
    Button btnNextRole;

    String selectedRole = "";
    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_step2);

        cardFarmer   = findViewById(R.id.cardFarmer);
        cardCustomer = findViewById(R.id.cardCustomer);
        rbFarmer     = findViewById(R.id.rbFarmer);
        rbCustomer   = findViewById(R.id.rbCustomer);
        btnNextRole  = findViewById(R.id.btnNextRole);

        email    = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        // Default - koi select nahi
        rbFarmer.setChecked(false);
        rbCustomer.setChecked(false);

        // Farmer card click
        cardFarmer.setOnClickListener(v -> {
            selectedRole = "farmer";
            rbFarmer.setChecked(true);
            rbCustomer.setChecked(false);
            highlightCard(cardFarmer, cardCustomer);
        });

        // Farmer RadioButton click
        rbFarmer.setOnClickListener(v -> {
            selectedRole = "farmer";
            rbFarmer.setChecked(true);
            rbCustomer.setChecked(false);
            highlightCard(cardFarmer, cardCustomer);
        });

        // Customer card click
        cardCustomer.setOnClickListener(v -> {
            selectedRole = "customer";
            rbCustomer.setChecked(true);
            rbFarmer.setChecked(false);
            highlightCard(cardCustomer, cardFarmer);
        });

        // Customer RadioButton click
        rbCustomer.setOnClickListener(v -> {
            selectedRole = "customer";
            rbCustomer.setChecked(true);
            rbFarmer.setChecked(false);
            highlightCard(cardCustomer, cardFarmer);
        });

        btnNextRole.setOnClickListener(v -> {

            if (selectedRole.isEmpty()) {
                Toast.makeText(this,
                        "Please select a role",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 3 pe jao
            Intent intent = new Intent(this, RegisterStep3Activity.class);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            intent.putExtra("role", selectedRole);
            startActivity(intent);
        });
    }

    // Selected card green border, other card normal
    private void highlightCard(LinearLayout selected, LinearLayout unselected) {
        selected.setBackgroundResource(R.drawable.role_card_selected_bg);
        unselected.setBackgroundResource(R.drawable.role_card_bg);
    }
}
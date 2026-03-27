package com.example.farmermarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    EditText etName, etPhone, etState, etCity, etPincode;
    Button btnSave, btnLogout;
    ProgressBar progressBar;
    TextView tvProfileName, tvProfileRole, tvInitials;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth  = FirebaseAuth.getInstance();
        db     = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        etName        = findViewById(R.id.etProfileName);
        etPhone       = findViewById(R.id.etProfilePhone);
        etState       = findViewById(R.id.etProfileState);
        etCity        = findViewById(R.id.etProfileCity);
        etPincode     = findViewById(R.id.etProfilePincode);
        btnSave       = findViewById(R.id.btnSaveProfile);
        btnLogout     = findViewById(R.id.btnLogout);
        progressBar   = findViewById(R.id.progressBar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvInitials    = findViewById(R.id.tvInitials);

        loadProfile();

        btnSave.setOnClickListener(v -> saveProfile());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadProfile() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name    = doc.getString("name");
                        String phone   = doc.getString("phone");
                        String state   = doc.getString("state");
                        String city    = doc.getString("city");
                        String pincode = doc.getString("pincode");
                        String role    = doc.getString("role");

                        etName.setText(name != null ? name : "");
                        etPhone.setText(phone != null ? phone : "");
                        etState.setText(state != null ? state : "");
                        etCity.setText(city != null ? city : "");
                        etPincode.setText(pincode != null ? pincode : "");

                        tvProfileName.setText(
                                name != null ? name : "Your Name");

                        tvProfileRole.setText(
                                role != null
                                        ? role.substring(0, 1).toUpperCase()
                                        + role.substring(1) : "");

                        // Initials set karo
                        setInitials(name);
                    }
                });
    }

    // Naam se initials nikalo
    private void setInitials(String name) {
        if (name == null || name.isEmpty()) {
            tvInitials.setText("?");
            return;
        }

        String[] parts = name.trim().split(" ");
        String initials = "";

        if (parts.length >= 2) {
            // Pehle aur doosre word ka pehla letter
            initials = String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        } else {
            // Sirf ek word — pehle 2 letters
            initials = name.length() >= 2
                    ? name.substring(0, 2).toUpperCase()
                    : name.substring(0, 1).toUpperCase();
        }

        tvInitials.setText(initials);
    }

    private void saveProfile() {

        String name    = etName.getText().toString().trim();
        String phone   = etPhone.getText().toString().trim();
        String state   = etState.getText().toString().trim();
        String city    = etCity.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name required");
            etName.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Phone required");
            etPhone.requestFocus();
            return;
        }
        if (state.isEmpty()) {
            etState.setError("State required");
            etState.requestFocus();
            return;
        }
        if (city.isEmpty()) {
            etCity.setError("City required");
            etCity.requestFocus();
            return;
        }
        if (pincode.isEmpty()) {
            etPincode.setError("Pincode required");
            etPincode.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("state", state);
        updates.put("city", city);
        updates.put("pincode", pincode);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    tvProfileName.setText(name);
                    setInitials(name);
                    Toast.makeText(this,
                            "Profile updated! ✅",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
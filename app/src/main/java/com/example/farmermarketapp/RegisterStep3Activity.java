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

public class RegisterStep3Activity extends AppCompatActivity {

    EditText etName, etPhone, etState, etCity, etPincode;
    Button btnRegisterFinal;
    ProgressBar progressBar;

    String email, password, role;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_step3);

        etName          = findViewById(R.id.etName);
        etPhone         = findViewById(R.id.etPhone);
        etState         = findViewById(R.id.etState);
        etCity          = findViewById(R.id.etCity);
        etPincode       = findViewById(R.id.etPincode);
        btnRegisterFinal = findViewById(R.id.btnRegisterFinal);
        progressBar     = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // Step 1 aur Step 2 se data
        email    = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        role     = getIntent().getStringExtra("role");

        btnRegisterFinal.setOnClickListener(v -> {

            String name    = etName.getText().toString().trim();
            String phone   = etPhone.getText().toString().trim();
            String state   = etState.getText().toString().trim();
            String city    = etCity.getText().toString().trim();
            String pincode = etPincode.getText().toString().trim();

            // Validation
            if (name.isEmpty()) {
                etName.setError("Name required");
                etName.requestFocus();
                return;
            }
            if (phone.isEmpty() || phone.length() < 10) {
                etPhone.setError("Valid phone required");
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
            if (pincode.isEmpty() || pincode.length() < 6) {
                etPincode.setError("Valid pincode required");
                etPincode.requestFocus();
                return;
            }

            // Loading show
            progressBar.setVisibility(View.VISIBLE);
            btnRegisterFinal.setEnabled(false);

            // Firebase Auth account banao
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String userId = mAuth.getCurrentUser().getUid();

                            // Firestore mein save karo
                            Map<String, Object> user = new HashMap<>();
                            user.put("email", email);
                            user.put("role", role);
                            user.put("name", name);
                            user.put("phone", phone);
                            user.put("state", state);
                            user.put("city", city);
                            user.put("pincode", pincode);
                            user.put("profileCompleted", true);

                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {

                                        progressBar.setVisibility(View.GONE);

                                        // Sign out karo — Login pe bhejo
                                        mAuth.signOut();

                                        Toast.makeText(this,
                                                "Registration Successful! Please Login.",
                                                Toast.LENGTH_LONG).show();

                                        // Login page pe jao, back stack clear
                                        Intent intent = new Intent(this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        btnRegisterFinal.setEnabled(true);
                                        Toast.makeText(this,
                                                "Save failed: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnRegisterFinal.setEnabled(true);
                            Toast.makeText(this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
package com.example.farmermarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView registerText;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email        = findViewById(R.id.email);
        password     = findViewById(R.id.password);
        loginBtn     = findViewById(R.id.loginBtn);
        registerText = findViewById(R.id.registerText);
        progressBar  = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        loginBtn.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim();
            String userPass  = password.getText().toString().trim();

            if (TextUtils.isEmpty(userEmail)) {
                email.setError("Email required");
                email.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(userPass)) {
                password.setError("Password required");
                password.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(false);

            mAuth.signInWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            if (mAuth.getCurrentUser() == null) return;
                            String userId = mAuth.getCurrentUser().getUid();
                            db.collection("users").document(userId).get()
                                    .addOnSuccessListener(doc -> {

                                        progressBar.setVisibility(View.GONE);

                                        if (doc.exists()) {

                                            String role = doc.getString("role");

                                            Intent intent;
                                            if ("farmer".equals(role)) {
                                                intent = new Intent(this,
                                                        FarmerDashboardActivity.class);
                                            } else {
                                                intent = new Intent(this,
                                                        HomeActivity.class);
                                            }
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);

                                        } else {
                                            loginBtn.setEnabled(true);
                                            Toast.makeText(this,
                                                    "User data not found. Please register.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        loginBtn.setEnabled(true);
                                        Toast.makeText(this,
                                                "Error: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });

                        } else {
                            progressBar.setVisibility(View.GONE);
                            loginBtn.setEnabled(true);
                            Toast.makeText(this,
                                    "Login Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        registerText.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterStep1Activity.class)));
    }
}
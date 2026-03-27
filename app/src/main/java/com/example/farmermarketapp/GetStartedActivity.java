package com.example.farmermarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class GetStartedActivity extends AppCompatActivity {

    Button btnGetStarted;
    TextView btnLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkRoleAndGo(currentUser.getUid());
            return;
        }

        setContentView(R.layout.activity_get_started);

        btnGetStarted = findViewById(R.id.getStartedBtn);
        btnLogin      = findViewById(R.id.btnLogin);

        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterStep1Activity.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void checkRoleAndGo(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String role = doc.getString("role");
                    Intent intent;
                    if ("farmer".equals(role)) {
                        intent = new Intent(this, FarmerDashboardActivity.class);
                    } else {
                        intent = new Intent(this, HomeActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }
}
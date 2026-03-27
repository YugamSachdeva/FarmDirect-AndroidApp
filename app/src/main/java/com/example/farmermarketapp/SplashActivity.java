package com.example.farmermarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        LinearLayout splashContent = findViewById(R.id.splashContent);
        LinearLayout splashBottom  = findViewById(R.id.splashBottom);

        // Fade in — Center content
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);
        splashContent.startAnimation(fadeIn);

        // Slide up — Bottom content
        TranslateAnimation slideUp = new TranslateAnimation(
                0, 0,          // X — koi change nahi
                300, 0         // Y — neeche se upar
        );
        slideUp.setDuration(800);
        slideUp.setFillAfter(true);

        AlphaAnimation fadeInBottom = new AlphaAnimation(0f, 1f);
        fadeInBottom.setDuration(800);
        fadeInBottom.setFillAfter(true);

        android.view.animation.AnimationSet set =
                new android.view.animation.AnimationSet(true);
        set.addAnimation(slideUp);
        set.addAnimation(fadeInBottom);
        set.setFillAfter(true);

        splashBottom.startAnimation(set);

        // 2.5 second baad navigate karo
        new Handler().postDelayed(() -> {

            FirebaseUser user = mAuth.getCurrentUser();

            if (user == null) {
                startActivity(new Intent(this, GetStartedActivity.class));
                finish();
            } else {
                db.collection("users").document(user.getUid()).get()
                        .addOnSuccessListener(doc -> {
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
                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                startActivity(new Intent(this,
                                        GetStartedActivity.class));
                            }
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            startActivity(new Intent(this,
                                    GetStartedActivity.class));
                            finish();
                        });
            }
        }, 2500);
    }
}
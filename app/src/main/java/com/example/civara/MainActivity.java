package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    Button btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        ThemeHelper.loadTheme(this);

        // 🔐 Check if user is logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
            finish();
            return;
        }

        // ✅ Load layout FIRST
        setContentView(R.layout.activity_main);

        // ✅ Then bind views
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
            finish();
        });
    }
}


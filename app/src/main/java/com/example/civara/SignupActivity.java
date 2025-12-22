package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    // UI
    private EditText etName, etEmail, etPassword, etPhone;
    private Button btnSignup;
    private ProgressBar progressBar;
    private TextView tvLogin;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // UI initialization
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Click listeners
        btnSignup.setOnClickListener(v -> signupUser());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void signupUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (!validateInputs(name, email, password, phone)) return;

        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        btnSignup.setEnabled(true);
                        Toast.makeText(
                                this,
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Signup failed",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    // Send email verification (does not block signup)
                    user.sendEmailVerification();

                    saveUserToFirestore(user.getUid(), name, email, phone);
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String phone) {

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(uid)
                .set(userMap)
                .addOnSuccessListener(unused -> {

                    progressBar.setVisibility(View.GONE);
                    btnSignup.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Registration successful! Verify your email.",
                            Toast.LENGTH_LONG
                    ).show();

                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {

                    progressBar.setVisibility(View.GONE);
                    btnSignup.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Firestore error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private boolean validateInputs(String name, String email, String password, String phone) {

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            return false;
        }

        if (TextUtils.isEmpty(email)
                || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email required");
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Minimum 6 characters");
            return false;
        }

        if (TextUtils.isEmpty(phone)
                || phone.length() < 10
                || !phone.matches("\\d+")) {
            etPhone.setError("Valid phone number required");
            return false;
        }

        return true;
    }
}

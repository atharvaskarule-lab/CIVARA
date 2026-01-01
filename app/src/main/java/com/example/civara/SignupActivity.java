package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword;
    private Button btnSignup;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();

        btnSignup.setOnClickListener(v -> registerUser());
    }

    private void initViews() {
        etEmail = findViewById(R.id.etSignupEmail);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etSignupConfirmPassword);
        tilEmail = findViewById(R.id.tilSignupEmail);
        tilPassword = findViewById(R.id.tilSignupPassword);
        tilConfirmPassword = findViewById(R.id.tilSignupConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.signupProgressBar);
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInputs(email, password, confirmPassword)) return;

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user, email);
                        }
                    } else {
                        setLoading(false);
                        Toast.makeText(SignupActivity.this, "Registration Failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser, String email) {
        // Create a map for user data
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", "user"); // Default role is 'user'. Change to 'admin' manually in console for admins.
        user.put("uid", firebaseUser.getUid());

        // Save to 'users' collection with Document ID = UID
        db.collection("users").document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    firebaseUser.sendEmailVerification();
                    setLoading(false);
                    Toast.makeText(this, "Registered! Please verify your email.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    finish(); // Go back to Login
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        // Simple validation logic
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Valid email required");
            return false;
        }
        if (password.length() < 6) {
            tilPassword.setError("Password must be 6+ chars");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignup.setEnabled(!loading);
    }
}
package com.example.civara;

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

    private TextInputEditText etName, etMobile, etEmail, etPassword, etConfirmPassword;
    private TextInputLayout tilName, tilMobile, tilEmail, tilPassword, tilConfirmPassword;
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
        etName = findViewById(R.id.etSignupName);
        etMobile = findViewById(R.id.etSignupMobile);
        etEmail = findViewById(R.id.etSignupEmail);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etSignupConfirmPassword);

        tilName = findViewById(R.id.tilSignupName);
        tilMobile = findViewById(R.id.tilSignupMobile);
        tilEmail = findViewById(R.id.tilSignupEmail);
        tilPassword = findViewById(R.id.tilSignupPassword);
        tilConfirmPassword = findViewById(R.id.tilSignupConfirmPassword);

        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.signupProgressBar);
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInputs(name, mobile, email, password, confirmPassword)) return;

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user, name, mobile, email);
                        }
                    } else {
                        setLoading(false);
                        Toast.makeText(SignupActivity.this, "Auth Failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser, String name, String mobile, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", firebaseUser.getUid());
        userData.put("name", name);
        userData.put("mobile", mobile);
        userData.put("email", email);
        userData.put("role", "user");

        db.collection("users").document(firebaseUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // IMPORTANT: Send verification and wait for the result
                    firebaseUser.sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                setLoading(false);
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this,
                                            "Verification email sent to: " + email, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(SignupActivity.this,
                                            "Failed to send email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                                mAuth.signOut(); // Always sign out after signup to force login later
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs(String name, String mobile, String email, String password, String confirmPassword) {
        tilName.setError(null);
        tilMobile.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name required");
            return false;
        }
        if (mobile.length() < 10) {
            tilMobile.setError("Valid mobile number required");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Valid email required");
            return false;
        }
        if (password.length() < 6) {
            tilPassword.setError("Minimum 6 characters");
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
package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;
    private TextView tvSignupLink, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (currentUser.isEmailVerified()) {
                    checkUserRoleAndRedirect(currentUser.getUid());
                }
            });
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        tilEmail = findViewById(R.id.tilLoginEmail);
        tilPassword = findViewById(R.id.tilLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignupLink = findViewById(R.id.tvSignupLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.loginProgressBar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        tvSignupLink.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        if (!validateInputs()) return;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.reload().addOnCompleteListener(reloadTask -> {
                                if (user.isEmailVerified()) {
                                    checkUserRoleAndRedirect(user.getUid());
                                } else {
                                    setLoading(false);
                                    showEmailVerificationDialog(user);
                                    mAuth.signOut();
                                }
                            });
                        }
                    } else {
                        setLoading(false);
                        handleLoginFailure(task);
                    }
                });
    }

    private void checkUserRoleAndRedirect(String uid) {
        setLoading(true);
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    setLoading(false);
                    if (document.exists()) {
                        String role = document.getString("role");
                        String dept = document.getString("department");

                        Intent intent;

                        // Role check logic
                        if ("admin".equalsIgnoreCase(role)) {

                            // AGAR SUPER ADMIN HAI (Jo aapne dashboard manga hai)
                            if (dept == null || dept.isEmpty() || "super".equalsIgnoreCase(dept)) {
                                intent = new Intent(this, AdminDashboardActivity.class);
                                Toast.makeText(this, "Welcome to Admin Dashboard", Toast.LENGTH_SHORT).show();
                            }
                            // AGAR DEPARTMENT SPECIFIC ADMIN HAI
                            else if ("garbage".equalsIgnoreCase(dept)) {
                                intent = new Intent(this, GarbageAdminActivity.class);
                            } else if ("road".equalsIgnoreCase(dept)) {
                                intent = new Intent(this, RoadDamageAdminActivity.class);
                            } else if ("water".equalsIgnoreCase(dept)) {
                                intent = new Intent(this, WaterSupplyAdminActivity.class);
                            } else if ("sanitation".equalsIgnoreCase(dept)) {
                                intent = new Intent(this, SanitationAdminActivity.class);
                            } else if ("parks".equalsIgnoreCase(dept)) {
                                intent = new Intent(this, ParksAdminActivity.class);
                            } else {
                                intent = new Intent(this, HomepageActivity.class);
                            }

                        } else {
                            // Regular user ke liye Homepage (Aapne MainActivity bola tha, maine HomepageActivity rakha hai flow ke liye)
                            intent = new Intent(this, HomepageActivity.class);
                        }

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "User data not found!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean valid = true;

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            valid = false;
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password required");
            valid = false;
        } else {
            tilPassword.setError(null);
        }
        return valid;
    }

    private void showForgotPasswordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.forgot_password, null);
        EditText etDialogEmail = view.findViewById(R.id.etDialogEmail);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(view)
                .setPositiveButton("Send Link", (d, w) -> {
                    String email = etDialogEmail.getText().toString().trim();
                    if (!TextUtils.isEmpty(email)) {
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Reset link sent!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEmailVerificationDialog(FirebaseUser user) {
        new AlertDialog.Builder(this)
                .setTitle("Verify Email")
                .setMessage("Please verify your email to continue.")
                .setPositiveButton("Resend", (d, w) -> user.sendEmailVerification())
                .setNegativeButton("OK", null)
                .show();
    }

    private void handleLoginFailure(@NonNull Task<?> task) {
        Exception e = task.getException();
        if (e instanceof FirebaseAuthInvalidUserException) {
            tilEmail.setError("User not found");
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            tilPassword.setError("Wrong password");
        } else {
            Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (btnLogin != null) btnLogin.setEnabled(!loading);
    }
}
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

        // Auto-login check
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
                                setLoading(false);
                                if (user.isEmailVerified()) {
                                    checkUserRoleAndRedirect(user.getUid());
                                } else {
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

    private void showEmailVerificationDialog(FirebaseUser user) {
        new AlertDialog.Builder(this)
                .setTitle("Verify Your Email")
                .setMessage("A verification link was sent to " + user.getEmail() + ". Please check your inbox and spam folder.")
                .setPositiveButton("Resend Email", (d, w) -> {
                    user.sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Verification email resent!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "Resend failed: " + task.getException().getMessage());
                                    Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("Dismiss", null)
                .show();
    }

    private void showForgotPasswordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.forgot_password, null);
        EditText etDialogEmail = view.findViewById(R.id.etDialogEmail);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(view)
                .setPositiveButton("Send Link", (d, w) -> {
                    String email = etDialogEmail.getText().toString().trim();

                    if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // SEND PASSWORD RESET EMAIL
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Reset link sent to " + email, Toast.LENGTH_LONG).show();
                                } else {
                                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                    Log.e(TAG, "Reset Error: " + error);
                                    Toast.makeText(LoginActivity.this, "Failed: " + error, Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkUserRoleAndRedirect(String uid) {
        setLoading(true);
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        Intent intent;
                        if ("admin".equals(role)) {
                            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, HomepageActivity.class);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> setLoading(false));
    }

    private boolean validateInputs() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email required");
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

    private void handleLoginFailure(@NonNull com.google.android.gms.tasks.Task<?> task) {
        Exception e = task.getException();
        if (e instanceof FirebaseAuthInvalidUserException) {
            tilEmail.setError("Account does not exist");
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            tilPassword.setError("Incorrect password");
        } else {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (btnLogin != null) btnLogin.setEnabled(!loading);
    }
}
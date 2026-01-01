package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;
    private TextView tvSignupLink, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. MUST set content view first so findViewById works
        setContentView(R.layout.activity_login);

        // 2. Initialize your views (ProgressBar, etc.)
        initViews();
        setupClickListeners();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 3. Now check for the user
        if (currentUser != null && currentUser.isEmailVerified()) {
            checkUserRoleAndRedirect(currentUser.getUid());
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
        tvSignupLink.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        if (!validateInputs()) return;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Instead of redirectToHomepage(), call the role check
                            checkUserRoleAndRedirect(user.getUid());
                        } else {
                            showEmailVerificationDialog(user);
                        }
                    }else {
                        handleLoginFailure(task);
                    }
                });
    }

    private boolean validateInputs() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password required");
            return false;
        }

        return true;
    }

    private void handleLoginFailure(@NonNull com.google.android.gms.tasks.Task<?> task) {
        Exception e = task.getException();

        if (e instanceof FirebaseAuthInvalidUserException) {
            tilEmail.setError("Account does not exist");
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            tilPassword.setError("Incorrect password");
        }

        Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show();
    }

    private void showEmailVerificationDialog(FirebaseUser user) {
        if (user == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Email not verified")
                .setMessage("Please verify your email to continue.")
                .setPositiveButton("Resend", (d, w) ->
                        user.sendEmailVerification()
                                .addOnSuccessListener(v ->
                                        Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("OK", null)
                .show();
    }

    private void showForgotPasswordDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.forgot_password, null);

        EditText etEmail = view.findViewById(R.id.etDialogEmail);

        new AlertDialog.Builder(this)
                .setTitle("Forgot Password")
                .setView(view)
                .setPositiveButton("Send", (d, w) -> {
                    String email = etEmail.getText().toString().trim();

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mAuth.sendPasswordResetEmail(email)
                            .addOnSuccessListener(v ->
                                    Toast.makeText(this, "Reset link sent", Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkUserRoleAndRedirect(String uid) {
        setLoading(true);
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        if ("admin".equals(role)) {
                            Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // DEBUG TOAST: Tell us what the role actually is
                            Toast.makeText(this, "Logged in as: " + (role == null ? "No Role Found" : role), Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Firestore document missing for this UID!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void setLoading(boolean loading) {
        // Check if views are null before accessing them
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (btnLogin != null) btnLogin.setEnabled(!loading);
        if (tvSignupLink != null) tvSignupLink.setEnabled(!loading);
        if (tvForgotPassword != null) tvForgotPassword.setEnabled(!loading);
    }
}

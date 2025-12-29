package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ComplaintStatusActivity extends AppCompatActivity {

    private Button btnViewComplaints, btnRefresh;
    private TextView tvStatus, tvCount;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_status);

        // Link UI components
        btnViewComplaints = findViewById(R.id.btnViewComplaints);
        btnRefresh = findViewById(R.id.btnRefresh);
        tvStatus = findViewById(R.id.tvStatus);
        tvCount = findViewById(R.id.tvCount);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Fetch data on start
        loadComplaints();

        btnRefresh.setOnClickListener(v -> loadComplaints());

        btnViewComplaints.setOnClickListener(v -> {
            // Note: Ensure ViewComplaintsActivity exists in your project
            Intent intent = new Intent(ComplaintStatusActivity.this, ViewComplaintsActivity.class);
            startActivity(intent);
        });
    }

    private void loadComplaints() {
        if (auth.getCurrentUser() == null) {
            tvStatus.setText("User not logged in.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Syncing...");

        String userId = auth.getCurrentUser().getUid();

        db.collection("complaints")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    progressBar.setVisibility(View.GONE);

                    if (query.isEmpty()) {
                        tvStatus.setText("No complaints found.");
                        tvCount.setText("0");
                        return;
                    }

                    tvCount.setText(String.valueOf(query.size()));
                    StringBuilder sb = new StringBuilder();

                    for (QueryDocumentSnapshot doc : query) {
                        String type = doc.getString("type");
                        String status = doc.getString("status");

                        // Fallback values if fields are null in Firestore
                        if (type == null) type = "Unknown Type";
                        if (status == null) status = "Pending";

                        sb.append("• ").append(type)
                                .append("\n  Status: ").append(status)
                                .append("\n\n");
                    }

                    tvStatus.setText(sb.toString());
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("Error: " + e.getMessage());
                    Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                });
    }
}
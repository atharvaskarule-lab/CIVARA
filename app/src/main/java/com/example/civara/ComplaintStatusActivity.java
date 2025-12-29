package com.example.civara;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class ComplaintStatusActivity extends AppCompatActivity {

    private TextView tvDetailType, tvDetailDesc, tvDetailDate, tvCurrentStatus;
    private View step1, step2, step3;
    private TextView label1, label2, label3;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_status);

        db = FirebaseFirestore.getInstance();

        // UI Links
        tvDetailType = findViewById(R.id.tvDetailType);
        tvDetailDesc = findViewById(R.id.tvDetailDesc);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        progressBar = findViewById(R.id.progressBar);

        // Timeline Links - Ensure these match your XML IDs exactly
        step1 = findViewById(R.id.step_submitted);
        step2 = findViewById(R.id.step_progress);
        step3 = findViewById(R.id.step_resolved);
        label1 = findViewById(R.id.label_submitted);
        label2 = findViewById(R.id.label_progress);
        label3 = findViewById(R.id.label_resolved);

        String complaintId = getIntent().getStringExtra("complaintId");
        if (complaintId != null) {
            fetchComplaintDetails(complaintId);
        }

        // Fixed back button reference
        View backBtn = findViewById(R.id.btnBack);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }
    }

    private void fetchComplaintDetails(String id) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("complaints").document(id).get().addOnSuccessListener(doc -> {
            progressBar.setVisibility(View.GONE);
            if (doc.exists()) {
                tvDetailType.setText(doc.getString("type"));
                tvDetailDesc.setText(doc.getString("description"));
                tvDetailDate.setText("Filed on: " + doc.getString("date"));

                String status = doc.getString("status");
                if (status == null) status = "Pending";

                tvCurrentStatus.setText("Current Status: " + status);
                updateTimeline(status);
            }
        });
    }

    private void updateTimeline(String status) {
        int grey = Color.parseColor("#CBD5E1");
        int active = Color.parseColor("#2563EB"); // Blue

        // Default: Reset all to grey
        step1.setBackgroundColor(grey);
        step2.setBackgroundColor(grey);
        step3.setBackgroundColor(grey);
        label1.setTextColor(grey);
        label2.setTextColor(grey);
        label3.setTextColor(grey);

        // Logic based on status strings in Firestore
        if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("In Progress") || status.equalsIgnoreCase("Solved")) {
            step1.setBackgroundColor(active);
            label1.setTextColor(active);
        }
        if (status.equalsIgnoreCase("In Progress") || status.equalsIgnoreCase("Solved")) {
            step2.setBackgroundColor(active);
            label2.setTextColor(active);
        }
        if (status.equalsIgnoreCase("Solved")) {
            step3.setBackgroundColor(active);
            label3.setTextColor(active);
        }
    }
}
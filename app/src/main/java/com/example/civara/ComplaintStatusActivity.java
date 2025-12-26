package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ComplaintStatusActivity extends AppCompatActivity {

    Button btnViewComplaints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the correct XML layout
        setContentView(R.layout.activity_complaint_status);

        // Initialize button
        btnViewComplaints = findViewById(R.id.btnViewComplaints);

        // Navigate to View Complaints page
        btnViewComplaints.setOnClickListener(v -> {
            startActivity(new Intent(ComplaintStatusActivity.this, ViewComplaintsActivity.class));
        });

        // You can also fetch complaints from DB here and update tvStatus dynamically
        // Example placeholder:
        // TextView tvStatus = findViewById(R.id.tvStatus);
        // tvStatus.setText("Complaint 1: In Progress\nComplaint 2: Solved");
    }
}

package com.example.civara;

import android.content.Intent;
import android.os.Bundle;import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ComplaintDashboardActivity extends AppCompatActivity {

    Button btnFileComplaint, btnStatus, btnView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_dashboard);

        btnFileComplaint = findViewById(R.id.btnFileComplaint);
        // ✅ CORRECTED: Changed the ID to match the layout file
        btnStatus = findViewById(R.id.btnStatus);
        btnView = findViewById(R.id.btnView);

        // File Complaint Page
        btnFileComplaint.setOnClickListener(v ->
                startActivity(new Intent(
                        ComplaintDashboardActivity.this,
                        FileComplaintActivity.class)));

        // Status Page
        btnStatus.setOnClickListener(v ->
                startActivity(new Intent(
                        ComplaintDashboardActivity.this,
                        ComplaintStatusActivity.class)));

        // View Complaints Page
        btnView.setOnClickListener(v ->
                startActivity(new Intent(
                        ComplaintDashboardActivity.this,
                        ViewComplaintsActivity.class)));
    }
}

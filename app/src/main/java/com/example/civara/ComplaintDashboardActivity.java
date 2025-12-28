package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class ComplaintDashboardActivity extends AppCompatActivity {

    // Using MaterialCardView instead of standard Button for a better feel
    MaterialCardView btnFileComplaint, btnStatus, btnView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make status bar icons dark for the light background
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_complaint_dashboard);

        // Bind Views
        btnFileComplaint = findViewById(R.id.btnFileComplaint);
        btnStatus = findViewById(R.id.btnStatus);
        btnView = findViewById(R.id.btnView);

        // Navigation Logic
        btnFileComplaint.setOnClickListener(v ->
                startActivity(new Intent(this, FileComplaintActivity.class)));

        btnStatus.setOnClickListener(v ->
                startActivity(new Intent(this, ComplaintStatusActivity.class)));

        btnView.setOnClickListener(v ->
                startActivity(new Intent(this, ViewComplaintsActivity.class)));
    }
}
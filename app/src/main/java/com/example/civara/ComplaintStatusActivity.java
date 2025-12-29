package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ComplaintStatusActivity extends AppCompatActivity {

    Button btnViewComplaints, btnRefresh;
    TextView tvStatus, tvCount;
    ProgressBar progressBar;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_status);

        btnViewComplaints = findViewById(R.id.btnViewComplaints);
        btnRefresh = findViewById(R.id.btnRefresh);
        tvStatus = findViewById(R.id.tvStatus);
        tvCount = findViewById(R.id.tvCount);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadComplaints();

        btnRefresh.setOnClickListener(v -> loadComplaints());

        btnViewComplaints.setOnClickListener(v ->
                startActivity(new Intent(
                        ComplaintStatusActivity.this,
                        ViewComplaintsActivity.class
                )));
    }

    private void loadComplaints() {
        if (auth.getCurrentUser() == null) {
            tvStatus.setText("Please login again");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("");

        String userId = auth.getCurrentUser().getUid();

        db.collection("complaints")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {

                    progressBar.setVisibility(View.GONE);

                    if (query.isEmpty()) {
                        tvStatus.setText("No complaints found");
                        tvCount.setText("0");
                        return;
                    }

                    tvCount.setText(String.valueOf(query.size()));
                    StringBuilder sb = new StringBuilder();

                    for (QueryDocumentSnapshot doc : query) {
                        String type = doc.getString("type");
                        String status = doc.getString("status");

                        sb.append("• ").append(type)
                                .append("\nStatus : ").append(status)
                                .append("\n\n");
                    }

                    tvStatus.setText(sb.toString());
                });
    }
}

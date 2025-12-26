package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ComplaintStatusActivity extends AppCompatActivity {

    Button btnViewComplaints;
    TextView tvStatus;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_status);

        btnViewComplaints = findViewById(R.id.btnViewComplaints);
        tvStatus = findViewById(R.id.tvStatus);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String userId = auth.getCurrentUser().getUid();

        // 🔥 FETCH COMPLAINT STATUS FROM FIRESTORE
        db.collection("complaints")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.isEmpty()) {
                        tvStatus.setText("No complaints found");
                        return;
                    }

                    StringBuilder statusText = new StringBuilder();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String type = doc.getString("type");
                        String status = doc.getString("status");

                        statusText.append("• ")
                                .append(type)
                                .append(" : ")
                                .append(status)
                                .append("\n\n");
                    }

                    tvStatus.setText(statusText.toString());
                });

        // View All Complaints Button
        btnViewComplaints.setOnClickListener(v -> {
            startActivity(new Intent(
                    ComplaintStatusActivity.this,
                    ViewComplaintsActivity.class
            ));
        });
    }
}

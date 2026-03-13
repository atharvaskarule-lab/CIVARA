package com.example.civara;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class RoadDamageAdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<IssueModel> roadIssues;
    private FirebaseFirestore db;
    private GarbageAdapter adapter; // Humne purana adapter hi reuse kiya hai kyunki layout same hai

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_damage_admin);

        recyclerView = findViewById(R.id.roadRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        roadIssues = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        loadRoadIssues();
    }

    private void loadRoadIssues() {
        // Yahan category filter "Road Damage" hai jo FileComplaintActivity se match karta hai
        db.collection("complaints")
                .whereEqualTo("category", "Road Damage")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    roadIssues.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        IssueModel issue = document.toObject(IssueModel.class);
                        issue.setDocumentId(document.getId());
                        roadIssues.add(issue);
                    }
                    updateUI(roadIssues);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(List<IssueModel> list) {
        if (list.size() > 0) {
            adapter = new GarbageAdapter(list, (issue, position) -> {
                showStatusDialog(issue);
            });
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No Road Damage issues found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatusDialog(IssueModel issue) {
        String[] options = {"Pending", "Under Repair", "Resolved", "Rejected"};

        new AlertDialog.Builder(this)
                .setTitle("Update Repair Status")
                .setItems(options, (dialog, which) -> {
                    String newStatus = options[which];
                    updateStatusInFirestore(issue.getDocumentId(), newStatus);
                })
                .show();
    }

    private void updateStatusInFirestore(String docId, String status) {
        if (docId == null) return;

        db.collection("complaints").document(docId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show();
                    loadRoadIssues(); // Refresh list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
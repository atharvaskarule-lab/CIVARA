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

public class WaterSupplyAdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<IssueModel> waterIssues;
    private FirebaseFirestore db;
    private GarbageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_supply_admin);

        recyclerView = findViewById(R.id.waterRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        waterIssues = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        loadWaterIssues();
    }

    private void loadWaterIssues() {
        // "Water Supply" category filter
        db.collection("complaints")
                .whereEqualTo("category", "Water Supply")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    waterIssues.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        IssueModel issue = document.toObject(IssueModel.class);
                        issue.setDocumentId(document.getId());
                        waterIssues.add(issue);
                    }
                    updateUI(waterIssues);
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
            Toast.makeText(this, "No Water Supply issues found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatusDialog(IssueModel issue) {
        String[] options = {"Pending", "Under Maintenance", "Resolved", "Rejected"};

        new AlertDialog.Builder(this)
                .setTitle("Update Maintenance Status")
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
                    loadWaterIssues();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
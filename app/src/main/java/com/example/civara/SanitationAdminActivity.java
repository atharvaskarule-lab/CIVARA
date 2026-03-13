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

public class SanitationAdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<IssueModel> sanitationIssues;
    private FirebaseFirestore db;
    private GarbageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sanitation_admin);

        recyclerView = findViewById(R.id.sanitationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sanitationIssues = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        loadSanitationIssues();
    }

    private void loadSanitationIssues() {
        // Sanitation category filter logic
        db.collection("complaints")
                .whereEqualTo("category", "Sanitation")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sanitationIssues.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        IssueModel issue = document.toObject(IssueModel.class);
                        issue.setDocumentId(document.getId());
                        sanitationIssues.add(issue);
                    }
                    updateUI(sanitationIssues);
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
            Toast.makeText(this, "No Sanitation issues found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatusDialog(IssueModel issue) {
        String[] options = {"Pending", "Cleaning in Progress", "Resolved", "Rejected"};

        new AlertDialog.Builder(this)
                .setTitle("Update Sanitation Status")
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
                    loadSanitationIssues();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
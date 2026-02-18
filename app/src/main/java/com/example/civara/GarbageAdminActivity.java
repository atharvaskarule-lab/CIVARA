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

public class GarbageAdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<IssueModel> garbageIssues;
    private FirebaseFirestore db;
    private GarbageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garbage_admin);

        recyclerView = findViewById(R.id.garbageRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        garbageIssues = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        loadGarbageIssues();
    }

    private void loadGarbageIssues() {
        db.collection("Issues")
                .whereEqualTo("category", "Garbage")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    garbageIssues.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        IssueModel issue = document.toObject(IssueModel.class);
                        // 1. Loop mein ID save karna
                        issue.setDocumentId(document.getId());
                        garbageIssues.add(issue);
                    }

                    updateUI(garbageIssues);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 2. updateUI method click listener ke saath
    private void updateUI(List<IssueModel> list) {
        if (list.size() > 0) {
            adapter = new GarbageAdapter(list, (issue, position) -> {
                showStatusDialog(issue);
            });
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No issues found", Toast.LENGTH_SHORT).show();
        }
    }

    // 3. Dialog Box status change karne ke liye
    private void showStatusDialog(IssueModel issue) {
        String[] options = {"Pending", "Resolved", "Rejected"};

        new AlertDialog.Builder(this)
                .setTitle("Change Status")
                .setItems(options, (dialog, which) -> {
                    String newStatus = options[which];
                    updateStatusInFirestore(issue.getDocumentId(), newStatus);
                })
                .show();
    }

    // 4. Firestore Update function
    private void updateStatusInFirestore(String docId, String status) {
        if (docId == null) return;

        db.collection("Issues").document(docId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show();
                    loadGarbageIssues(); // List refresh karein
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
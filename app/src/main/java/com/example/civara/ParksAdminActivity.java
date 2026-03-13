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

public class ParksAdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<IssueModel> parkIssues;
    private FirebaseFirestore db;
    private GarbageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parks_admin);

        recyclerView = findViewById(R.id.parksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        parkIssues = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        loadParkIssues();
    }

    private void loadParkIssues() {
        // collectionGroup ka use taaki sabhi users ke "Issue" sub-collection se data mile
        db.collectionGroup("Issue")
                .whereEqualTo("category", "Parks and Public Places")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    parkIssues.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        IssueModel issue = document.toObject(IssueModel.class);
                        issue.setDocumentId(document.getId());
                        parkIssues.add(issue);
                    }
                    updateUI(parkIssues);
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
            Toast.makeText(this, "No Park issues found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatusDialog(IssueModel issue) {
        String[] options = {"Pending", "Under Maintenance", "Resolved", "Rejected"};

        new AlertDialog.Builder(this)
                .setTitle("Update Park Issue Status")
                .setItems(options, (dialog, which) -> {
                    String newStatus = options[which];
                    updateStatusInFirestore(issue, newStatus);
                })
                .show();
    }

    private void updateStatusInFirestore(IssueModel issue, String status) {
        // Note: Sub-collection mein update karne ke liye document path ki zaroorat hoti hai
        db.collectionGroup("Issue").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (doc.getId().equals(issue.getDocumentId())) {
                    doc.getReference().update("status", status)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                                loadParkIssues();
                            });
                }
            }
        });
    }
}
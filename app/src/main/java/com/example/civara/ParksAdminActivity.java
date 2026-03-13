package com.example.civara;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
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
        // Changed to "complaints" collection for consistency and added real-time listener
        db.collection("complaints")
                .whereEqualTo("category", "Parks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        parkIssues.clear();
                        for (QueryDocumentSnapshot document : value) {
                            IssueModel issue = document.toObject(IssueModel.class);
                            issue.setDocumentId(document.getId());
                            parkIssues.add(issue);
                        }
                        updateUI(parkIssues);
                    }
                });
    }

    private void updateUI(List<IssueModel> list) {
        if (adapter == null) {
            adapter = new GarbageAdapter(list, new GarbageAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(IssueModel issue, int position) {
                    showStatusDialog(issue);
                }

                @Override
                public void onVoteClick(IssueModel issue, int position) {
                    handleVote(issue);
                }
            });
            adapter.setAdmin(true);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void handleVote(IssueModel issue) {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null || issue.getDocumentId() == null) return;

        db.collection("complaints").document(issue.getDocumentId())
                .update("voterIds", FieldValue.arrayUnion(currentUid),
                        "voteCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Vote added!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Vote failed", Toast.LENGTH_SHORT).show());
    }

    private void showStatusDialog(IssueModel issue) {
        String[] options = {"Pending", "Under Maintenance", "Resolved", "Rejected"};

        new AlertDialog.Builder(this)
                .setTitle("Update Park Issue Status")
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
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
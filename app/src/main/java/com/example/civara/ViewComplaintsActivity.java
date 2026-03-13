package com.example.civara;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ViewComplaintsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_complaints);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewComplaints);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        complaintList = new ArrayList<>();

        adapter = new ComplaintAdapter(complaintList, new ComplaintAdapter.OnComplaintClickListener() {
            @Override
            public void onComplaintClick(Complaint complaint) {
                // Handle complaint click
            }

            @Override
            public void onVoteClick(Complaint complaint) {
                handleVote(complaint);
            }
        });
        recyclerView.setAdapter(adapter);

        loadComplaintHistory();
    }

    private void handleVote(Complaint complaint) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null || complaint.getDocumentId() == null) return;

        db.collection("complaints").document(complaint.getDocumentId())
                .update("voterIds", FieldValue.arrayUnion(currentUserId),
                        "voteCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Vote added!", Toast.LENGTH_SHORT).show();
                    // Optionally refresh the list or local object
                    loadComplaintHistory(); 
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Vote failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadComplaintHistory() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        progressBar.setVisibility(View.VISIBLE);

        db.collection("complaints")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    complaintList.clear();

                    Log.d("History", "Total documents found: " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Complaint complaint = doc.toObject(Complaint.class);
                        complaint.setDocumentId(doc.getId());
                        complaintList.add(complaint);
                    }

                    adapter.notifyDataSetChanged();

                    if (complaintList.isEmpty()) {
                        Toast.makeText(this, "No history found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("History", "Error fetching data: " + e.getMessage());
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
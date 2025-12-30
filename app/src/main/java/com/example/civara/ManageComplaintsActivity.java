package com.example.civara;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageComplaintsActivity extends AppCompatActivity {

    private RecyclerView rvComplaints;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_complaints);

        db = FirebaseFirestore.getInstance();
        rvComplaints = findViewById(R.id.rvManageComplaints);

        complaintList = new ArrayList<>();
        // Note: You'll create ComplaintAdapter next
        adapter = new ComplaintAdapter(complaintList, complaint -> {
            // Handle clicking a complaint (e.g., to update status)
            Toast.makeText(this, "Selected: " + complaint.getTitle(), Toast.LENGTH_SHORT).show();
        });

        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        rvComplaints.setAdapter(adapter);

        fetchComplaints();
    }

    private void fetchComplaints() {
        db.collection("complaints")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        complaintList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Complaint c = doc.toObject(Complaint.class);
                            complaintList.add(c);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
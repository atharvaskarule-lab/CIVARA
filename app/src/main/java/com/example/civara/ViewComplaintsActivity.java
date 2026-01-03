package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ViewComplaintsActivity extends AppCompatActivity {

    private RecyclerView rvComplaints;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList; // Local list
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_complaints);

        rvComplaints = findViewById(R.id.rvComplaints);
        SearchView searchView = findViewById(R.id.searchView);

        complaintList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        setupRecyclerView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        loadComplaints();
    }

    private void setupRecyclerView() {
        adapter = new ComplaintAdapter(complaintList, complaint -> {
            Intent intent = new Intent(this, ComplaintStatusActivity.class);
            intent.putExtra("complaintId", complaint.getDocumentId());
            startActivity(intent);
        });
        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        rvComplaints.setAdapter(adapter);
    }

    private void loadComplaints() {
        db.collection("complaints")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    List<Complaint> fetchedList = new ArrayList<>();
                    query.forEach(doc -> {
                        Complaint c = doc.toObject(Complaint.class);
                        c.setDocumentId(doc.getId());
                        fetchedList.add(c);
                    });
                    adapter.updateList(fetchedList); // Refresh the list and backup
                });
    }
}
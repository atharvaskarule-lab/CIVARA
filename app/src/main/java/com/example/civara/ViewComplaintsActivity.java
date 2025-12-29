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
    private List<Complaint> fullList;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_complaints);

        rvComplaints = findViewById(R.id.rvComplaints);
        SearchView searchView = findViewById(R.id.searchView);

        fullList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
        adapter = new ComplaintAdapter(fullList, complaint -> {
            // Detailed View Option: Open Status page with details
            Intent intent = new Intent(this, ComplaintStatusActivity.class);
            intent.putExtra("complaintId", complaint.getId());
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
                    fullList.clear();
                    query.forEach(doc -> {
                        Complaint c = doc.toObject(Complaint.class);
                        // Ensure you store the document ID
                        fullList.add(new Complaint(doc.getId(), c.getType(),
                                c.getDescription(), c.getStatus(), c.getDate()));
                    });
                    adapter.updateList(fullList);
                });
    }
}
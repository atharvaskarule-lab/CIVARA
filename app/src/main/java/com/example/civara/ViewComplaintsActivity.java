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

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        setupRecyclerView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        loadComplaints();
    }

    private void setupRecyclerView() {
        adapter = new ComplaintAdapter(fullList, complaint -> {
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
                    fullList.clear();
                    query.forEach(doc -> {
                        Complaint c = doc.toObject(Complaint.class);
                        // Assigning the Firestore Document ID to the object
                        c.setDocumentId(doc.getId());
                        fullList.add(c);
                    });
                    adapter.updateList(fullList);
                });
    }
}
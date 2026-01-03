package com.example.civara;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageComplaintsActivity extends AppCompatActivity {

    private RecyclerView rvComplaints;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList; // Master list from Firebase
    private FirebaseFirestore db;

    private ImageButton btnFilterStatus;
    private SearchView searchViewAdmin;
    private LinearLayout emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_complaints);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // 1. Initialize UI Views
        rvComplaints = findViewById(R.id.rvManageComplaints);
        btnFilterStatus = findViewById(R.id.btnFilterStatus);
        searchViewAdmin = findViewById(R.id.searchViewAdmin);
        emptyView = findViewById(R.id.emptyView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        complaintList = new ArrayList<>();

        // 2. Setup Adapter
        // When a complaint is clicked, show the status update dialog
        adapter = new ComplaintAdapter(complaintList, complaint -> {
            showUpdateStatusDialog(complaint);
        });

        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        rvComplaints.setAdapter(adapter);

        // 3. Setup Swipe to Refresh
        swipeRefreshLayout.setOnRefreshListener(this::fetchComplaints);

        // 4. Setup Filter Button
        btnFilterStatus.setOnClickListener(v -> showFilterDialog());

        // 5. Setup Search Functionality
        setupSearchView();

        // Initial Fetch
        fetchComplaints();
    }

    private void setupSearchView() {
        searchViewAdmin.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                    // Note: In a production app, you'd add a delay or a callback
                    // to toggleEmptyState here if the search results are 0.
                }
                return true;
            }
        });
    }

    /**
     * Fetches complaints from Firestore with real-time updates.
     */
    private void fetchComplaints() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        db.collection("complaints")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    swipeRefreshLayout.setRefreshing(false);

                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        complaintList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Complaint c = doc.toObject(Complaint.class);
                            c.setDocumentId(doc.getId()); // Map the Firestore Auto-ID
                            complaintList.add(c);
                        }

                        adapter.updateList(complaintList);
                        toggleEmptyState(complaintList.size());
                    }
                });
    }

    private void showUpdateStatusDialog(Complaint complaint) {
        String[] statusOptions = {"Pending", "In Progress", "Resolved", "Rejected"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Status: " + complaint.getTitle());
        builder.setItems(statusOptions, (dialog, which) -> {
            String newStatus = statusOptions[which];
            updateStatusInFirestore(complaint.getDocumentId(), newStatus);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateStatusInFirestore(String docId, String newStatus) {
        if (docId == null || docId.isEmpty()) return;

        db.collection("complaints").document(docId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showFilterDialog() {
        String[] filterOptions = {"All", "Pending", "In Progress", "Resolved", "Rejected"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Category");
        builder.setItems(filterOptions, (dialog, which) -> {
            applyStatusFilter(filterOptions[which]);
        });
        builder.show();
    }

    private void applyStatusFilter(String status) {
        if (status.equalsIgnoreCase("All")) {
            adapter.updateList(complaintList);
            toggleEmptyState(complaintList.size());
        } else {
            List<Complaint> filtered = new ArrayList<>();
            for (Complaint c : complaintList) {
                if (c.getStatus() != null && c.getStatus().equalsIgnoreCase(status)) {
                    filtered.add(c);
                }
            }
            adapter.updateList(filtered);
            toggleEmptyState(filtered.size());
        }
    }

    /**
     * Toggles visibility between the list and the "No Complaints" message.
     */
    private void toggleEmptyState(int count) {
        if (count == 0) {
            rvComplaints.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rvComplaints.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
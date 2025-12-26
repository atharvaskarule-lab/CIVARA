package com.example.civara;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

// ✅ CORRECTED: Class name now matches the file name (ViewComplaintsActivity)
public class ViewComplaintsActivity extends AppCompatActivity {

    ListView listComplaints;
    ArrayList<String> complaintList;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The layout file is probably named activity_view_complaints.xml (plural)
        setContentView(R.layout.activity_view_complaints);

        listComplaints = findViewById(R.id.listComplaints);
        complaintList = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                complaintList);
        listComplaints.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Added a null check for safety
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            loadComplaints();
        }
    }

    private void loadComplaints() {
        db.collection("complaints")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    complaintList.clear();
                    query.forEach(doc -> {
                        String item =
                                "Description: " + doc.getString("description") +
                                        "\nType: " + doc.getString("type") +
                                        "\nStatus: " + doc.getString("status");
                        complaintList.add(item);
                    });
                    adapter.notifyDataSetChanged();
                });
    }
}

package com.example.civara;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationItem> list;
    private FirebaseFirestore db;
    private TextView tvNoNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();
        tvNoNotifications = findViewById(R.id.tvNoNotifications);
        rvNotifications = findViewById(R.id.rvNotifications);

        findViewById(R.id.btnBackNotif).setOnClickListener(v -> finish());

        setupRecyclerView();
        listenForGlobalNotifications();
    }

    private void setupRecyclerView() {
        list = new ArrayList<>();
        adapter = new NotificationAdapter(list);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private void listenForGlobalNotifications() {
        // Fetching from a global "notifications" collection
        // Ordered by timestamp so newest appear first
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        list.clear();
                        value.forEach(doc -> {
                            NotificationItem item = doc.toObject(NotificationItem.class);
                            list.add(item);
                        });

                        adapter.notifyDataSetChanged();

                        // Toggle empty state visibility
                        tvNoNotifications.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }
}
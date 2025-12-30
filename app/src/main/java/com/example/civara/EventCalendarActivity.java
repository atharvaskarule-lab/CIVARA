package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventCalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvTodayEventTitle, tvTodayEventLocation, tvEventTitle, tvEventDesc, tvSelectedDateLabel;
    private ImageButton btnShareEvent, btnSetReminder;
    private TextInputEditText etComment;
    private Button btnSubmitFeedback;

    // RecyclerView Components
    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private List<Event> eventList;

    private FirebaseFirestore db;
    private String selectedDate;
    private String currentEventId = "none";
    private int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_calendar);

        db = FirebaseFirestore.getInstance();

        // 1. Bind Existing Views
        calendarView = findViewById(R.id.calendarView);
        tvTodayEventTitle = findViewById(R.id.tvTodayEventTitle);
        tvTodayEventLocation = findViewById(R.id.tvTodayEventLocation);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventDesc = findViewById(R.id.tvEventDesc);
        tvSelectedDateLabel = findViewById(R.id.tvSelectedDateLabel);
        btnShareEvent = findViewById(R.id.btnShareEvent);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        etComment = findViewById(R.id.etComment);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        // 2. Setup RecyclerView and Adapter
        rvEvents = findViewById(R.id.rvEvents); // Ensure this ID is in your XML
        eventList = new ArrayList<>();

        // Initialize Adapter with click listener
        adapter = new EventAdapter(eventList, event -> {
            // When an event in the list is clicked, update the Detail Card
            tvEventTitle.setText(event.getEventTitle());
            tvEventDesc.setText(event.getEventDescription());
            selectedDate = event.getEventDate();
            tvSelectedDateLabel.setText("Selected: " + selectedDate);
            // You can also scroll the calendar to this date if needed
        });

        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);

        // 3. Set initial today's date
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        selectedDate = mDay + "/" + (mMonth + 1) + "/" + mYear;
        tvSelectedDateLabel.setText("Selected: " + selectedDate);

        // 4. Fetch Data
        fetchTodayHighlight(selectedDate);
        fetchEventForDate(selectedDate);
        fetchAllEvents(); // Load the full list for the RecyclerView

        // 5. Listeners
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            mYear = year;
            mMonth = month;
            mDay = dayOfMonth;
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvSelectedDateLabel.setText("Selected: " + selectedDate);
            fetchEventForDate(selectedDate);
        });

        btnShareEvent.setOnClickListener(v -> shareEvent());
        btnSetReminder.setOnClickListener(v -> setCalendarReminder());
        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void fetchAllEvents() {
        // Fetches all upcoming events for the RecyclerView
        db.collection("events")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        eventList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Event event = doc.toObject(Event.class);
                            eventList.add(event);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // ... Keep your fetchTodayHighlight, fetchEventForDate, shareEvent,
    // setCalendarReminder, and submitFeedback methods exactly as they were ...

    private void fetchTodayHighlight(String date) {
        db.collection("events").whereEqualTo("date", date).limit(1).get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) snapshots.getDocuments().get(0);
                        tvTodayEventTitle.setText(doc.getString("title"));
                        String loc = doc.getString("location") != null ? doc.getString("location") : "Community Hub";
                        tvTodayEventLocation.setText("Location: " + loc);
                    } else {
                        tvTodayEventTitle.setText("No major events today");
                        tvTodayEventLocation.setText("Tap calendar to explore");
                    }
                });
    }

    private void fetchEventForDate(String date) {
        db.collection("events").whereEqualTo("date", date).get().addOnSuccessListener(snapshots -> {
            if (!snapshots.isEmpty()) {
                QueryDocumentSnapshot doc = (QueryDocumentSnapshot) snapshots.getDocuments().get(0);
                tvEventTitle.setText(doc.getString("title"));
                tvEventDesc.setText(doc.getString("description"));
                currentEventId = doc.getId();
            } else {
                tvEventTitle.setText("No Event Scheduled");
                tvEventDesc.setText("Check another date for activities.");
                currentEventId = "none";
            }
        });
    }

    private void shareEvent() {
        if (currentEventId.equals("none")) {
            Toast.makeText(this, "No event to share", Toast.LENGTH_SHORT).show();
            return;
        }
        String msg = "📅 *Civara Event Alert*\n\n*Event:* " + tvEventTitle.getText() +
                "\n*Date:* " + selectedDate + "\n*Details:* " + tvEventDesc.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        startActivity(Intent.createChooser(intent, "Share Event via"));
    }

    private void setCalendarReminder() {
        if (currentEventId.equals("none")) {
            Toast.makeText(this, "Please select a date with an event", Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(mYear, mMonth, mDay, 9, 0);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, tvEventTitle.getText().toString())
                .putExtra(CalendarContract.Events.DESCRIPTION, tvEventDesc.getText().toString())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "Community Hall")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        startActivity(intent);
    }

    private void submitFeedback() {
        String comment = etComment.getText().toString().trim();
        if (comment.isEmpty()) return;
        if (currentEventId.equals("none")) {
            Toast.makeText(this, "Select an event to comment on", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", FirebaseAuth.getInstance().getUid());
        data.put("eventId", currentEventId);
        data.put("eventTitle", tvEventTitle.getText().toString());
        data.put("comment", comment);
        data.put("timestamp", System.currentTimeMillis());
        db.collection("event_feedback").add(data).addOnSuccessListener(ref -> {
            Toast.makeText(this, "Feedback shared!", Toast.LENGTH_SHORT).show();
            etComment.setText("");
        });
    }
}
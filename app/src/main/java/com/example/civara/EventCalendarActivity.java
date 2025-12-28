package com.example.civara;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventCalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvTodayEventTitle, tvTodayEventLocation, tvEventTitle, tvEventDesc, tvSelectedDateLabel;
    private ImageButton btnShareEvent, btnSetReminder;
    private TextInputEditText etComment;
    private Button btnSubmitFeedback;

    private FirebaseFirestore db;
    private String selectedDate;
    private String currentEventId = "none";
    private int mYear, mMonth, mDay; // For reminder logic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_calendar);

        db = FirebaseFirestore.getInstance();

        // Bind Views
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

        // Set initial today's date
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        String today = mDay + "-" + (mMonth + 1) + "-" + mYear;
        selectedDate = today;
        fetchTodayHighlight(today);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            mYear = year; mMonth = month; mDay = dayOfMonth;
            selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;
            tvSelectedDateLabel.setText("Selected: " + selectedDate);
            fetchEventForDate(selectedDate);
        });

        btnShareEvent.setOnClickListener(v -> shareEvent());
        btnSetReminder.setOnClickListener(v -> setCalendarReminder());
        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void fetchTodayHighlight(String date) {
        db.collection("events").document(date).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvTodayEventTitle.setText(doc.getString("title"));
                tvTodayEventLocation.setText("Location: " + doc.getString("location"));
            }
        });
    }

    private void fetchEventForDate(String date) {
        db.collection("events").whereEqualTo("date", date).get().addOnSuccessListener(snapshots -> {
            if (!snapshots.isEmpty()) {
                tvEventTitle.setText(snapshots.getDocuments().get(0).getString("title"));
                tvEventDesc.setText(snapshots.getDocuments().get(0).getString("description"));
                currentEventId = snapshots.getDocuments().get(0).getId();
            } else {
                tvEventTitle.setText("No Event Scheduled");
                tvEventDesc.setText("Check another date for city activities.");
                currentEventId = "none";
            }
        });
    }

    private void shareEvent() {
        if (currentEventId.equals("none")) return;
        String msg = "📅 *Event:* " + tvEventTitle.getText() + "\n*Date:* " + selectedDate + "\n" + tvEventDesc.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void setCalendarReminder() {
        if (currentEventId.equals("none")) {
            Toast.makeText(this, "Select an event first", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(mYear, mMonth, mDay, 9, 0); // Default 9 AM

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, tvEventTitle.getText().toString())
                .putExtra(CalendarContract.Events.DESCRIPTION, tvEventDesc.getText().toString())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "Pune")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        startActivity(intent);
    }

    private void submitFeedback() {
        String comment = etComment.getText().toString().trim();
        if (comment.isEmpty()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", FirebaseAuth.getInstance().getUid());
        data.put("eventId", currentEventId);
        data.put("comment", comment);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("event_feedback").add(data).addOnSuccessListener(ref -> {
            Toast.makeText(this, "Feedback shared!", Toast.LENGTH_SHORT).show();
            etComment.setText("");
        });
    }
}
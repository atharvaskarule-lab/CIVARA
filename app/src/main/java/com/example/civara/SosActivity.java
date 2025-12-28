package com.example.civara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SosActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private TextView tvSosStatus;
    private CardView btnTriggerSos;
    private Button btnSafeNow;

    // CHANGE THIS: Add a real 10-digit number here
    private final String EMERGENCY_NUMBER = "9876543210";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tvSosStatus = findViewById(R.id.tvSosStatus);
        btnTriggerSos = findViewById(R.id.btnTriggerSos);
        btnSafeNow = findViewById(R.id.btnSafeNow);

        btnTriggerSos.setOnClickListener(v -> checkPermissionsAndStart());
        btnSafeNow.setOnClickListener(v -> stopEmergency());
    }

    private void checkPermissionsAndStart() {
        // Updated: Always request FINE and COARSE together for Android 12+
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 101);
        } else {
            startEmergency();
        }
    }

    private void startEmergency() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    String mapLink = "https://www.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                    broadcastAlert(mapLink);

                    // NEW: Fetch all users and send SMS to everyone
                    sendSmsToAllUsers(mapLink);
                }
            });
        }
    }

    private void sendSmsToAllUsers(String mapLink) {
        String message = "🚨 CIVARA EMERGENCY! Someone needs help at: " + mapLink;

        // 1. Get all documents from the "users" collection
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {

            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }

            // 2. Loop through every user found in the database
            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                String phoneNumber = doc.getString("phone"); // Ensure "phone" matches your Firestore field name

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    try {
                        // Send multipart because links are long
                        ArrayList<String> parts = smsManager.divideMessage(message);
                        smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
                    } catch (Exception e) {
                        // Log error for specific number if it fails
                        e.printStackTrace();
                    }
                }
            }
            Toast.makeText(this, "SOS SMS Broadcasted to all registered users", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch user list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void broadcastAlert(String link) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> alert = new HashMap<>();
        alert.put("senderId", uid);
        alert.put("mapLink", link);
        alert.put("timestamp", System.currentTimeMillis());

        db.collection("active_alerts").document(uid).set(alert)
                .addOnSuccessListener(aVoid -> {
                    tvSosStatus.setText("ALERT ACTIVE");
                    tvSosStatus.setTextColor(android.graphics.Color.RED);
                });
    }

    private void stopEmergency() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("active_alerts").document(uid).delete().addOnSuccessListener(aVoid -> {
            tvSosStatus.setText("Status: Secure");
            tvSosStatus.setTextColor(android.graphics.Color.BLACK);
            Toast.makeText(this, "Emergency Cleared", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startEmergency();
        } else {
            Toast.makeText(this, "Permissions required for SOS", Toast.LENGTH_SHORT).show();
        }
    }
}
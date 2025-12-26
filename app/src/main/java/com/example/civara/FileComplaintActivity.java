package com.example.civara;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class FileComplaintActivity extends AppCompatActivity {

    EditText etDescription;
    CheckBox cbGarbage, cbRoadDamage, cbWaterSupply;
    Button btnUploadImage, btnSubmitComplaint;
    Uri imageUri;
    FusedLocationProviderClient fusedLocationClient;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_complaint);

        etDescription = findViewById(R.id.etDescription);
        cbGarbage = findViewById(R.id.cbGarbage);
        cbRoadDamage = findViewById(R.id.cbRoadDamage);
        cbWaterSupply = findViewById(R.id.cbWaterSupply);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnSubmitComplaint = findViewById(R.id.btnSubmitComplaint);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), 100);
        });

        btnSubmitComplaint.setOnClickListener(v -> submitComplaint());

        getLocation();
    }

    private void getLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if(location != null){
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void submitComplaint() {
        String description = etDescription.getText().toString();
        String type = "";
        if(cbGarbage.isChecked()) type += "Garbage ";
        if(cbRoadDamage.isChecked()) type += "Road Damage ";
        if(cbWaterSupply.isChecked()) type += "Water Supply ";

        if(description.isEmpty() || type.isEmpty()){
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save complaint to DB (Firebase Firestore / Realtime DB or SQLite)
        Toast.makeText(this, "Complaint Submitted!\nLat: " + latitude + " Lon: " + longitude, Toast.LENGTH_SHORT).show();

        etDescription.setText("");
        cbGarbage.setChecked(false);
        cbRoadDamage.setChecked(false);
        cbWaterSupply.setChecked(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            Toast.makeText(this, "Image Selected", Toast.LENGTH_SHORT).show();
        }
    }
}

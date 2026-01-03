package com.example.civara;

public class Complaint {
    private String documentId;
    private String title;
    private String description;
    private String status;
    private String date;
    private String name;
    private String imageUrl; // ADDED: To store the Base64 image string
    private double latitude;  // ADDED: For location support
    private double longitude; // ADDED: For location support

    public Complaint() {} // Required for Firestore

    // GETTERS AND SETTERS FOR IMAGES (Fixes the Adapter Error)
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // GETTERS AND SETTERS FOR LOCATION
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    // EXISTING GETTERS AND SETTERS
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}
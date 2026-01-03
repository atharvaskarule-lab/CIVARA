package com.example.civara;

public class Complaint {
    private String documentId;
    private String title;
    private String description;
    private String status;
    private String date;
    private String userId;
    private long timestamp;

    // Required empty constructor for Firebase
    public Complaint() {}

    // Constructor for manual creation
    public Complaint(String documentId, String title, String description, String status, String date) {
        this.documentId = documentId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.date = date;
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
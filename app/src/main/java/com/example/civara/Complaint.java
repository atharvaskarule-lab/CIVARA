package com.example.civara;

public class Complaint {
    private String documentId;
    private String title;
    private String description;
    private String status;
    private String date;

    // Required empty constructor for Firebase
    public Complaint() {}

    // Multi-argument constructor to fix the "Expected no arguments but found 5" error
    public Complaint(String documentId, String title, String description, String status, String date) {
        this.documentId = documentId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.date = date;
    }

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
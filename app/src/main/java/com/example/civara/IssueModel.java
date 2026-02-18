package com.example.civara;

public class IssueModel {
    private String title;
    private String description;
    private String category;
    private String status;
    private String documentId; // Firestore document ID store karne ke liye

    // Required empty constructor for Firebase
    public IssueModel() {}

    public IssueModel(String title, String description, String category, String status, String documentId) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.documentId = documentId;
    }

    // documentId ke liye Getter aur Setter
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Baki Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
package com.example.civara;

public class IssueModel {
    private String title;
    private String description;
    private String category;
    private String status;
    private String documentId;
    private String email; // Admin panel ke liye
    private String name;  // History aur Display ke liye

    // Required empty constructor for Firebase
    public IssueModel() {}

    // Updated Constructor (Sari fields ke saath)
    public IssueModel(String title, String description, String category, String status, String documentId, String email, String name) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.documentId = documentId;
        this.email = email;
        this.name = name;
    }

    // Getters
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getDocumentId() { return documentId; }
    public String getCategory() { return category; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setCategory(String category) { this.category = category; }
}
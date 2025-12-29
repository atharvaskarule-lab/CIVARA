package com.example.civara;

public class Complaint {
    private String id;
    private String type;
    private String description;
    private String status;
    private String date;

    // Required empty constructor for Firebase
    public Complaint() {}

    public Complaint(String id, String type, String description, String status, String date) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.status = status;
        this.date = date;
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getDate() { return date; }

    // Setters (Optional, but good for Firebase)
    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setDate(String date) { this.date = date; }
}
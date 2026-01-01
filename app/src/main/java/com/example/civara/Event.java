package com.example.civara;

public class Event {
    private String title;
    private String description;
    private String date;
    private String location;
    private long createdAt;

    // Required empty constructor for Firebase
    public Event() {}

    public Event(String title, String description, String date, String location) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
    }

    // Getters match what the Adapter expects
    public String getEventTitle() { return title; }
    public String getEventDescription() { return description; }
    public String getEventDate() { return date; }

    // Standard Getters/Setters for Firebase
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
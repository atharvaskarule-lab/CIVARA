package com.example.civara;

public class NotificationItem {
    private String title, message, timestamp;

    public NotificationItem() {} // For Firestore

    public NotificationItem(String title, String message, String timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}
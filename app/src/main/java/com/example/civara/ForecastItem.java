package com.example.civara;

public class ForecastItem {
    private String date;
    private String icon;    private String temperature;

    // Constructor
    public ForecastItem(String date, String icon, String temperature) {
        this.date = date;
        this.icon = icon;
        this.temperature = temperature;
    }

    // Getters
    public String getDate() {
        return date;
    }

    public String getIcon() {
        return icon;
    }

    public String getTemperature() {
        return temperature;
    }
}

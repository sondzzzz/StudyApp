package com.example.myapplication.models;

public class AvailableCourse {
    private String name;
    private int totalLessons;
    private int iconResId;

    public AvailableCourse(String name, int totalLessons, int iconResId) {
        this.name = name;
        this.totalLessons = totalLessons;
        this.iconResId = iconResId;
    }

    public String getName() { return name; }
    public int getTotalLessons() { return totalLessons; }
    public int getIconResId() { return iconResId; }
}

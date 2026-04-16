package com.example.myapplication.models;

import java.io.Serializable;

public class CourseProgress implements Serializable {
    private int id;
    private int userId; 
    private String courseName;
    private int currentLesson;
    private int totalLessons;
    private int percentComplete;
    private int iconResId;
    private long lastAccessed;


    public CourseProgress() {
    }

    public CourseProgress(int userId, String courseName, int currentLesson, int totalLessons, int percentComplete, int iconResId) {
        this.userId = userId;
        this.courseName = courseName;
        this.currentLesson = currentLesson;
        this.totalLessons = totalLessons;
        this.percentComplete = percentComplete;
        this.iconResId = iconResId;
        this.lastAccessed = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public int getCurrentLesson() { return currentLesson; }
    public void setCurrentLesson(int currentLesson) { this.currentLesson = currentLesson; }
    public int getTotalLessons() { return totalLessons; }
    public void setTotalLessons(int totalLessons) { this.totalLessons = totalLessons; }
    public int getPercentComplete() { return percentComplete; }
    public void setPercentComplete(int percentComplete) { this.percentComplete = percentComplete; }
    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public long getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(long lastAccessed) { this.lastAccessed = lastAccessed; }
}

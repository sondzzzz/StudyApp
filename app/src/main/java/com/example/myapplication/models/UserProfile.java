package com.example.myapplication.models;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private int id;
    private String name;
    private int streak;
    private int currentXp;
    private int maxXp;
    private int level;

    public UserProfile() {}

    public UserProfile(String name, int streak, int currentXp, int maxXp, int level) {
        this.name = name;
        this.streak = streak;
        this.currentXp = currentXp;
        this.maxXp = maxXp;
        this.level = level;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }
    public int getCurrentXp() { return currentXp; }
    public void setCurrentXp(int currentXp) { this.currentXp = currentXp; }
    public int getMaxXp() { return maxXp; }
    public void setMaxXp(int maxXp) { this.maxXp = maxXp; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}

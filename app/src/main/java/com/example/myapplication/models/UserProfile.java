package com.example.myapplication.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfile {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int streak;
    private int currentXp;
    private int maxXp;
    private int level;

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
    public int getStreak() { return streak; }
    public int getCurrentXp() { return currentXp; }
    public int getMaxXp() { return maxXp; }
    public int getLevel() { return level; }
}

package com.example.myapplication.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lessons")
public class Lesson {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String courseName;
    private int lessonNumber;
    private String title;
    private String content;

    public Lesson(String courseName, int lessonNumber, String title, String content) {
        this.courseName = courseName;
        this.lessonNumber = lessonNumber;
        this.title = title;
        this.content = content;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public int getLessonNumber() { return lessonNumber; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}

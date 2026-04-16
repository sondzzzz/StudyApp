package com.example.myapplication.models;

import java.io.Serializable;

public class Lesson implements Serializable {
    private String courseName;
    private int lessonNumber;
    private String title;
    private String content;

    // Required for Firebase
    public Lesson() {}

    public Lesson(String courseName, int lessonNumber, String title, String content) {
        this.courseName = courseName;
        this.lessonNumber = lessonNumber;
        this.title = title;
        this.content = content;
    }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public int getLessonNumber() { return lessonNumber; }
    public void setLessonNumber(int lessonNumber) { this.lessonNumber = lessonNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

package com.example.myapplication.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_questions")
public class QuizQuestion {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String courseName;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int correctAnswer; // 1 for A, 2 for B, 3 for C, 4 for D

    public QuizQuestion(String courseName, String question, String optionA, String optionB, String optionC, String optionD, int correctAnswer) {
        this.courseName = courseName;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public String getQuestion() { return question; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public int getCorrectAnswer() { return correctAnswer; }
}

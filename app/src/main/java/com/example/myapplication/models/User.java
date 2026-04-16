package com.example.myapplication.models;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private int streak;
    private int currentXp;
    private int maxXp;
    private int level;
    private int targetLessons = 5;
    private int targetFlashcards = 20;
    private int targetQuiz = 3;
    private int targetTime = 0;
    private int lessonsDoneToday = 0;
    private int flashcardsDoneToday = 0;
    private int quizDoneToday = 0;
    private String lastActiveDate;

    public User() {}

    public User(String username, String password, String fullName, int streak, int currentXp, int maxXp, int level) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.streak = streak;
        this.currentXp = currentXp;
        this.maxXp = maxXp;
        this.level = level;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }
    public int getCurrentXp() { return currentXp; }
    public void setCurrentXp(int currentXp) { this.currentXp = currentXp; }
    public int getMaxXp() { return maxXp; }
    public void setMaxXp(int maxXp) { this.maxXp = maxXp; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getTargetLessons() { return targetLessons; }
    public void setTargetLessons(int targetLessons) { this.targetLessons = targetLessons; }
    public int getTargetFlashcards() { return targetFlashcards; }
    public void setTargetFlashcards(int targetFlashcards) { this.targetFlashcards = targetFlashcards; }
    public int getTargetQuiz() { return targetQuiz; }
    public void setTargetQuiz(int targetQuiz) { this.targetQuiz = targetQuiz; }

    public int getTargetTime() { return targetTime; }
    public void setTargetTime(int targetTime) { this.targetTime = targetTime; }

    public int getLessonsDoneToday() { return lessonsDoneToday; }
    public void setLessonsDoneToday(int lessonsDoneToday) { this.lessonsDoneToday = lessonsDoneToday; }
    public int getFlashcardsDoneToday() { return flashcardsDoneToday; }
    public void setFlashcardsDoneToday(int flashcardsDoneToday) { this.flashcardsDoneToday = flashcardsDoneToday; }
    public int getQuizDoneToday() { return quizDoneToday; }
    public void setQuizDoneToday(int quizDoneToday) { this.quizDoneToday = quizDoneToday; }
    public String getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(String lastActiveDate) { this.lastActiveDate = lastActiveDate; }
}

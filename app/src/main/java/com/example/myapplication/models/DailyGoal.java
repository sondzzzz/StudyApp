package com.example.myapplication.models;

public class DailyGoal {
    private int lessonsDone;
    private int lessonsTarget;
    private int flashcardsDone;
    private int flashcardsTarget;
    private int quizDone;
    private int quizTarget;
    private int minutesDone;
    private int minutesTarget;

    public DailyGoal(int lessonsDone, int lessonsTarget, int flashcardsDone, int flashcardsTarget, int quizDone, int quizTarget, int minutesDone, int minutesTarget) {
        this.lessonsDone = lessonsDone;
        this.lessonsTarget = lessonsTarget;
        this.flashcardsDone = flashcardsDone;
        this.flashcardsTarget = flashcardsTarget;
        this.quizDone = quizDone;
        this.quizTarget = quizTarget;
        this.minutesDone = minutesDone;
        this.minutesTarget = minutesTarget;
    }

    public int getLessonsDone() { return lessonsDone; }
    public int getLessonsTarget() { return lessonsTarget; }
    public int getFlashcardsDone() { return flashcardsDone; }
    public int getFlashcardsTarget() { return flashcardsTarget; }
    public int getQuizDone() { return quizDone; }
    public int getQuizTarget() { return quizTarget; }
    public int getMinutesDone() { return minutesDone; }
    public int getMinutesTarget() { return minutesTarget; }
}

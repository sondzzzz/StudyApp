package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.myapplication.models.User;
import com.example.myapplication.models.CourseProgress;
import com.example.myapplication.models.QuizQuestion;
import com.example.myapplication.models.Lesson;
import java.util.List;

@Dao
public interface AppDao {
    // User operations
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    @Insert
    long insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    // Course operations
    @Query("SELECT * FROM course_progress WHERE userId = :userId ORDER BY lastAccessed DESC")
    List<CourseProgress> getCoursesByUserId(int userId);

    @Query("SELECT * FROM course_progress WHERE userId = :userId AND courseName = :courseName LIMIT 1")
    CourseProgress getCourseProgress(int userId, String courseName);

    @Insert
    void insertCourse(CourseProgress course);

    @Update
    void updateCourse(CourseProgress course);

    // Quiz operations
    @Query("SELECT * FROM quiz_questions WHERE courseName = :courseName")
    List<QuizQuestion> getQuestionsByCourse(String courseName);

    @Insert
    void insertQuestion(QuizQuestion question);

    // Lesson operations
    @Query("SELECT * FROM lessons WHERE courseName = :courseName ORDER BY lessonNumber ASC")
    List<Lesson> getLessonsByCourse(String courseName);

    @Query("SELECT * FROM lessons WHERE courseName = :courseName AND lessonNumber = :lessonNumber LIMIT 1")
    Lesson getLessonByNumber(String courseName, int lessonNumber);

    @Insert
    void insertLesson(Lesson lesson);
}

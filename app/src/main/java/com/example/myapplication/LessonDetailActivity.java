package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.CourseProgress;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.models.User;

public class LessonDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvContent;
    private Button btnNext;
    private String courseName;
    private int lessonNumber;
    private User currentUser;
    private CourseProgress currentCourseProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);

        courseName = getIntent().getStringExtra("course_name");
        lessonNumber = getIntent().getIntExtra("lesson_number", 1);

        tvTitle = findViewById(R.id.tv_lesson_title);
        tvContent = findViewById(R.id.tv_lesson_content);
        btnNext = findViewById(R.id.btn_next_lesson);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        loadLessonData();
    }

    private void loadLessonData() {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Lesson lesson = db.appDao().getLessonByNumber(courseName, lessonNumber);
            int userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);
            currentUser = db.appDao().getUserById(userId);
            currentCourseProgress = db.appDao().getCourseProgress(userId, courseName);

            runOnUiThread(() -> {
                if (lesson != null) {
                    tvTitle.setText(lesson.getTitle());
                    tvContent.setText(lesson.getContent());
                    getSupportActionBar().setTitle("Bài " + lesson.getLessonNumber());
                }
            });
        });

        btnNext.setOnClickListener(v -> completeLesson());
    }

    private void completeLesson() {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Cập nhật XP và Mục tiêu trong User
            if (currentUser != null) {
                currentUser.setCurrentXp(currentUser.getCurrentXp() + 10);
                currentUser.setLessonsDoneToday(currentUser.getLessonsDoneToday() + 1);
                
                // Kiểm tra lên cấp (ví dụ đơn giản)
                if (currentUser.getCurrentXp() >= currentUser.getMaxXp()) {
                    currentUser.setLevel(currentUser.getLevel() + 1);
                    currentUser.setCurrentXp(0);
                    currentUser.setMaxXp(currentUser.getMaxXp() + 500);
                }
                db.appDao().updateUser(currentUser);
            }

            // 2. Cập nhật tiến độ khóa học
            if (currentCourseProgress != null) {
                if (lessonNumber >= currentCourseProgress.getCurrentLesson()) {
                    currentCourseProgress.setCurrentLesson(lessonNumber + 1);
                    int progress = (int) (((float) lessonNumber / currentCourseProgress.getTotalLessons()) * 100);
                    currentCourseProgress.setPercentComplete(Math.min(progress, 100));
                    db.appDao().updateCourse(currentCourseProgress);
                }
            }

            // 3. Tìm bài tiếp theo
            Lesson nextLesson = db.appDao().getLessonByNumber(courseName, lessonNumber + 1);
            runOnUiThread(() -> {
                Toast.makeText(this, "+10 XP! Đã cập nhật mục tiêu.", Toast.LENGTH_SHORT).show();
                if (nextLesson != null) {
                    lessonNumber++;
                    loadLessonData();
                } else {
                    Toast.makeText(this, "Chúc mừng! Bạn đã hoàn thành khóa học.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        });
    }
}

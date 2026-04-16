package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myapplication.models.Lesson;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LessonDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvContent;
    private Button btnNext;
    private String courseName;
    private int lessonNumber;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
        String safeCourseName = courseName.replace("/", "_");
        String docId = safeCourseName + "_L" + lessonNumber;

        db.collection("lessons").document(docId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Lesson lesson = documentSnapshot.toObject(Lesson.class);
                    if (lesson != null) {
                        tvTitle.setText(lesson.getTitle());
                        tvContent.setText(lesson.getContent());
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Bài " + lesson.getLessonNumber());
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy dữ liệu bài học!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải bài học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        btnNext.setOnClickListener(v -> completeLessonOnFirebase());
    }

    private void completeLessonOnFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // 1. Cập nhật User (XP và Mục tiêu ngày)
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                long currentXp = doc.getLong("currentXp");
                long maxXp = doc.getLong("maxXp");
                long level = doc.getLong("level");
                long lessonsDoneToday = doc.contains("lessonsDoneToday") ? doc.getLong("lessonsDoneToday") : 0;

                long newXp = currentXp + 10;
                long newLessonsDone = lessonsDoneToday + 1;
                long newLevel = level;
                long newMaxXp = maxXp;

                if (newXp >= maxXp) {
                    newLevel++;
                    newXp = 0;
                    newMaxXp += 500;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("currentXp", newXp);
                updates.put("lessonsDoneToday", newLessonsDone);
                updates.put("level", newLevel);
                updates.put("maxXp", newMaxXp);

                db.collection("users").document(uid).update(updates);
            }
        });

        // 2. Cập nhật Tiến độ khóa học
        db.collection("users").document(uid).collection("course_progress")
            .whereEqualTo("courseName", courseName)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot courseDoc = queryDocumentSnapshots.getDocuments().get(0);
                    long totalLessons = courseDoc.getLong("totalLessons");
                    
                    int nextLessonNum = lessonNumber + 1;
                    int progress = (int) (((float) lessonNumber / totalLessons) * 100);

                    Map<String, Object> courseUpdate = new HashMap<>();
                    courseUpdate.put("currentLesson", nextLessonNum);
                    courseUpdate.put("percentComplete", Math.min(progress, 100));
                    courseUpdate.put("lastAccessed", System.currentTimeMillis());

                    db.collection("users").document(uid).collection("course_progress")
                        .document(courseDoc.getId()).update(courseUpdate);
                }
            });

        // 3. Tìm bài tiếp theo trong Firebase
        db.collection("lessons")
                .whereEqualTo("courseName", courseName)
                .whereEqualTo("lessonNumber", lessonNumber + 1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        lessonNumber++;
                        loadLessonData();
                        Toast.makeText(this, "+10 XP! Đã cập nhật mục tiêu ngày.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Chúc mừng! Bạn đã hoàn thành khóa học.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }
}

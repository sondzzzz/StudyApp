package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.CourseProgress;
import com.example.myapplication.models.QuizQuestion;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizFragment extends Fragment {

    private View layoutCourseList, layoutQuizQuestion, layoutQuizResult;
    private RecyclerView rvQuizCourses;
    private ProgressBar progressBar;
    private TextView tvQuestionCount, tvQuestionText;
    private MaterialButton[] options = new MaterialButton[4];
    private MaterialButton btnCheck;

    // Result UI
    private TextView tvResultTitle, tvResultScore, tvResultXp, tvResultStatus;
    private ImageView ivResultIcon;
    private MaterialButton btnFinishQuiz;

    private List<QuizQuestion> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String currentCourseName = "";
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        layoutCourseList = view.findViewById(R.id.layout_course_list);
        layoutQuizQuestion = view.findViewById(R.id.layout_quiz_question);
        layoutQuizResult = view.findViewById(R.id.layout_quiz_result);
        rvQuizCourses = view.findViewById(R.id.rv_quiz_courses);

        progressBar = view.findViewById(R.id.quiz_progress);
        tvQuestionCount = view.findViewById(R.id.tv_question_count);
        tvQuestionText = view.findViewById(R.id.tv_question_text);
        options[0] = view.findViewById(R.id.btn_option_1);
        options[1] = view.findViewById(R.id.btn_option_2);
        options[2] = view.findViewById(R.id.btn_option_3);
        options[3] = view.findViewById(R.id.btn_option_4);
        btnCheck = view.findViewById(R.id.btn_next_question);

        tvResultTitle = view.findViewById(R.id.tv_result_title);
        tvResultScore = view.findViewById(R.id.tv_result_score);
        tvResultXp = view.findViewById(R.id.tv_result_xp);
        tvResultStatus = view.findViewById(R.id.tv_result_status);
        ivResultIcon = view.findViewById(R.id.iv_result_icon);
        btnFinishQuiz = view.findViewById(R.id.btn_finish_quiz);

        rvQuizCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        loadEnrolledCoursesFromFirebase();

        btnFinishQuiz.setOnClickListener(v -> {
            layoutQuizResult.setVisibility(View.GONE);
            layoutCourseList.setVisibility(View.VISIBLE);
            loadEnrolledCoursesFromFirebase();
        });

        return view;
    }

    private void loadEnrolledCoursesFromFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
            .collection("course_progress")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<CourseProgress> courses = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    CourseProgress cp = new CourseProgress(
                            0,
                            doc.getString("courseName"),
                            doc.getLong("currentLesson").intValue(),
                            doc.getLong("totalLessons").intValue(),
                            doc.getLong("percentComplete").intValue(),
                            doc.getLong("iconResId").intValue()
                    );
                    courses.add(cp);
                }
                if (isAdded()) {
                    QuizCourseAdapter adapter = new QuizCourseAdapter(courses, course -> startQuiz(course.getCourseName()));
                    rvQuizCourses.setAdapter(adapter);
                }
            });
    }

    private void startQuiz(String courseName) {
        currentCourseName = courseName;
        currentQuestionIndex = 0;
        score = 0;

        db.collection("quiz_questions")
                .whereEqualTo("courseName", courseName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    questionList = queryDocumentSnapshots.toObjects(QuizQuestion.class);
                    if (!questionList.isEmpty()) {
                        layoutCourseList.setVisibility(View.GONE);
                        layoutQuizResult.setVisibility(View.GONE);
                        layoutQuizQuestion.setVisibility(View.VISIBLE);
                        displayQuestion();
                    } else {
                        Toast.makeText(getContext(), "Khóa học này chưa có câu hỏi Quiz!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải câu hỏi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayQuestion() {
        QuizQuestion q = questionList.get(currentQuestionIndex);
        tvQuestionCount.setText("Câu hỏi " + (currentQuestionIndex + 1) + "/" + questionList.size());
        tvQuestionText.setText(q.getQuestion());
        options[0].setText(q.getOptionA());
        options[1].setText(q.getOptionB());
        options[2].setText(q.getOptionC());
        options[3].setText(q.getOptionD());

        progressBar.setMax(questionList.size());
        progressBar.setProgress(currentQuestionIndex + 1);

        resetButtons();

        for (int i = 0; i < 4; i++) {
            int index = i + 1;
            options[i].setOnClickListener(v -> checkAnswer(index));
        }
    }

    private void resetButtons() {
        for (MaterialButton btn : options) {
            btn.setEnabled(true);
            btn.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            btn.setBackgroundColor(Color.TRANSPARENT);
        }
        btnCheck.setVisibility(View.GONE);
    }

    private void checkAnswer(int selectedOption) {
        QuizQuestion q = questionList.get(currentQuestionIndex);
        for (MaterialButton btn : options) btn.setEnabled(false);

        if (selectedOption == q.getCorrectAnswer()) {
            score++;
            options[selectedOption - 1].setStrokeColor(android.content.res.ColorStateList.valueOf(Color.GREEN));
        } else {
            options[selectedOption - 1].setStrokeColor(android.content.res.ColorStateList.valueOf(Color.RED));
            options[q.getCorrectAnswer() - 1].setStrokeColor(android.content.res.ColorStateList.valueOf(Color.GREEN));
        }

        btnCheck.setVisibility(View.VISIBLE);
        btnCheck.setText(currentQuestionIndex < questionList.size() - 1 ? "Câu tiếp theo" : "Xem kết quả");
        btnCheck.setOnClickListener(v -> {
            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                displayQuestion();
            } else {
                showResult();
            }
        });
    }

    private void showResult() {
        layoutQuizQuestion.setVisibility(View.GONE);
        layoutQuizResult.setVisibility(View.VISIBLE);

        int totalQuestions = questionList.size();
        int requiredScore = totalQuestions / 2;
        boolean isPassed = score >= requiredScore;
        int xpEarned = isPassed ? score * 10 : 0;

        tvResultScore.setText("Bạn đúng " + score + "/" + totalQuestions + " câu");
        tvResultXp.setText("+" + xpEarned + " XP");

        if (isPassed) {
            tvResultTitle.setText("Chúc mừng!");
            tvResultStatus.setText("ĐẠT");
            tvResultStatus.setTextColor(Color.parseColor("#4CAF50"));
            ivResultIcon.setImageResource(android.R.drawable.btn_star_big_on);
            updateUserFirebaseAfterQuiz(xpEarned);
        } else {
            tvResultTitle.setText("Tiếc quá!");
            tvResultStatus.setText("CHƯA ĐẠT");
            tvResultStatus.setTextColor(Color.RED);
            ivResultIcon.setImageResource(android.R.drawable.ic_delete);
        }
    }

    private void updateUserFirebaseAfterQuiz(int xpToAdd) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // 1. Cập nhật User
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                long currentXp = doc.getLong("currentXp");
                long maxXp = doc.getLong("maxXp");
                long level = doc.getLong("level");
                long quizDoneToday = doc.contains("quizDoneToday") ? doc.getLong("quizDoneToday") : 0;

                long newXp = currentXp + xpToAdd;
                long newQuizDone = quizDoneToday + 1;
                long newLevel = level;
                long newMaxXp = maxXp;

                if (newXp >= maxXp) {
                    newLevel++;
                    newXp = 0;
                    newMaxXp += 500;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("currentXp", newXp);
                updates.put("quizDoneToday", newQuizDone);
                updates.put("level", newLevel);
                updates.put("maxXp", newMaxXp);

                db.collection("users").document(uid).update(updates);
            }
        });

        // 2. Cập nhật Tiến độ khóa học
        db.collection("users").document(uid).collection("course_progress")
            .whereEqualTo("courseName", currentCourseName)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot courseDoc = queryDocumentSnapshots.getDocuments().get(0);
                    long currentLesson = courseDoc.getLong("currentLesson");
                    long totalLessons = courseDoc.getLong("totalLessons");
                    
                    long nextLesson = currentLesson + 1;
                    int percent = (int) ((nextLesson * 100) / totalLessons);

                    Map<String, Object> courseUpdate = new HashMap<>();
                    courseUpdate.put("currentLesson", nextLesson);
                    courseUpdate.put("percentComplete", Math.min(percent, 100));
                    courseUpdate.put("lastAccessed", System.currentTimeMillis());

                    db.collection("users").document(uid).collection("course_progress")
                        .document(courseDoc.getId()).update(courseUpdate);
                }
            });
    }

    private class QuizCourseAdapter extends RecyclerView.Adapter<QuizCourseAdapter.ViewHolder> {
        private List<CourseProgress> courses;
        private OnCourseClickListener listener;

        public QuizCourseAdapter(List<CourseProgress> courses, OnCourseClickListener listener) {
            this.courses = courses;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_course, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CourseProgress course = courses.get(position);
            holder.name.setText(course.getCourseName());
            holder.icon.setImageResource(course.getIconResId());
            holder.itemView.setOnClickListener(v -> listener.onCourseClick(course));
        }

        @Override
        public int getItemCount() {
            return courses.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            ImageView icon;
            ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tv_course_name);
                icon = itemView.findViewById(R.id.iv_course_icon);
            }
        }
    }

    interface OnCourseClickListener {
        void onCourseClick(CourseProgress course);
    }
}

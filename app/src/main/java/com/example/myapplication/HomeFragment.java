package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.CourseProgress;
import com.example.myapplication.models.DailyGoal;
import com.example.myapplication.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvUserName, tvStreakCount, tvXpInfo, tvLevelInfo;
    private TextView tvGoalLessons, tvGoalFlashcards, tvGoalQuiz, tvGoalTime;
    private View cardLessons, cardFlashcards, cardQuiz, cardTime;
    private ProgressBar xpProgressBar;
    private RecyclerView rvCourses;
    private CourseAdapter courseAdapter;
    
    private User currentUser;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initViews(view);
        loadDataFromFirebase();
        return view;
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvStreakCount = view.findViewById(R.id.tv_streak_count);
        tvXpInfo = view.findViewById(R.id.tv_xp_info);
        tvLevelInfo = view.findViewById(R.id.tv_level_info);
        xpProgressBar = view.findViewById(R.id.xp_progress_bar);

        tvGoalLessons = view.findViewById(R.id.tv_goal_lessons);
        tvGoalFlashcards = view.findViewById(R.id.tv_goal_flashcards);
        tvGoalQuiz = view.findViewById(R.id.tv_goal_quiz);
        tvGoalTime = view.findViewById(R.id.tv_goal_time);

        cardLessons = (View) tvGoalLessons.getParent().getParent();
        cardFlashcards = (View) tvGoalFlashcards.getParent().getParent();
        cardQuiz = (View) tvGoalQuiz.getParent().getParent();
        cardTime = (View) tvGoalTime.getParent().getParent();

        rvCourses = view.findViewById(R.id.rv_courses);
        rvCourses.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        
        setupGoalClickListeners();
    }

    private void setupGoalClickListeners() {
        cardLessons.setOnClickListener(v -> {
            if (currentUser == null) return;
            showEditGoalDialog(getString(R.string.lessons), currentUser.getTargetLessons(), newGoal -> {
                currentUser.setTargetLessons(newGoal);
                updateUserInFirebase("targetLessons", newGoal);
            });
        });

        cardFlashcards.setOnClickListener(v -> {
            if (currentUser == null) return;
            showEditGoalDialog(getString(R.string.flashcards), currentUser.getTargetFlashcards(), newGoal -> {
                currentUser.setTargetFlashcards(newGoal);
                updateUserInFirebase("targetFlashcards", newGoal);
            });
        });

        cardQuiz.setOnClickListener(v -> {
            if (currentUser == null) return;
            showEditGoalDialog(getString(R.string.quiz), currentUser.getTargetQuiz(), newGoal -> {
                currentUser.setTargetQuiz(newGoal);
                updateUserInFirebase("targetQuiz", newGoal);
            });
        });

        cardTime.setOnClickListener(v -> {
            if (currentUser == null) return;
            showEditGoalDialog(getString(R.string.time), currentUser.getTargetTime(), newGoal -> {
                currentUser.setTargetTime(newGoal);
                updateUserInFirebase("targetTime", newGoal);
            });
        });
    }

    private void updateUserInFirebase(String field, int value) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                .update(field, value)
                .addOnSuccessListener(aVoid -> loadDataFromFirebase())
                .addOnFailureListener(e -> {
                        // Nếu lỗi do kết nối Firebase (ví dụ: UNAVAILABLE)
                        Intent intent = new Intent(getContext(), NoInternetActivity.class);
                        startActivity(intent);
                });;
        }
    }

    private void showEditGoalDialog(String title, int currentTarget, OnGoalUpdateListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_goal, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvDescription = dialogView.findViewById(R.id.tv_dialog_description);
        EditText input = dialogView.findViewById(R.id.et_goal_value);

        tvTitle.setText(getString(R.string.edit_goal_title, title));
        tvDescription.setText(getString(R.string.edit_goal_description, title));
        input.setText(String.valueOf(currentTarget));
        input.setSelection(input.getText().length());

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                listener.onGoalUpdate(Integer.parseInt(val));
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.purple_primary));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.text_color_secondary));
    }

    private void loadDataFromFirebase() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;

        db.collection("users").document(firebaseUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUser = mapDocumentToUser(documentSnapshot);
                    
                    DailyGoal userGoals = new DailyGoal(
                            currentUser.getLessonsDoneToday(), currentUser.getTargetLessons(), 
                            currentUser.getFlashcardsDoneToday(), currentUser.getTargetFlashcards(), 
                            currentUser.getQuizDoneToday(), currentUser.getTargetQuiz(),
                            0, 
                            currentUser.getTargetTime());
                            
                    bindData(currentUser, userGoals);
                    loadCoursesFromFirebase(firebaseUser.getUid());
                }
            });
    }

    private void loadCoursesFromFirebase(String userId) {
        db.collection("users").document(userId).collection("course_progress")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<CourseProgress> courses = queryDocumentSnapshots.toObjects(CourseProgress.class);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        courseAdapter = new CourseAdapter(courses);
                        rvCourses.setAdapter(courseAdapter);
                    });
                }
            });
    }



    private User mapDocumentToUser(DocumentSnapshot doc) {
        User user = new User("", "", doc.getString("fullName"), 
                doc.getLong("streak").intValue(), 
                doc.getLong("currentXp").intValue(), 
                doc.getLong("maxXp").intValue(), 
                doc.getLong("level").intValue());
        
        user.setTargetLessons(doc.getLong("targetLessons").intValue());
        user.setTargetFlashcards(doc.getLong("targetFlashcards").intValue());
        user.setTargetQuiz(doc.getLong("targetQuiz").intValue());
        user.setTargetTime(doc.getLong("targetTime").intValue());
        
        // Cần đảm bảo các field này có trong Firestore hoặc xử lý null
        if (doc.contains("lessonsDoneToday")) user.setLessonsDoneToday(doc.getLong("lessonsDoneToday").intValue());
        if (doc.contains("flashcardsDoneToday")) user.setFlashcardsDoneToday(doc.getLong("flashcardsDoneToday").intValue());
        if (doc.contains("quizDoneToday")) user.setQuizDoneToday(doc.getLong("quizDoneToday").intValue());
        
        return user;
    }

    private void bindData(User user, DailyGoal goals) {
        if (!isAdded()) return;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            tvGreeting.setText(R.string.good_morning);
        } else if (hour < 18) {
            tvGreeting.setText(R.string.good_afternoon);
        } else {
            tvGreeting.setText(R.string.good_evening);
        }

        if (user != null) {
            tvUserName.setText(user.getFullName());
            tvStreakCount.setText(getString(R.string.streak_format, user.getStreak()));
            tvXpInfo.setText(getString(R.string.xp_progress_format, user.getCurrentXp(), user.getMaxXp()));
            tvLevelInfo.setText(getString(R.string.level_format, user.getLevel()));
            xpProgressBar.setMax(user.getMaxXp());
            xpProgressBar.setProgress(user.getCurrentXp());
        }

        if (goals != null) {
            tvGoalLessons.setText(getString(R.string.goal_fraction_format, goals.getLessonsDone(), goals.getLessonsTarget()));
            tvGoalFlashcards.setText(getString(R.string.goal_fraction_format, goals.getFlashcardsDone(), goals.getFlashcardsTarget()));
            tvGoalQuiz.setText(getString(R.string.goal_fraction_format, goals.getQuizDone(), goals.getQuizTarget()));
            tvGoalTime.setText(getString(R.string.time_format, goals.getMinutesTarget()));
        }
    }

    interface OnGoalUpdateListener {
        void onGoalUpdate(int newGoal);
    }
}

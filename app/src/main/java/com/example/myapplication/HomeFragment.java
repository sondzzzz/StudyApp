package com.example.myapplication;

import android.content.Context;
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

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.CourseProgress;
import com.example.myapplication.models.DailyGoal;
import com.example.myapplication.models.User;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        loadDataFromDatabase();
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
                updateUserInDb();
            });
        });

        cardFlashcards.setOnClickListener(v -> {
            if (currentUser == null) return;
            showEditGoalDialog(getString(R.string.flashcards), currentUser.getTargetFlashcards(), newGoal -> {
                currentUser.setTargetFlashcards(newGoal);
                updateUserInDb();
            });
        });

        cardQuiz.setOnClickListener(v -> {
            if (currentUser == null) return;
            showEditGoalDialog(getString(R.string.quiz), currentUser.getTargetQuiz(), newGoal -> {
                currentUser.setTargetQuiz(newGoal);
                updateUserInDb();
            });
        });

        cardTime.setOnClickListener(v -> {
            if (currentUser == null) return;
            showEditGoalDialog(getString(R.string.time), currentUser.getTargetTime(), newGoal -> {
                currentUser.setTargetTime(newGoal);
                updateUserInDb();
            });
        });
    }

    private void updateUserInDb() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.appDao().updateUser(currentUser);
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::loadDataFromDatabase);
            }
        });
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
        
        // Tùy chỉnh màu nút sau khi show
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.purple_primary));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.text_color_secondary));
    }

    private void loadDataFromDatabase() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) return;

        AppDatabase db = AppDatabase.getInstance(requireContext());
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentUser = db.appDao().getUserById(userId);
            List<CourseProgress> allCourses = db.appDao().getCoursesByUserId(userId);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (currentUser != null) {
                        // Sử dụng dữ liệu thực từ database
                        DailyGoal userGoals = new DailyGoal(
                                currentUser.getLessonsDoneToday(), currentUser.getTargetLessons(), 
                                currentUser.getFlashcardsDoneToday(), currentUser.getTargetFlashcards(), 
                                currentUser.getQuizDoneToday(), currentUser.getTargetQuiz(),
                                0, // minutesDone
                                currentUser.getTargetTime());
                                
                        bindData(currentUser, userGoals);
                        
                        courseAdapter = new CourseAdapter(allCourses);
                        rvCourses.setAdapter(courseAdapter);
                    }
                });
            }
        });
    }

    private void bindData(User user, DailyGoal goals) {
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

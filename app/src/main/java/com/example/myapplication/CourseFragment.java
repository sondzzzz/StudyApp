package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.AvailableCourse;
import com.example.myapplication.models.CourseProgress;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CourseFragment extends Fragment {

    private RecyclerView rvAllCourses;
    private CourseListAdapter adapter;
    private TextView tvTotalCourses;
    private FloatingActionButton fabAddCourse;
    private List<AvailableCourse> availableCoursesList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        
        tvTotalCourses = view.findViewById(R.id.tv_total_courses);
        rvAllCourses = view.findViewById(R.id.rv_all_courses);
        fabAddCourse = view.findViewById(R.id.fab_add_course);
        
        rvAllCourses.setLayoutManager(new LinearLayoutManager(getContext()));

        setupAvailableCourses();
        loadUserCourses();

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        fabAddCourse.setOnClickListener(v -> {
            if (userId != -1) {
                showCourseSelectionDialog(userId);
            }
        });
        
        return view;
    }

    private void setupAvailableCourses() {
        availableCoursesList = new ArrayList<>();
        availableCoursesList.add(new AvailableCourse("Lập trình Android nâng cao", 25, android.R.drawable.ic_lock_idle_lock));
        availableCoursesList.add(new AvailableCourse("Java Cơ bản", 15, android.R.drawable.ic_menu_view));
        availableCoursesList.add(new AvailableCourse("Python cho Data Science", 30, android.R.drawable.btn_star_big_on));
        availableCoursesList.add(new AvailableCourse("Thiết kế UI/UX", 10, android.R.drawable.ic_menu_gallery));
        availableCoursesList.add(new AvailableCourse("Cấu trúc dữ liệu & Giải thuật", 40, android.R.drawable.ic_menu_sort_alphabetically));
    }

    private void loadUserCourses() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) return;

        AppDatabase db = AppDatabase.getInstance(requireContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CourseProgress> userCourses = db.appDao().getCoursesByUserId(userId);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvTotalCourses.setText("Khóa học đang tham gia: " + userCourses.size());
                    adapter = new CourseListAdapter(userCourses, course -> {
                        android.content.Intent intent = new android.content.Intent(getContext(), LessonListActivity.class);
                        intent.putExtra("course_name", course.getCourseName());
                        startActivity(intent);
                    });
                    rvAllCourses.setAdapter(adapter);
                });
            }
        });
    }

    private void showCourseSelectionDialog(int userId) {
        String[] courseNames = new String[availableCoursesList.size()];
        for (int i = 0; i < availableCoursesList.size(); i++) {
            courseNames[i] = availableCoursesList.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn khóa học mới");
        builder.setItems(courseNames, (dialog, which) -> {
            AvailableCourse selected = availableCoursesList.get(which);
            enrollCourse(userId, selected);
        });
        builder.show();
    }

    private void enrollCourse(int userId, AvailableCourse course) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CourseProgress> current = db.appDao().getCoursesByUserId(userId);
            for (CourseProgress cp : current) {
                if (cp.getCourseName().equals(course.getName())) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Bạn đã tham gia khóa học này rồi!", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }
            }

            CourseProgress newEnrollment = new CourseProgress(userId, course.getName(), 0, course.getTotalLessons(), 0, course.getIconResId());
            newEnrollment.setLastAccessed(System.currentTimeMillis());
            db.appDao().insertCourse(newEnrollment);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Đã thêm khóa học: " + course.getName(), Toast.LENGTH_SHORT).show();
                    loadUserCourses();
                });
            }
        });
    }
}

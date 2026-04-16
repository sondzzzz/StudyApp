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


import com.example.myapplication.models.AvailableCourse;
import com.example.myapplication.models.CourseProgress;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseFragment extends Fragment {

    private RecyclerView rvAllCourses;
    private CourseListAdapter adapter;
    private TextView tvTotalCourses;
    private FloatingActionButton fabAddCourse;
    private List<AvailableCourse> availableCoursesList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvTotalCourses = view.findViewById(R.id.tv_total_courses);
        rvAllCourses = view.findViewById(R.id.rv_all_courses);
        fabAddCourse = view.findViewById(R.id.fab_add_course);
        
        rvAllCourses.setLayoutManager(new LinearLayoutManager(getContext()));

        setupAvailableCourses();
        loadUserCoursesFromFirebase();

        fabAddCourse.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                showCourseSelectionDialog();
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

    private void loadUserCoursesFromFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
            .collection("course_progress")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<CourseProgress> userCourses = queryDocumentSnapshots.toObjects(CourseProgress.class);

                if (isAdded()) {
                    tvTotalCourses.setText("Khóa học đang tham gia: " + userCourses.size());
                    adapter = new CourseListAdapter(userCourses, course -> {
                        android.content.Intent intent = new android.content.Intent(getContext(), LessonListActivity.class);
                        intent.putExtra("course_name", course.getCourseName());
                        startActivity(intent);
                    });
                    rvAllCourses.setAdapter(adapter);
                }
            })
            .addOnFailureListener(e -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải khóa học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showCourseSelectionDialog() {
        String[] courseNames = new String[availableCoursesList.size()];
        for (int i = 0; i < availableCoursesList.size(); i++) {
            courseNames[i] = availableCoursesList.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn khóa học mới");
        builder.setItems(courseNames, (dialog, which) -> {
            AvailableCourse selected = availableCoursesList.get(which);
            enrollCourseFirebase(selected);
        });
        builder.show();
    }

    private void enrollCourseFirebase(AvailableCourse course) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Kiểm tra xem đã đăng ký chưa
        db.collection("users").document(user.getUid())
            .collection("course_progress")
            .whereEqualTo("courseName", course.getName())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    Toast.makeText(getContext(), "Bạn đã tham gia khóa học này rồi!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Thêm khóa học mới
                Map<String, Object> newEnrollment = new HashMap<>();
                newEnrollment.put("courseName", course.getName());
                newEnrollment.put("currentLesson", 0);
                newEnrollment.put("totalLessons", course.getTotalLessons());
                newEnrollment.put("percentComplete", 0);
                newEnrollment.put("iconResId", course.getIconResId());
                newEnrollment.put("lastAccessed", System.currentTimeMillis());

                db.collection("users").document(user.getUid())
                    .collection("course_progress")
                    .add(newEnrollment)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Đã thêm khóa học: " + course.getName(), Toast.LENGTH_SHORT).show();
                        loadUserCoursesFromFirebase();
                    });
            });
    }
}

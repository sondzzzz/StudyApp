package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.models.CourseProgress;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<CourseProgress> courseList;

    public CourseAdapter(List<CourseProgress> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_card, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseProgress course = courseList.get(position);
        holder.tvCourseName.setText(course.getCourseName());
        holder.ivCourseIcon.setImageResource(course.getIconResId());
        
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), LessonListActivity.class);
            intent.putExtra("course_name", course.getCourseName());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCourseIcon;
        TextView tvCourseName;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourseIcon = itemView.findViewById(R.id.iv_course_icon);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
        }
    }
}

package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.Lesson;
import java.util.List;

public class LessonListActivity extends AppCompatActivity {

    private RecyclerView rvLessons;
    private String courseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        courseName = getIntent().getStringExtra("course_name");

        Toolbar toolbar = findViewById(R.id.toolbar_lessons);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(courseName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvLessons = findViewById(R.id.rv_lessons);
        rvLessons.setLayoutManager(new LinearLayoutManager(this));

        loadLessons();
    }

    private void loadLessons() {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Lesson> lessons = db.appDao().getLessonsByCourse(courseName);
            runOnUiThread(() -> {
                rvLessons.setAdapter(new LessonAdapter(lessons));
            });
        });
    }

    class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {
        private List<Lesson> lessons;

        LessonAdapter(List<Lesson> lessons) {
            this.lessons = lessons;
        }

        @NonNull
        @Override
        public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new LessonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
            Lesson lesson = lessons.get(position);
            holder.text1.setText("Bài " + lesson.getLessonNumber());
            holder.text2.setText(lesson.getTitle());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(LessonListActivity.this, LessonDetailActivity.class);
                intent.putExtra("course_name", courseName);
                intent.putExtra("lesson_number", lesson.getLessonNumber());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return lessons.size();
        }

        class LessonViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            LessonViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}

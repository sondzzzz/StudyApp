package com.example.myapplication.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.myapplication.models.User;
import com.example.myapplication.models.CourseProgress;
import com.example.myapplication.models.QuizQuestion;
import com.example.myapplication.models.Lesson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {User.class, CourseProgress.class, QuizQuestion.class, Lesson.class}, version = 11)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract AppDao appDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "edupro_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            databaseWriteExecutor.execute(() -> {
                                prefillDatabase(context, getInstance(context).appDao());
                            });
                        }

                        @Override
                        public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                            super.onDestructiveMigration(db);
                            databaseWriteExecutor.execute(() -> {
                                prefillDatabase(context, getInstance(context).appDao());
                            });
                        }
                    })
                    .build();
        }
        return instance;
    }

    private static void prefillDatabase(Context context, AppDao dao) {
        try {
            StringBuilder builder = new StringBuilder();
            InputStream in = context.getAssets().open("quiz_questions.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JSONArray jsonArray = new JSONArray(builder.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                QuizQuestion question = new QuizQuestion(
                        obj.getString("courseName"),
                        obj.getString("question"),
                        obj.getString("optionA"),
                        obj.getString("optionB"),
                        obj.getString("optionC"),
                        obj.getString("optionD"),
                        obj.getInt("correctAnswer")
                );
                dao.insertQuestion(question);
            }

            builder = new StringBuilder();
            in = context.getAssets().open("lessons.json");
            reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JSONArray lessonsArray = new JSONArray(builder.toString());

            for (int i = 0; i < lessonsArray.length(); i++) {
                JSONObject obj = lessonsArray.getJSONObject(i);
                Lesson lesson = new Lesson(
                        obj.getString("courseName"),
                        obj.getInt("lessonNumber"),
                        obj.getString("title"),
                        obj.getString("content")
                );
                dao.insertLesson(lesson);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

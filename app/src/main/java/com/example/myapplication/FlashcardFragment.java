package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.CourseProgress;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlashcardFragment extends Fragment {

    private View layoutCourseList, layoutPlayer;
    private RecyclerView rvCourses;
    private TextView tvTitle, tvCardContent, tvCardSide, tvCardProgress;
    private CardView cvFlashcard;
    private MaterialButton btnPrev, btnNext, btnComplete;
    private ImageButton btnBack;

    private List<Flashcard> flashcardList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isFront = true;
    private String currentCourseName = "";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        layoutCourseList = view.findViewById(R.id.layout_flashcard_course_list);
        layoutPlayer = view.findViewById(R.id.layout_flashcard_player);
        rvCourses = view.findViewById(R.id.rv_flashcard_courses);
        tvTitle = view.findViewById(R.id.tv_flashcard_title);
        tvCardContent = view.findViewById(R.id.tv_card_content);
        tvCardSide = view.findViewById(R.id.tv_card_side);
        tvCardProgress = view.findViewById(R.id.tv_card_progress);
        cvFlashcard = view.findViewById(R.id.cv_flashcard);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);
        btnComplete = view.findViewById(R.id.btn_complete_flashcard);
        btnBack = view.findViewById(R.id.btn_back_to_list);

        rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        loadEnrolledCourses();

        cvFlashcard.setOnClickListener(v -> {
            isFront = !isFront;
            updateCardUI();
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < flashcardList.size() - 1) {
                currentIndex++;
                isFront = true;
                updateCardUI();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                isFront = true;
                updateCardUI();
            }
        });

        btnBack.setOnClickListener(v -> {
            layoutPlayer.setVisibility(View.GONE);
            layoutCourseList.setVisibility(View.VISIBLE);
        });

        btnComplete.setOnClickListener(v -> completeSession());

        return view;
    }

    private void loadEnrolledCourses() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .collection("course_progress")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CourseProgress> courses = queryDocumentSnapshots.toObjects(CourseProgress.class);
                    if (isAdded()) {
                        FlashcardCourseAdapter adapter = new FlashcardCourseAdapter(courses, this::startFlashcards);
                        rvCourses.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Lỗi tải khóa học: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getSafeDocId(String courseName) {
        // Firestore ID không được chứa "/"
        return courseName.replace("/", "_");
    }

    private void startFlashcards(String courseName) {
        currentCourseName = courseName;
        currentIndex = 0;
        isFront = true;
        
        String safeId = getSafeDocId(courseName);
        
        db.collection("flashcards").document(safeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    flashcardList.clear();
                    if (documentSnapshot.exists() && documentSnapshot.contains("cards")) {
                        List<Map<String, String>> cards = (List<Map<String, String>>) documentSnapshot.get("cards");
                        if (cards != null) {
                            for (Map<String, String> card : cards) {
                                flashcardList.add(new Flashcard(card.get("term"), card.get("definition")));
                            }
                        }
                    }

                    if (!flashcardList.isEmpty()) {
                        showPlayer();
                    } else {
                        Toast.makeText(getContext(), "Môn học này hiện chưa có bộ thẻ Flashcard!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showPlayer() {
        layoutCourseList.setVisibility(View.GONE);
        layoutPlayer.setVisibility(View.VISIBLE);
        tvTitle.setText(currentCourseName);
        updateCardUI();
    }

    private void updateCardUI() {
        if (flashcardList.isEmpty()) return;
        
        Flashcard current = flashcardList.get(currentIndex);
        tvCardContent.setText(isFront ? current.term : current.definition);
        tvCardSide.setText(isFront ? "Mặt trước (Thuật ngữ)" : "Mặt sau (Định nghĩa)");
        tvCardProgress.setText((currentIndex + 1) + " / " + flashcardList.size());

        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < flashcardList.size() - 1);
        
        if (currentIndex == flashcardList.size() - 1 && !isFront) {
            btnComplete.setVisibility(View.VISIBLE);
        } else {
            btnComplete.setVisibility(View.GONE);
        }
    }

    private void completeSession() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    long done = doc.contains("flashcardsDoneToday") ? doc.getLong("flashcardsDoneToday") : 0;
                    db.collection("users").document(user.getUid()).update("flashcardsDoneToday", done + 1)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Đã cập nhật mục tiêu Flashcard!", Toast.LENGTH_SHORT).show();
                                layoutPlayer.setVisibility(View.GONE);
                                layoutCourseList.setVisibility(View.VISIBLE);
                            });
                });
    }

    private static class Flashcard {
        String term, definition;
        Flashcard(String t, String d) { term = t; definition = d; }
    }

    private class FlashcardCourseAdapter extends RecyclerView.Adapter<FlashcardCourseAdapter.ViewHolder> {
        private List<CourseProgress> courses;
        private java.util.function.Consumer<String> listener;

        FlashcardCourseAdapter(List<CourseProgress> courses, java.util.function.Consumer<String> listener) {
            this.courses = courses;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_course, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CourseProgress cp = courses.get(position);
            holder.name.setText(cp.getCourseName());
            holder.icon.setImageResource(cp.getIconResId());
            holder.itemView.setOnClickListener(v -> listener.accept(cp.getCourseName()));
        }

        @Override
        public int getItemCount() { return courses.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name; ImageView icon;
            ViewHolder(View v) { super(v); name = v.findViewById(R.id.tv_course_name); icon = v.findViewById(R.id.iv_course_icon); }
        }
    }
}

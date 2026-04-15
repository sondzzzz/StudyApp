package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.User;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private TextView tvFullName, tvUsername;
    private MaterialButton btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvFullName = view.findViewById(R.id.tv_profile_name);
        tvUsername = view.findViewById(R.id.tv_profile_username);
        btnLogout = view.findViewById(R.id.btn_logout);

        loadUserData();

        btnLogout.setOnClickListener(v -> {
            // Xóa session
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Quay lại màn hình đăng nhập
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId != -1) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                User user = AppDatabase.getInstance(requireContext()).appDao().getUserById(userId);
                if (user != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvFullName.setText(user.getFullName());
                        tvUsername.setText("@" + user.getUsername());
                    });
                }
            });
        }
    }
}

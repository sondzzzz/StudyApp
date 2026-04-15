package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etUsername, etPassword;
    private MaterialButton btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.et_register_fullname);
        etUsername = findViewById(R.id.et_register_username);
        etPassword = findViewById(R.id.et_register_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);

        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                // Kiểm tra username tồn tại chưa
                if (db.appDao().getUserByUsername(username) != null) {
                    runOnUiThread(() -> Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Tạo user mới
                User newUser = new User(username, password, fullName, 0, 0, 1000, 1);
                long userId = db.appDao().insertUser(newUser);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Lưu phiên đăng nhập
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("user_id", (int)userId);
                    editor.apply();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
            });
        });

        tvLoginLink.setOnClickListener(v -> {
            finish();
        });
    }
}

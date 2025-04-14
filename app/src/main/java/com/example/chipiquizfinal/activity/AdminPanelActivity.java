package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.R;

public class AdminPanelActivity extends AppCompatActivity {

    Button addQuestionBtn, viewUsersBtn, viewQuestionsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        addQuestionBtn = findViewById(R.id.addQuestionBtn);
        viewUsersBtn = findViewById(R.id.viewUsersBtn);
        viewQuestionsBtn = findViewById(R.id.viewQuestionsBtn);

        addQuestionBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, SelectQuestionTypeActivity.class));
        });

        viewUsersBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, UserListActivity.class));
        });

        viewQuestionsBtn.setOnClickListener(v -> {
            Toast.makeText(this, "TODO: Show all questions", Toast.LENGTH_SHORT).show();
        });
    }
}

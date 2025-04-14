package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.R;

public class SelectQuestionTypeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_question_type);

        findViewById(R.id.multipleChoiceBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, AddMultipleChoiceQuestionActivity.class));
        });

        findViewById(R.id.inputQuestionBtn).setOnClickListener(v -> {
//            startActivity(new Intent(this, AddInputQuestionActivity.class));
        });

        findViewById(R.id.hardwareQuestionBtn).setOnClickListener(v -> {
//            startActivity(new Intent(this, AddHardwareQuestionActivity.class));
        });
    }
}

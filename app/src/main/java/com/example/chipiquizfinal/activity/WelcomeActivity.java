package com.example.chipiquizfinal.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.chipiquizfinal.AppDatabase;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.ExerciseDao;
import com.example.chipiquizfinal.dao.ProgrammingLanguageDao;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.Exercise;
import com.example.chipiquizfinal.entity.ProgrammingLanguage;
import com.example.chipiquizfinal.entity.User;

import java.util.List;
import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String langCode = prefs.getString("language", "bg"); // default bg
        setLocale(langCode);

        ExerciseDao exerciseDao = MyApplication.getDatabase().exerciseDao();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button registerBtn = findViewById(R.id.registerButton);
        Button loginBtn = findViewById(R.id.loginButton);

        registerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });

        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });




            insertExercisesForAllLanguages();





    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Записване на езика в SharedPreferences (ако искаш да го помниш)
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putString("language", langCode).apply();
    }
    private void insertExercisesForAllLanguages() {
        ExerciseDao exerciseDao = MyApplication.getDatabase().exerciseDao();
        ProgrammingLanguageDao languageDao = MyApplication.getDatabase().programmingLanguageDao();

        List<ProgrammingLanguage> languages = languageDao.getAllLanguages();

        for (ProgrammingLanguage lang : languages) {

            if (exerciseDao.getExercisesForLevel(lang.getId(), 1).isEmpty()) {
                for (int level = 1; level <= 5; level++) {
                    for (int position = 1; position <= 10; position++) {
                        Exercise exercise = new Exercise();
                        exercise.languageId = lang.getId();
                        exercise.level = level;
                        exercise.position = position;
                        exercise.isUnlocked = (level == 1 && position == 1);

                        exerciseDao.insert(exercise);
                    }
                }
            }
        }
    }


}
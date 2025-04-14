package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.*;
import com.example.chipiquizfinal.entity.*;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LinearLayout lessonPathContainer;
    private Spinner languageSwitcher;
    private TextView streakCount, pointsCount, livesCount;

    private UserDao userDao;
    private User currentUser;
    private int selectedLanguageId;
    private int currentLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Зареди езика от SharedPreferences (по подразбиране "bg")
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String langCode = prefs.getString("language", "bg");
        setLocale(langCode);  // приложи езика

        setContentView(R.layout.activity_main);

        lessonPathContainer = findViewById(R.id.lessonPathContainer);
        streakCount = findViewById(R.id.streakCount);
        pointsCount = findViewById(R.id.pointsCount);
        livesCount = findViewById(R.id.livesCount);
        ImageButton changeLanguageBtn = findViewById(R.id.changeLanguageBtn);
        changeLanguageBtn.setOnClickListener(v -> showLanguageDialog());

        userDao = MyApplication.getDatabase().userDao();
        ExerciseDao exerciseDao = MyApplication.getDatabase().exerciseDao();

        // 2. Вземаме текущия потребител по имейл
        currentUser = userDao.getUserByEmail(MyApplication.getLoggedEmail());

        if (currentUser != null) {
            // Ако не е зададен език, по подразбиране "bg"
            if (currentUser.getSelectedLanguageCode() == null || currentUser.getSelectedLanguageCode().isEmpty()) {
                currentUser.setSelectedLanguageCode("bg");
                userDao.update(currentUser);
            }

            // 3. Вземи езика от UserLanguageChoice таблицата
            selectedLanguageId = getLanguageIdForUser(currentUser.getId());
            currentLevel = currentUser.getLevel();

            updateStats();
            insertExercisesIfNeeded(selectedLanguageId, currentLevel);

            List<Exercise> exercises = exerciseDao.getExercisesForLevel(selectedLanguageId, currentLevel);

            int userProgressIndex = 2;
            loadLessonPathZigzag(exercises, userProgressIndex);
        } else {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
        }

        Button openArButton = findViewById(R.id.openArButton);
        openArButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ArModelViewerActivity.class);
            startActivity(intent);
        });
    }



    private void updateStats() {
        streakCount.setText(String.valueOf(currentUser.getConsecutiveDays()));
        pointsCount.setText(String.valueOf(currentUser.getPoints()));
        livesCount.setText(String.valueOf(currentUser.getLives()));
    }

    private int getLanguageIdForUser(int userId) {
        UserLanguageChoiceDao langDao = MyApplication.getDatabase().userLanguageChoiceDao();
        UserLanguageChoice choice = langDao.getByUserId(userId);
        return (choice != null) ? choice.getLanguageId() : 1; // default fallback
    }

    private void insertExercisesIfNeeded(int languageId, int level) {
        ExerciseDao exerciseDao = MyApplication.getDatabase().exerciseDao();
        List<Exercise> existing = exerciseDao.getExercisesForLevel(languageId, level);

        if (existing == null || existing.isEmpty()) {
            for (int i = 1; i <= 10; i++) {
                Exercise ex = new Exercise();
                ex.setLanguageId(languageId);
                ex.setLevel(level);
                ex.setPosition(i);
                exerciseDao.insert(ex);
            }
            Log.d("EXERCISE_INIT", "10 упражнения добавени.");
        }
    }

    private void loadLessonPathZigzag(List<Exercise> exercises, int userProgress) {
        LinearLayout container = findViewById(R.id.lessonPathContainer);
        container.removeAllViews();
        int exercisesPerLevel = 10;
        boolean logoAdded = false;

        for (int i = 0; i < exercises.size(); i++) {
            final int index = i;

            // 🔼 Добавяме заглавие за всяко ниво
            if (i % exercisesPerLevel == 0) {
                TextView levelTitle = new TextView(this);
                levelTitle.setText("Level " + (i / exercisesPerLevel + 1));
                levelTitle.setTextSize(20f);
                levelTitle.setTextColor(getResources().getColor(R.color.purple_dark));
                levelTitle.setPadding(0, 40, 0, 16);
                levelTitle.setGravity(Gravity.CENTER_HORIZONTAL);
                lessonPathContainer.addView(levelTitle);
            }

//            // 🐿️ Добавяме логото само веднъж след 6-тия
//            if (i == 6 && !logoAdded) {
//                ImageView logo = new ImageView(this);
//                logo.setImageResource(R.drawable.pixee);
//                LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(200, 200);
//                logoParams.setMargins(0, 40, 0, 40);
//                logo.setLayoutParams(logoParams);
//                logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                lessonPathContainer.addView(logo);
//                logoAdded = true;
//            }

            // 🎯 Добавяне на самото балонче
            View rowView = getLayoutInflater().inflate(R.layout.item_lesson_row, lessonPathContainer, false);
            ImageView characterIcon = rowView.findViewById(R.id.characterIcon);
            FrameLayout lessonContainer = rowView.findViewById(R.id.lessonBubbleContainer);

            View lessonView = getLayoutInflater().inflate(R.layout.item_lesson, lessonContainer, false);
            TextView lessonNumber = lessonView.findViewById(R.id.lessonNumber);
            ImageView lessonIcon = lessonView.findViewById(R.id.lessonIcon);

            lessonNumber.setText(String.valueOf(i + 1));

            if (index <= userProgress) {
                lessonIcon.setBackgroundResource(R.drawable.lesson_circle_bg);

                lessonView.setOnClickListener(v -> {
                    int exerciseId = exercises.get(index).getId();

                    Intent intent = new Intent(MainActivity.this, LessonActivity.class);
                    intent.putExtra("exerciseId", exerciseId);
                    intent.putExtra("level", exercises.get(index).getLevel());
                    intent.putExtra("position", exercises.get(index).getPosition());
                    startActivity(intent);
                });

            } else {
                lessonIcon.setBackgroundResource(R.drawable.lesson_circle_locked);
                lessonIcon.setAlpha(0.4f);
                lessonView.setEnabled(false); // заключено
            }


            // Редуване: i % 2 -> зигзаг
            LinearLayout layout = (LinearLayout) rowView;
            layout.removeAllViews();

            if (i % 2 == 0) {
                layout.addView(lessonContainer);
                layout.addView(characterIcon);
            } else {
                layout.addView(characterIcon);
                layout.addView(lessonContainer);
            }

            lessonContainer.addView(lessonView);
            container.addView(rowView);
        }
    }

    private void showLanguageDialog() {
        final String[] languages = {"Български", "English"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Избери език / Choose Language");
        builder.setItems(languages, (dialog, which) -> {
            String langCode = (which == 0) ? "bg" : "en";
            setLocale(langCode);
            recreate(); // презарежда Activity-то
        });
        builder.show();
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Записване за бъдещо стартиране
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putString("language", langCode).apply();

        // Записване и в базата, ако имаме потребител
        if (currentUser != null) {
            currentUser.setSelectedLanguageCode(langCode);
            userDao.update(currentUser);
        }
    }



}

package com.example.chipiquizfinal.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.*;
import com.example.chipiquizfinal.entity.*;
import com.example.chipiquizfinal.firestore.FirestoreUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity  {

    private LinearLayout lessonPathContainer;
    private Spinner languageSwitcher;
    private TextView streakCount, pointsCount, livesCount;

    private UserDao userDao;
    private User currentUser;
    private int selectedLanguageId;
    private FirebaseFirestore db;
    private int currentLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        seedInitialUsers();
        setupHeader();

        String selectedLangCode = "bg";  // или "bg"

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("lang", selectedLangCode)
                .apply();


        setContentView(R.layout.activity_main);

        lessonPathContainer = findViewById(R.id.lessonPathContainer);
        streakCount = findViewById(R.id.streakCount);
        pointsCount = findViewById(R.id.pointsCount);
        livesCount = findViewById(R.id.livesCount);
        ImageButton changeLanguageBtn = findViewById(R.id.changeLanguageBtn);
        changeLanguageBtn.setOnClickListener(v -> showLanguageDialog());

        userDao = MyApplication.getDatabase().userDao();
        ExerciseDao exerciseDao = MyApplication.getDatabase().exerciseDao();


        String email = MyApplication.getLoggedEmail();
        loadCurrentUser(this,email,
                user -> {
                    // onLoaded
                    currentUser = user;
                    setupHeader();
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


                    BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
                    bottomNav.setSelectedItemId(R.id.nav_home);

                    bottomNav.setOnItemSelectedListener(item -> {
                        int id = item.getItemId();
                        if (id == R.id.nav_home) {
                            // already here
                            return true;
                        } else if (id == R.id.nav_community) {
                            startActivity(new Intent(this, AllUsersActivity.class));
                            return true;
                        } else if (id == R.id.nav_profile) {
                            startActivity(new Intent(this, ProfileActivity.class));
                            return true;
                        }
                        else if (id == R.id.nav_map) {
                            startActivity(new Intent(this, MapActivity.class));
                            return true;
                        }      else if (id == R.id.nav_rewards) {
                            startActivity(new Intent(this, LeaderboardActivity.class));
                            return true;
                        }
                        return false;
                    });
                },
                errorMsg -> {
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    finish();
                }
        );
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

            if (i % exercisesPerLevel == 0) {
                TextView levelTitle = new TextView(this);
                levelTitle.setText("Level " + (i / exercisesPerLevel + 1));
                levelTitle.setTextSize(20f);
                levelTitle.setTextColor(getResources().getColor(R.color.purple_dark));
                levelTitle.setPadding(0, 40, 0, 16);
                levelTitle.setGravity(Gravity.CENTER_HORIZONTAL);
                lessonPathContainer.addView(levelTitle);
                Typeface tf = ResourcesCompat.getFont(this, R.font.underdog);
                levelTitle.setTypeface(tf);
            }

            View rowView = getLayoutInflater().inflate(R.layout.item_lesson_row, lessonPathContainer, false);
            ImageView characterIcon = rowView.findViewById(R.id.characterIcon);
            ImageView characterIcon1 = rowView.findViewById(R.id.characterIcon1);
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
                lessonView.setEnabled(false);
            }

            LinearLayout layout = (LinearLayout) rowView;
            layout.removeAllViews();

            if (i % 2 == 0) {
                layout.addView(lessonContainer);
                layout.addView(characterIcon);
            } else {
                layout.addView(characterIcon1);
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
            recreate();
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

    private void seedInitialUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String placeholderUrl = "https://firebasestorage.googleapis.com/v0/b/your‑project.appspot.com/o/default_profile.png?alt=media";

        List<FirestoreUser> initial = List.of(
                new FirestoreUser("admin@pixee.com", "Admin", placeholderUrl, 0,5,0,"en"),
                new FirestoreUser("oli@pixee.com",   "Oli",   placeholderUrl, 0,5,0,"bg"),
                new FirestoreUser("val@pixee.com",   "Val",   placeholderUrl, 0,5,0,"en")
        );
        for (int i = 0; i < initial.size(); i++) {
            String docId = String.valueOf(i+1);
            db.collection("users")
                    .document(docId)
                    .set(initial.get(i))
                    .addOnSuccessListener(aVoid -> Log.d("SEED", "User "+docId+" записан успешно"))
                    .addOnFailureListener(e -> Log.e("SEED", "Грешка при seed на "+docId, e));
        }
    }


}

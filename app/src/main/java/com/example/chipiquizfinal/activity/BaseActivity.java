package com.example.chipiquizfinal.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.ProgrammingLanguageDao;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.dao.UserLanguageChoiceDao;
import com.example.chipiquizfinal.entity.ProgrammingLanguage;
import com.example.chipiquizfinal.entity.User;
import com.example.chipiquizfinal.entity.UserLanguageChoice;
import com.example.chipiquizfinal.helper.LocaleHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseActivity extends AppCompatActivity {
    protected static final String PREFS_NAME = "user_prefs";
    protected static final String KEY_LANG   = "language";
    private static final String KEY_PATH_NAME   = "path_name";

    protected Spinner     languageSwitcher;
    protected TextView    streakCount;
    protected TextView    pointsCount;
    protected TextView    livesCount;
    protected ImageButton changeLanguageBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    protected void setupHeader() {
        languageSwitcher   = findViewById(R.id.languageSwitcher);
        streakCount        = findViewById(R.id.streakCount);
        pointsCount        = findViewById(R.id.pointsCount);
        livesCount         = findViewById(R.id.livesCount);
        changeLanguageBtn  = findViewById(R.id.changeLanguageBtn);

        refreshHeaderStats();

        ProgrammingLanguageDao langDao = MyApplication.getDatabase().programmingLanguageDao();
        List<ProgrammingLanguage> langs = langDao.getAllLanguages();
        ArrayAdapter<ProgrammingLanguage> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, langs
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSwitcher.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedName = prefs.getString(KEY_PATH_NAME, null);
        if (savedName != null) {
            for (int i = 0; i < langs.size(); i++) {
                if (savedName.equals(langs.get(i).getName())) {
                    languageSwitcher.setSelection(i);
                    break;
                }
            }
        }
        languageSwitcher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit()
                        .putString(KEY_PATH_NAME, langs.get(position).getName())
                        .apply();

            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        changeLanguageBtn.setOnClickListener(v -> {
            String cur  = prefs.getString(KEY_LANG, "en");
            String next = cur.equals("en") ? "bg" : "en";
            prefs.edit().putString(KEY_LANG, next).apply();
            recreate();
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANG, "en");
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang));
    }


    protected void refreshHeaderStats() {
        User me = MyApplication.getDatabase()
                .userDao()
                .getUserByEmail(MyApplication.getLoggedEmail());
        if (me != null) {
            streakCount.setText(String.valueOf(me.getConsecutiveDays()));
            pointsCount.setText(String.valueOf(me.getPoints()));
            livesCount.setText(String.valueOf(me.getLives()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHeaderStats();
    }




        private void insertUserAndChoices(
                User u,
                Consumer<User> onLoaded,
                Consumer<String> onError
        ) {
            UserDao userDao = MyApplication.getDatabase().userDao();
            UserLanguageChoiceDao choiceDao = MyApplication.getDatabase().userLanguageChoiceDao();
            ProgrammingLanguageDao langDao = MyApplication.getDatabase().programmingLanguageDao();

             userDao.insert(u);

             FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(String.valueOf(u.getId()))
                    .collection("choices")
                    .get()
                    .addOnSuccessListener(qs2 -> {
                        for (DocumentSnapshot cdoc : qs2.getDocuments()) {
                            String langName = cdoc.getString("languageName");
                            ProgrammingLanguage pl = langDao.getByName(langName);
                            if (pl == null) continue;

                            UserLanguageChoice choice = new UserLanguageChoice();
                            choice.setUserId(u.getId());
                            choice.setLanguageId(pl.getId());
                            choice.setLevel(cdoc.getLong("level").intValue());
                            choice.setDailyPractice(cdoc.getLong("dailyPractice").intValue());
                            choiceDao.insert(choice);
                        }
                        onLoaded.accept(u);
                    })
                    .addOnFailureListener(e -> onError.accept(e.getMessage()));
        }







    public void loadCurrentUser(Context context, String email, Consumer<User> onLoaded, Consumer<String> onError) {
        User local = MyApplication.getDatabase().userDao().getUserByEmail(email);
        UserDao userDao = MyApplication.getDatabase().userDao();
        UserLanguageChoiceDao choiceDao = MyApplication.getDatabase().userLanguageChoiceDao();
        ProgrammingLanguageDao langDao = MyApplication.getDatabase().programmingLanguageDao();

        if (local != null) {
            onLoaded.accept(local);
        } else {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(qs -> {
                        if (!qs.isEmpty()) {
                            DocumentSnapshot doc = qs.getDocuments().get(0);

                            User u = new User();
                            u.setId(Integer.parseInt(doc.getId()));
                            u.setEmail(doc.getString("email"));
                            u.setUsername(doc.getString("username"));
                            u.setPassword(doc.getString("password"));    // ако пазиш паролата
                            u.setFirstName(doc.getString("firstName"));  // ← ново
                            u.setLastName(doc.getString("lastName"));    // ← ново
                            u.setLanguage(doc.getString("language"));    // ← ново
                            u.setPoints(doc.getLong("points").intValue());
                            u.setLives(doc.getLong("lives").intValue());
                            u.setConsecutiveDays(doc.getLong("streak").intValue());
                            u.setSelectedLanguageCode(doc.getString("selectedLanguage"));

                            String imageUrl = doc.getString("profileImagePath");
                            u.setProfileImagePath(imageUrl);

                            insertUserAndChoices(u, onLoaded, onError);
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(MyApplication.getContext())
                                        .asBitmap()
                                        .load(imageUrl)
                                        .circleCrop()
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> t) {
                                                File dir = new File(context.getFilesDir(), "profile_images");
                                                if (!dir.exists()) dir.mkdirs();
                                                File out = new File(dir, u.getId() + ".png");
                                                try (FileOutputStream fos = new FileOutputStream(out)) {
                                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                                    fos.flush();
                                                     u.setProfileImagePath(out.getAbsolutePath());
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                                MyApplication.getDatabase().userDao().insert(u);
                                                onLoaded.accept(u);
                                            }
                                            @Override public void onLoadCleared(@Nullable Drawable placeholder) { }
                                        });
                            } else {
                               MyApplication.getDatabase().userDao().insert(u);
                                onLoaded.accept(u);
                            }



                            MyApplication.getDatabase().userDao().insert(u);

                             userDao.insert(u);


                            ProgrammingLanguage pl = langDao.getByName(u.getLanguage());
                            if (pl != null) {
                                UserLanguageChoice choice = new UserLanguageChoice();
                                choice.setUserId(u.getId());
                                choice.setLanguageId(pl.getId());
                                choice.setLevel(u.getLevel());
                                choice.setDailyPractice(u.getDailyPractice());
                                choiceDao.insert(choice);
                            }

                            onLoaded.accept(u);

// Сега четем choices под‐колекцията и ги записваме локално:
//                            FirebaseFirestore.getInstance()
//                                    .collection("users")
//                                    .document(String.valueOf(u.getId()))
//                                    .collection("choices")
//                                    .get()
//                                    .addOnSuccessListener(qsChoices -> {
//                                        for (DocumentSnapshot choiceDoc : qsChoices.getDocuments()) {
//                                            // Прочитаме името на езика от Firestore
//                                            String langName = choiceDoc.getString("languageName");
//                                            // Намираме записания ProgrammingLanguage в Room
//                                             pl = langDao.getByName(langName);
//                                            if (pl == null) continue;
//
//                                            UserLanguageChoice choice = new UserLanguageChoice();
//                                            choice.setUserId(u.getId());
//                                            choice.setLanguageId(pl.getId());
//                                            choice.setLevel(choiceDoc.getLong("level").intValue());
//                                            choice.setDailyPractice(choiceDoc.getLong("dailyPractice").intValue());
//
//                                            choiceDao.insert(choice);
//                                        }
//                                        // След като сме записали всички choices, викаме callback
//                                        onLoaded.accept(u);
//                                    })
//                                    .addOnFailureListener(e -> onError.accept("Failed to load choices: " + e.getMessage()));
//


                        } else {
                            onError.accept("User not found in cloud");
                        }
                    })
                    .addOnFailureListener(e -> onError.accept(e.getMessage()));
        }
    }




}

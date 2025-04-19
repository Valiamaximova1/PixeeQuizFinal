package com.example.chipiquizfinal.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.ProgrammingLanguageDao;
import com.example.chipiquizfinal.entity.ProgrammingLanguage;
import com.example.chipiquizfinal.entity.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.function.Consumer;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String PREFS_NAME      = "prefs";
    private static final String KEY_LANG        = "lang";
    private static final String KEY_PATH_NAME   = "path_name";

    protected Spinner     languageSwitcher;
    protected TextView    streakCount;
    protected TextView    pointsCount;
    protected TextView    livesCount;
    protected ImageButton changeLanguageBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // setupHeader() да се извика в наследниците след setContentView()
    }

    /**
     * Инициализира header елементи: spinner за пътека, статистики и бутон за език.
     * Да се извика след setContentView() във всяко наследено Activity.
     */
    protected void setupHeader() {
        languageSwitcher   = findViewById(R.id.languageSwitcher);
        streakCount        = findViewById(R.id.streakCount);
        pointsCount        = findViewById(R.id.pointsCount);
        livesCount         = findViewById(R.id.livesCount);
        changeLanguageBtn  = findViewById(R.id.changeLanguageBtn);

        // Зареждаме статистиката на текущия потребител
        refreshHeaderStats();

        // Настройваме spinner за избор на пътека
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
                String name = langs.get(position).getName();
                prefs.edit().putString(KEY_PATH_NAME, name).apply();
                // TODO: обнови UI/въпросите според новия избор
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Бутон за смяна на интерфейсен език
        changeLanguageBtn.setOnClickListener(v -> {
            String cur = prefs.getString(KEY_LANG, "en");
            String next = cur.equals("en") ? "bg" : "en";
            prefs.edit().putString(KEY_LANG, next).apply();
            recreate();
        });
    }

    /**
     * Обновява lives, points и streak от базата.
     */
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
        // Обновяваме статистиката при връщане на преден план
        refreshHeaderStats();
    }

    public void loadCurrentUser(String email, Consumer<User> onLoaded, Consumer<String> onError) {
        User local = MyApplication.getDatabase().userDao().getUserByEmail(email);
        if (local != null) {
            onLoaded.accept(local);
        } else {
            // няма го локално, да го дръпнем от Firestore
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(qs -> {
                        if (!qs.isEmpty()) {
                            // в BaseActivity.loadCurrentUser, в onSuccessListener:
                            DocumentSnapshot doc = qs.getDocuments().get(0);
// …
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
                            u.setProfileImagePath(doc.getString("profileImageUrl"));
// …
                            MyApplication.getDatabase().userDao().insert(u);
                            onLoaded.accept(u);
                        } else {
                            onError.accept("User not found in cloud");
                        }
                    })
                    .addOnFailureListener(e -> onError.accept(e.getMessage()));
        }
    }

}

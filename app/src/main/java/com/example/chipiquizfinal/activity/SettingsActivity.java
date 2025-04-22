package com.example.chipiquizfinal.activity;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "prefs";
    private static final String KEY_LANG   = "lang";

    private RadioGroup langRadioGroup;
    private RadioButton rbBulgarian, rbEnglish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_settings);
//
//        langRadioGroup   = findViewById(R.id.langRadioGroup);
//        rbBulgarian      = findViewById(R.id.rbBulgarian);
//        rbEnglish        = findViewById(R.id.rbEnglish);
//
//        // 1) Зареждаме текущия избор
//        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        String currentLang = prefs.getString(KEY_LANG, "en");
//        if ("bg".equals(currentLang)) {
//            rbBulgarian.setChecked(true);
//        } else {
//            rbEnglish.setChecked(true);
//        }
//
//        // 2) Слушаме промени в групата
//        langRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            String newLang = "en";
//            if (checkedId == R.id.rbBulgarian) {
//                newLang = "bg";
//            }
//            // 3) Записваме в SharedPreferences
//            prefs.edit()
//                    .putString(KEY_LANG, newLang)
//                    .apply();
//
//            Toast.makeText(
//                    SettingsActivity.this,
//                    newLang.equals("bg") ? "Езикът е зададен на Български" : "Language set to English",
//                    Toast.LENGTH_SHORT
//            ).show();
//
//            // 4) Презареждаме само SettingsActivity, за да приеме новите ресурси
//            recreate();
//        });
    }
}

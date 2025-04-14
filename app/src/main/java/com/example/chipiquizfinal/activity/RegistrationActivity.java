package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.*;
import com.example.chipiquizfinal.entity.*;

import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, usernameInput, firstNameInput, lastNameInput;
    private Spinner languageSpinner, practiceSpinner, appLanguageSpinner;
    private Button createProfileBtn;

    private UserDao userDao;
    private ProgrammingLanguageDao languageDao;
    private UserLanguageChoiceDao userLanguageChoiceDao;

    private final String[] langCodes = {"bg", "en"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        userDao = MyApplication.getDatabase().userDao();
        languageDao = MyApplication.getDatabase().programmingLanguageDao();
        userLanguageChoiceDao = MyApplication.getDatabase().userLanguageChoiceDao();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        usernameInput = findViewById(R.id.usernameInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        languageSpinner = findViewById(R.id.languageSpinner);
        practiceSpinner = findViewById(R.id.practiceSpinner);
        appLanguageSpinner = findViewById(R.id.appLanguageSpinner);
        createProfileBtn = findViewById(R.id.createProfileBtn);

        // Езици за програмиране (на български)
        String[] languages = getResources().getStringArray(R.array.programming_languages_display);
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        languageSpinner.setAdapter(languageAdapter);

        // Време за упражнения
        String[] practiceOptions = {"5", "10", "15", "20", "30", "60"};
        ArrayAdapter<String> practiceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, practiceOptions);
        practiceSpinner.setAdapter(practiceAdapter);

        // Език на интерфейс
        ArrayAdapter<String> appLangAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Български", "English"});
        appLanguageSpinner.setAdapter(appLangAdapter);

        createProfileBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String selectedLangDisplay = languageSpinner.getSelectedItem().toString();
        String practiceStr = practiceSpinner.getSelectedItem().toString();
        int selectedLangIndex = appLanguageSpinner.getSelectedItemPosition();
        String selectedLangCode = langCodes[selectedLangIndex];

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()
                || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int practiceMinutes;
        try {
            practiceMinutes = Integer.parseInt(practiceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Practice time must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userDao.getUserByEmail(email) != null) {
            Toast.makeText(this, "User with this email already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Преобразуване на избрания език към оригиналното му име (ако е нужно)
        String baseLang = mapToBaseLanguage(selectedLangDisplay);

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setLevel(1);
        user.setPoints(0);
        user.setLives(5);
        user.setConsecutiveDays(0);
        user.setFreezeDay(false);
        user.setLanguage(baseLang);
        user.setDailyPractice(practiceMinutes);
        user.setSelectedLanguageCode(selectedLangCode);

        userDao.insert(user);

        User createdUser = userDao.getUserByEmail(email);
        if (createdUser == null) {
            Toast.makeText(this, "Error saving user.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgrammingLanguage lang = languageDao.getByName(baseLang);
        if (lang != null) {
            UserLanguageChoice choice = new UserLanguageChoice();
            choice.setUserId(createdUser.getId());
            choice.setLanguageId(lang.getId());
            choice.setLevel(1);
            choice.setDailyPractice(practiceMinutes);
            userLanguageChoiceDao.insert(choice);
        }

        Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private String mapToBaseLanguage(String displayLang) {
        switch (displayLang) {
            case "HTML и CSS": return "HTML & CSS";
            case "Хардуер": return "Hardware";
            default: return displayLang;
        }
    }
}

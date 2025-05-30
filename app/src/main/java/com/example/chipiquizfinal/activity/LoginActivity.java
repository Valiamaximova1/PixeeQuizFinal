package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.firestore.FirestoreUser;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn;
    private UserDao userDao;
    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDao = MyApplication.getDatabase().userDao();
        firestore = FirebaseFirestore.getInstance();


        emailInput    = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);
        loginBtn      = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> loginUser());
    }

//    private void loginUser() {
//        String email = emailInput.getText().toString().trim();
//        String password = passwordInput.getText().toString().trim();
//
//        if (email.isEmpty() || password.isEmpty()) {
//            Toast.makeText(this, "Моля, въведете имейл и парола.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 1) Локална проверка в Room
//        User localUser = userDao.getUserByEmail(email);
//        if (localUser == null || !localUser.getPassword().equals(password)) {
//            Toast.makeText(this, "Невалиден имейл или парола.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 2) Запазваме логнатия email и езика
//        MyApplication.setLoggedEmail(email);
//        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
//        prefs.edit()
//                .putString("logged_email", email)
//                .apply();
//
//        // Сменяме езика на приложението
//        String langCode = localUser.getSelectedLanguageCode();
//        if (langCode != null) setLocale(langCode);
//
//        // 3) Зареждаме допълнителни данни от Firestore
//        String docId = String.valueOf(localUser.getId());
//        firestore.collection("users")
//                .document(docId)
//                .get()
//                .addOnSuccessListener(this::onFirestoreUserLoaded)
//                .addOnFailureListener(e -> {
//                    // Ако не успее, продължаваме директно към Main
//                    goToNext(localUser);
//                });
//    }



    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String pwd   = passwordInput.getText().toString().trim();

        if (email.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Моля въведете имейл и парола.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        Toast.makeText(this, "Грешка: Потребителят не е намерен.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DocumentSnapshot doc = qs.getDocuments().get(0);
                    String storedPwd = doc.getString("password");
                    if (!pwd.equals(storedPwd)) {
                        Toast.makeText(this, "Грешна парола.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String username = doc.getString("username");
                    String first    = doc.getString("firstName");
                    String last     = doc.getString("lastName");
                    String lang     = doc.getString("selectedLanguage");
                    Long points     = doc.getLong("points");
                    Long lives      = doc.getLong("lives");
                    Long streak     = doc.getLong("streak");
                    String imgUrl   = doc.getString("profileImageUrl");
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("logged_email", email)
                            .apply();
                    MyApplication.setLoggedEmail(email);

//                    FirebaseMessaging.getInstance().getToken()
//                            .addOnSuccessListener(token -> {
//                                String uid = MyApplication.getLoggedUserId();
//                                FirebaseFirestore.getInstance()
//                                        .collection("users")
//                                        .document(uid)
//                                        .update("fcmToken", token);
//                            });



                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firestore грешка: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );


    }


    private String emailToDocId(String email) {
        return email.replaceAll("[^a-zA-Z0-9]", "_");
    }


    private void onFirestoreUserLoaded(DocumentSnapshot snapshot) {
      if (snapshot.exists()) {
            FirestoreUser fsUser = snapshot.toObject(FirestoreUser.class);
            if (fsUser != null) {
                User user = userDao.getUserByEmail(MyApplication.getLoggedEmail());
                user.setPoints(fsUser.getPoints());
                user.setLives(fsUser.getLives());
                user.setConsecutiveDays(fsUser.getStreak());
                user.setSelectedLanguageCode(fsUser.getSelectedLanguage());
                userDao.update(user);
            }
        }
        User updatedLocal = userDao.getUserByEmail(MyApplication.getLoggedEmail());
        goToNext(updatedLocal);
    }

    private void goToNext(User user) {
        Intent intent;
        if ("admin".equals(user.getRole())) {
            intent = new Intent(this, AdminPanelActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putString("language", langCode)
                .apply();
    }
}

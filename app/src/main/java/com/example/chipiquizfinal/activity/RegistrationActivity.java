package com.example.chipiquizfinal.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.UserHelper;
import com.example.chipiquizfinal.dao.*;
import com.example.chipiquizfinal.entity.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, usernameInput, firstNameInput, lastNameInput;
    private Spinner languageSpinner, practiceSpinner, appLanguageSpinner;
    private Button createProfileBtn;

    private UserDao userDao;
    private ProgrammingLanguageDao languageDao;
    private UserLanguageChoiceDao userLanguageChoiceDao;

    private ImageView profileImageView;
    private Button uploadImageButton;
    private Bitmap selectedBitmap = null;
    private static final int REQUEST_PICK_IMAGE = 1001;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private final String[] langCodes = {"bg", "en"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // DAO
        userDao = MyApplication.getDatabase().userDao();
        languageDao = MyApplication.getDatabase().programmingLanguageDao();
        userLanguageChoiceDao = MyApplication.getDatabase().userLanguageChoiceDao();

        // UI
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        usernameInput = findViewById(R.id.usernameInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        languageSpinner = findViewById(R.id.languageSpinner);
        practiceSpinner = findViewById(R.id.practiceSpinner);
        appLanguageSpinner = findViewById(R.id.appLanguageSpinner);
        createProfileBtn = findViewById(R.id.createProfileBtn);
        profileImageView = findViewById(R.id.profileImageView);
        uploadImageButton = findViewById(R.id.selectImageBtn);

        uploadImageButton.setOnClickListener(v -> openImagePicker());

        // Програмен език
        String[] languages = getResources().getStringArray(R.array.programming_languages_display);
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        languageSpinner.setAdapter(languageAdapter);

        // Практика
        String[] practiceOptions = {"5", "10", "15", "20", "30", "60"};
        ArrayAdapter<String> practiceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, practiceOptions);
        practiceSpinner.setAdapter(practiceAdapter);

        // Език на приложението
        ArrayAdapter<String> appLangAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Български", "English"});
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

        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Моля, попълнете всички полета.", Toast.LENGTH_SHORT).show();
            return;
        }

        int practiceMinutes;
        try {
            practiceMinutes = Integer.parseInt(practiceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Невалиден брой минути.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userDao.getUserByEmail(email) != null) {
            Toast.makeText(this, "Съществува потребител с този имейл.", Toast.LENGTH_SHORT).show();
            return;
        }

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

        // Запази снимката ако има
        if (selectedBitmap != null) {
            // Създаваме папка във вътрешното хранилище, ако липсва
            File dir = new File(getFilesDir(), "profile_images");
            if (!dir.exists()) dir.mkdirs();

            // Избираме име на файла (например от email)
            String imageFileName = email.replaceAll("[^a-zA-Z0-9]", "") + ".png";
            File file = new File(dir, imageFileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                // Записваме PNG
                selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();

                // В setter-а запазваме **абсолютния** път
                user.setProfileImagePath(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Грешка при запис на снимката", Toast.LENGTH_SHORT).show();
            }
        }

        userDao.insert(user);
        User createdUser = userDao.getUserByEmail(email);
        if (createdUser == null) {
            Toast.makeText(this, "Грешка при създаване на профила.", Toast.LENGTH_SHORT).show();
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
        saveUserToCloud(createdUser);
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(UserHelper::updateFcmToken);


        Toast.makeText(this, "Профилът е създаден!", Toast.LENGTH_SHORT).show();
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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = fixImageOrientation(this, uri);
                // 1️⃣ Запазваме избраното изображение в полето
                selectedBitmap = bitmap;
                selectedImageUri = uri;

                // 2️⃣ Показваме го веднага
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Не успя да зареди снимката", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap fixImageOrientation(Context context, Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        input.close();

        // Четем EXIF
        InputStream exifInput = context.getContentResolver().openInputStream(uri);
        ExifInterface exif = new ExifInterface(exifInput);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        exifInput.close();

        int rotationDegrees;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90: rotationDegrees = 90; break;
            case ExifInterface.ORIENTATION_ROTATE_180: rotationDegrees = 180; break;
            case ExifInterface.ORIENTATION_ROTATE_270: rotationDegrees = 270; break;
            default: rotationDegrees = 0; break;
        }

        if (rotationDegrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotated;
        }
        return bitmap;
    }


    private void saveUserToCloud(User user) {
        // 1) Подготвя данните в Map
        Map<String,Object> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("username", user.getUsername());
        data.put("password", user.getPassword());
        data.put("firstName", user.getFirstName());       // ← ново
        data.put("lastName", user.getLastName());         // ← ново
        data.put("language", user.getLanguage());         // ← ново
        data.put("points", user.getPoints());
        data.put("lives", user.getLives());
        data.put("streak", user.getConsecutiveDays());
        data.put("selectedLanguage", user.getSelectedLanguageCode());

        // ако си записал локалния път към снимката:
        data.put("profileImagePath",
                user.getProfileImagePath() != null ? user.getProfileImagePath() : "");
        // и инициално празен списък за приятели
        data.put("friends", new ArrayList<String>());

        // 2) Вкарва в колекцията "users", документът е с името на userId
        String docId = String.valueOf(user.getId());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(docId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    // записът е мина успешно
                    Log.d("CloudSave", "Потребителят е записан в облака: " + docId);
                })
                .addOnFailureListener(e -> {
                    // грешка при запис
                    Log.e("CloudSave", "Грешка при запис в облака", e);
                });
    }

}
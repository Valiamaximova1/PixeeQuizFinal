package com.example.chipiquizfinal.activity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.util.List;

public class ProfileActivity extends BaseActivity {

    private ImageView profileImage;

    private User currentUser;
    private UserDao userDao;

    private TextView usernameText, emailText;
    private EditText newPasswordInput;
    private Button changePasswordBtn, logoutBtn;
    private ImageView qrImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1) Зареждаме layout-а
        setContentView(R.layout.activity_profile);

        // 2) Намираме всички view-та от XML (включително profileImage!)
        profileImage      = findViewById(R.id.profileImage);
        usernameText      = findViewById(R.id.usernameText);
        emailText         = findViewById(R.id.emailText);
        newPasswordInput  = findViewById(R.id.newPasswordInput);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        logoutBtn         = findViewById(R.id.logoutBtn);
        qrImageView       = findViewById(R.id.qrImageView);

        // 3) Инициализираме общия header (spinner, stats, език)
        setupHeader();

        // 4) Зареждаме потребителя
        userDao     = MyApplication.getDatabase().userDao();
        currentUser = userDao.getUserByEmail(MyApplication.getLoggedEmail());
        if (currentUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 5) Попълваме текстовете с инфо за потребителя
        displayUserInfo();

        // 6) Генерираме и показваме QR кода
        generateAndDisplayQRCode();

        // 7) Зареждаме профилната снимка
        loadProfileImage();

        // 8) Смяна на парола
        changePasswordBtn.setOnClickListener(v -> {
            String newPass = newPasswordInput.getText().toString().trim();
            if (newPass.isEmpty()) {
                Toast.makeText(this, "Enter new password", Toast.LENGTH_SHORT).show();
                return;
            }
            currentUser.setPassword(newPass);
            userDao.update(currentUser);
            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
            newPasswordInput.setText("");
        });

        // 9) Навигация
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            return false;
        });

        // Logout
        logoutBtn.setOnClickListener(v -> {
            MyApplication.setLoggedEmail(null);
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });
    }

    /** Зарежда профилната снимка от currentUser.getPhotoUri() или показва placeholder */
    private void loadProfileImage() {
        String path = currentUser.getProfileImagePath();
        Toast.makeText(this, "tova se ebawa smennn" + path, Toast.LENGTH_SHORT).show();

        if (path != null && !path.isEmpty()) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Toast.makeText(this, "tova se ebawa" + R.drawable.ic_profile_placeholder, Toast.LENGTH_SHORT).show();

                // зареждаме локалния файл с Glide
                Glide.with(this)
                        .load(imgFile)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(profileImage);
                return;
            }
        }
        profileImage.setImageResource(R.drawable.ic_profile_placeholder);
        Toast.makeText(this, "kakwo se sluchva" + R.drawable.ic_profile_placeholder, Toast.LENGTH_SHORT).show();
    }
    private void displayUserInfo() {
        usernameText.setText("Username: " + currentUser.getUsername());
        emailText.setText("Email: " + currentUser.getEmail());

    }

    private void generateAndDisplayQRCode() {
        try {
            // Решавайки дали да използваме ID или имейл като съдържание; тук използваме ID
            String uniqueContent = String.valueOf(currentUser.getId());
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            // Генерираме QR код като Bitmap (можеш да промениш размерите според желания)
            Bitmap qrBitmap = barcodeEncoder.encodeBitmap(uniqueContent, BarcodeFormat.QR_CODE, 300, 300);
            qrImageView.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "QR Code generation failed", Toast.LENGTH_SHORT).show();
        }
    }
}

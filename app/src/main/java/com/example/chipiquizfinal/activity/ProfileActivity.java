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
import com.google.firebase.firestore.FirebaseFirestore;
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
        setContentView(R.layout.activity_profile);

        profileImage      = findViewById(R.id.profileImage);
        usernameText      = findViewById(R.id.usernameText);
        emailText         = findViewById(R.id.emailText);
        newPasswordInput  = findViewById(R.id.newPasswordInput);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        logoutBtn         = findViewById(R.id.logoutBtn);
        qrImageView       = findViewById(R.id.qrImageView);

        setupHeader();

        userDao     = MyApplication.getDatabase().userDao();
        currentUser = userDao.getUserByEmail(MyApplication.getLoggedEmail());
        if (currentUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayUserInfo();

        generateAndDisplayQRCode();

        loadProfileImage();
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


            String docId = String.valueOf(currentUser.getId());
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(docId)
                    .update("password", newPass)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Password synced to cloud", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Cloud update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );


        });

        // в onCreate(...)
        Button scanQrBtn = findViewById(R.id.scanQrBtn);
        scanQrBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRScannerActivity.class);
            startActivity(intent);
        });



        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_community) {
                startActivity(new Intent(this, AllUsersActivity.class));
                return true;
            }
            return false;
        });

        logoutBtn.setOnClickListener(v -> {
            MyApplication.setLoggedEmail(null);
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });
    }
    private void loadProfileImage() {
        String path = currentUser.getProfileImagePath();

        if (path != null && !path.isEmpty()) {
            File imgFile = new File(path);
            if (imgFile.exists()) {

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
          }
    private void displayUserInfo() {
        usernameText.setText("Username: " + currentUser.getUsername());
        emailText.setText("Email: " + currentUser.getEmail());

    }

    private void generateAndDisplayQRCode() {
        try {
            String uniqueContent = String.valueOf(currentUser.getId());
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
              Bitmap qrBitmap = barcodeEncoder.encodeBitmap(uniqueContent, BarcodeFormat.QR_CODE, 300, 300);
            qrImageView.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "QR кодът не може да се генерира", Toast.LENGTH_SHORT).show();
        }
    }
}

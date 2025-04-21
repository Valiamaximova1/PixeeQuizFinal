package com.example.chipiquizfinal.helper;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserHelper {
    public static void updateFcmToken(String email, String token) {
        String docId = email.replaceAll("[^A-Za-z0-9]", "_");

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("fcmToken", token);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(docId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }
}

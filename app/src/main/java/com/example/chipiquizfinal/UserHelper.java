package com.example.chipiquizfinal;

import com.google.firebase.firestore.FirebaseFirestore;

public class UserHelper {
    public static void updateFcmToken(String token) {
        String uid = MyApplication.getLoggedEmail(); // или ID
        if (uid==null) return;
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("fcmToken", token);
    }
}

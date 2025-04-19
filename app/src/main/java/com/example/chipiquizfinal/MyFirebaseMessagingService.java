package com.example.chipiquizfinal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // 2.1. Запиши го във Firestore под текущия user.document(firestoreId).set({ fcmToken: token }, merge)
//        FirebaseFirestore.getInstance()
//                .collection("users")
//                .document(MyApplication.getLoggedUserId())
//                .update("fcmToken", token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        super.onMessageReceived(msg);
        String title = msg.getData().get("title");
        String body  = msg.getData().get("body");
        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        String channelId = "friend_requests";
        NotificationChannel ch = new NotificationChannel(channelId, "Заявки за приятелство",
                NotificationManager.IMPORTANCE_HIGH);
        nm.createNotificationChannel(ch);
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);
        nm.notify((int)System.currentTimeMillis(), b.build());
    }
}

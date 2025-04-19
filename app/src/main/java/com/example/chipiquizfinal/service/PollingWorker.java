package com.example.chipiquizfinal.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PollingWorker extends Worker {
    private static final String CHANNEL_ID = "chipiquiz_notifications";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private UserDao userDao = MyApplication.getDatabase().userDao();
    private int myId = userDao.getUserByEmail(MyApplication.getLoggedEmail()).getId();
    private SharedPreferences prefs =
            getApplicationContext().getSharedPreferences("poll_prefs", Context.MODE_PRIVATE);

    public PollingWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
        createNotificationChannel();
    }

    @NonNull @Override
    public Result doWork() {
        checkFriendRequests();
        checkLivesReplenish();
        return Result.success();
    }

    private void checkFriendRequests() {
        long lastTs = prefs.getLong("last_friend_check", 0);
        db.collection("friendships")
                .whereEqualTo("friendId", myId)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(qs -> {
                    long newTs = System.currentTimeMillis();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        long created = doc.getLong("createdAt");
                        if (created > lastTs) {
                            sendNotification(
                                    "Нова заявка за приятелство",
                                    "Имаш нова заявка от " + doc.getString("fromUsername")
                            );
                        }
                    }
                    prefs.edit().putLong("last_friend_check", newTs).apply();
                });
    }

    private void checkLivesReplenish() {
        User u = userDao.getUserByEmail(MyApplication.getLoggedEmail());
        long lastLifeTs = u.getLastLifeTimestamp();
        long now = System.currentTimeMillis();
        if (now - lastLifeTs >= 1000L*60*60) {
            // добавяме един живот
            int newLives = Math.min(5, u.getLives() + 1);
            u.setLives(newLives);
            u.setLastLifeTimestamp(lastLifeTs + 1000L*60*60);
            userDao.update(u);
            sendNotification("Живот възстановен", "Имаш нов живот! Оставащи: " + newLives);
        }
    }

    private void sendNotification(String title, String text) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager nm = ContextCompat.getSystemService(getApplicationContext(), NotificationManager.class);
        nm.notify((int)System.currentTimeMillis(), b.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "ChipiQuiz уведомления",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            ContextCompat.getSystemService(getApplicationContext(), NotificationManager.class)
                    .createNotificationChannel(ch);
        }
    }
}


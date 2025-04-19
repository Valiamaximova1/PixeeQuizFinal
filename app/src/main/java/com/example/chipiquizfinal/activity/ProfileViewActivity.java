package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.FriendshipDao;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.Friendship;
import com.example.chipiquizfinal.entity.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileViewActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private TextView usernameTextView, emailTextView;
    private TextView pointsTextView, livesTextView, streakTextView;
    private Button btnAddFriend;
    private Button btnChat;


    private FirebaseFirestore firestore;
    private FriendshipDao friendshipDao;
    private UserDao userDao;

    private int currentUserId;   // ID на логнатия потребител
    private int viewedUserId;    // ID на профила, който разглеждаме

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        // --- findViewById ---
        profileImageView  = findViewById(R.id.profileImageView);
        usernameTextView  = findViewById(R.id.usernameTextView);
        emailTextView     = findViewById(R.id.emailTextView);
        pointsTextView    = findViewById(R.id.pointsTextView);
        livesTextView     = findViewById(R.id.livesTextView);
        streakTextView    = findViewById(R.id.streakTextView);
        btnAddFriend      = findViewById(R.id.btnAddFriend);
        btnChat = findViewById(R.id.btnChat);


        // --- DAO & Firestore init ---
        firestore     = FirebaseFirestore.getInstance();
        friendshipDao = MyApplication.getDatabase().friendshipDao();
        userDao       = MyApplication.getDatabase().userDao();

        // Вземаме текущия логнат потребител от Room
        String email = MyApplication.getLoggedEmail();
        if (email == null) {
            Toast.makeText(this, "Нямате активен потребител!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        User current = userDao.getUserByEmail(email);
        if (current == null) {
            Toast.makeText(this, "User not found locally!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = current.getId();

        // Прочитаме ID на разглеждания профил от Intent
        viewedUserId = getIntent().getIntExtra("userId", -1);
        if (viewedUserId < 0) {
            Toast.makeText(this, "Няма валиден профил!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Зареждаме данни от Firestore и конфигурираме бутон
        loadUserProfile();
        setupFriendButton();
    }

    private void loadUserProfile() {
        // Път: users/{viewedUserId}
        firestore.collection("users")
                .document(String.valueOf(viewedUserId))
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Профилът не съществува.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    // Попълваме UI
                    String username = doc.getString("username");
                    String email    = doc.getString("email");
                    Long points     = doc.getLong("points");
                    Long lives      = doc.getLong("lives");
                    Long streak     = doc.getLong("streak");
                    String imgUrl   = doc.getString("profileImageUrl");

                    usernameTextView.setText(username != null ? username : "");
                    emailTextView   .setText(email    != null ? email    : "");
                    pointsTextView  .setText("Points: " + (points != null ? points : 0));
                    livesTextView   .setText("Lives: "  + (lives  != null ? lives  : 0));
                    streakTextView  .setText("Streak: " + (streak != null ? streak : 0));

                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imgUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(profileImageView);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Грешка при зареждане: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void setupFriendButton() {
        // Проверяваме съществуващо приятелство/заявка
        Friendship outgoing = friendshipDao.getFriendship(currentUserId, viewedUserId);
        Friendship incoming = friendshipDao.getFriendship(viewedUserId, currentUserId);



        if ((outgoing != null && "ACCEPTED".equals(outgoing.getStatus())) ||
                (incoming != null && "ACCEPTED".equals(incoming.getStatus()))) {

            btnAddFriend.setText("Приятели");
            btnAddFriend.setEnabled(false);

            btnChat.setVisibility(View.VISIBLE);

        } else if (incoming != null && "PENDING".equals(incoming.getStatus())) {

            btnAddFriend.setText("Одобри");
            btnAddFriend.setOnClickListener(v -> {
                incoming.setStatus("ACCEPTED");
                friendshipDao.update(incoming);
                btnAddFriend.setText("Приятели");
                btnAddFriend.setEnabled(false);
            });

        } else if (outgoing != null && "PENDING".equals(outgoing.getStatus())) {

            btnAddFriend.setText("Изчаква...");
            btnAddFriend.setEnabled(false);

        } else {
            btnChat.setVisibility(View.VISIBLE);
            btnAddFriend.setText("Добави приятел");
            btnAddFriend.setOnClickListener(v -> {
                Friendship f = new Friendship();
                f.setUserId(currentUserId);
                f.setFriendId(viewedUserId);
                f.setStatus("PENDING");
                friendshipDao.insert(f);
                btnAddFriend.setText("Изчаква...");
                btnAddFriend.setEnabled(false);
            });
        }

        // ако вече сте приятели – покажи и чака
        btnChat.setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("chatWithUserId", viewedUserId);
            startActivity(i);
        });
    }
}

package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class LeaderboardActivity extends BaseActivity {
    private LinearLayout containerStreak, containerPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        setupHeader();

        containerStreak = findViewById(R.id.containerStreak);
        containerPoints = findViewById(R.id.containerPoints);

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        UserDao userDao = MyApplication.getDatabase().userDao();
        LayoutInflater inflater = LayoutInflater.from(this);

        List<User> topStreak = userDao.getTopByStreak(5);
        containerStreak.removeAllViews();
        for (int i = 0; i < topStreak.size(); i++) {
            User u = topStreak.get(i);
            View row = inflater.inflate(R.layout.item_leaderboard_row, containerStreak, false);
            ImageView avatar = row.findViewById(R.id.avatar);
            TextView name    = row.findViewById(R.id.username);
            TextView value   = row.findViewById(R.id.value);

            name.setText((i+1) + ". " + u.getUsername());
            value.setText(u.getConsecutiveDays() + " дни");
            loadAvatar(u.getProfileImagePath(), avatar);

            containerStreak.addView(row);
        }

        // Топ 5 точки
        List<User> topPoints = userDao.getTopByPoints(5);
        containerPoints.removeAllViews();
        for (int i = 0; i < topPoints.size(); i++) {
            User u = topPoints.get(i);
            View row = inflater.inflate(R.layout.item_leaderboard_row, containerPoints, false);
            ImageView avatar = row.findViewById(R.id.avatar);
            TextView name    = row.findViewById(R.id.username);
            TextView value   = row.findViewById(R.id.value);

            name.setText((i+1) + ". " + u.getUsername());
            value.setText(u.getPoints() + " точки");
            loadAvatar(u.getProfileImagePath(), avatar);

            containerPoints.addView(row);
        }


        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_community) {
                startActivity(new Intent(this, AllUsersActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            else if (id == R.id.nav_map) {
                startActivity(new Intent(this, MapActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadAvatar(String path, ImageView target) {
        if (path != null && !path.isEmpty()) {
            Glide.with(this)
                    .load(path.startsWith("http") ? path : "file://" + path)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(target);
        }
    }
}

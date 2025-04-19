package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.adapter.UserAdapter;
import com.example.chipiquizfinal.dao.FriendshipDao;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Iterator;
import java.util.List;

public class AllUsersActivity extends BaseActivity {

    private RecyclerView recyclerUsers;
    private SearchView searchView;
    private UserAdapter adapter;
    private UserDao userDao;
    private FriendshipDao friendshipDao;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        setupHeader();

        // 1. Инициализираме DAO-тата
        userDao       = MyApplication.getDatabase().userDao();
        friendshipDao = MyApplication.getDatabase().friendshipDao();

        // 2. Опитваме да вземем имейла на текущия потребител
        String email = MyApplication.getLoggedEmail();
        if (email == null) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            email = prefs.getString("logged_email", null);
        }
        if (email == null) {
            Toast.makeText(this, "Не сте логнат!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. Зареждаме User-а от базата
        User me = userDao.getUserByEmail(email);
        if (me == null) {
            Toast.makeText(this, "Потребителят не е намерен!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = me.getId();

        // 4. Настройка на RecyclerView
        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(List.of(), currentUserId, friendshipDao);
        recyclerUsers.setAdapter(adapter);

        // 5. Настройка на SearchView
        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint(getString(R.string.search_users_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                loadUsers(query);
                return true;
            }
            @Override public boolean onQueryTextChange(String query) {
                loadUsers(query);
                return true;
            }
        });

        // 6. Долно навигационно меню
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        // маркираме текущия таб
        bottomNav.setSelectedItemId(R.id.nav_community);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_community) {
                // вече сме тук
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // 7. Първоначално зареждаме всички потребители
        loadUsers("");
    }

    /**
     * Зарежда списък с потребители (филтрирани по query), без текущия user.
     */
    private void loadUsers(String query) {
        List<User> users = userDao.searchUsers(query);
        // махаме текущия
        Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == currentUserId) {
                it.remove();
            }
        }
        adapter.setItems(users);
    }
}

package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chipiquizfinal.MyApplication;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.FriendshipDao;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.Friendship;
import com.example.chipiquizfinal.entity.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
 import com.example.chipiquizfinal.activity.UserAdapter;
import java.util.ArrayList;
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

        // DAO-и
        userDao = MyApplication.getDatabase().userDao();
        friendshipDao = MyApplication.getDatabase().friendshipDao();

        // Текущ потребител
        String email = MyApplication.getLoggedEmail();
        User me = userDao.getUserByEmail(email);
        if (me == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = me.getId();

        // Настройка на RecyclerView
        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(new ArrayList<>(), currentUserId, friendshipDao);
        recyclerUsers.setAdapter(adapter);

        // Настройка на SearchView
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

        // Долно меню
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            return false;
        });
        // Зареждаме всички при първоначално отваряне
        loadUsers("");
    }

    /**
     * Зарежда потребители, филтрирани по query, без текущия user.
     */
    private void loadUsers(String query) {
        List<User> users = userDao.searchUsers(query);
        // Махаме текущия потребител от списъка
        Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == currentUserId) {
                it.remove();
            }
        }
        adapter.setItems(users);
    }
}

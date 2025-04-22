package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.adapter.UserAdapter;
import com.example.chipiquizfinal.dao.FriendshipDao;
import com.example.chipiquizfinal.entity.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AllUsersActivity extends BaseActivity {

    private RecyclerView recyclerUsers;
    private SearchView searchView;
    private UserAdapter adapter;
    private FriendshipDao friendshipDao;
    private int currentUserId;

    private final List<User> cloudUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        setupHeader();

        friendshipDao = MyApplication.getDatabase().friendshipDao();

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
         currentUserId = MyApplication.getDatabase().userDao().getUserByEmail(email).getId();

          recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(new ArrayList<>(), currentUserId, friendshipDao);
        recyclerUsers.setAdapter(adapter);

        // SearchView
        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint(getString(R.string.search_users_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                filterAndShow(query);
                return true;
            }
            @Override public boolean onQueryTextChange(String query) {
                filterAndShow(query);
                return true;
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_community);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return true;
        });


        loadAllUsersFromCloud();
    }

    private void loadAllUsersFromCloud() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(AllUsersActivity.this,
                                    "Грешка при зареждане на потребителите.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        cloudUsers.clear();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            User u = new User();
                            u.setId(Integer.parseInt(doc.getId()));
                            u.setEmail(doc.getString("email"));
                            u.setUsername(doc.getString("username"));
                            u.setProfileImagePath(doc.getString("profileImageUrl"));

                            if (u.getId() != currentUserId) {
                                cloudUsers.add(u);
                            }
                        }

                        adapter.setItems(new ArrayList<>(cloudUsers));
                    }
                });
    }

    private void filterAndShow(String query) {
        if (query == null || query.isEmpty()) {
            adapter.setItems(new ArrayList<>(cloudUsers));
            return;
        }
        List<User> filtered = new ArrayList<>();
        for (User u : cloudUsers) {
            if (u.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(u);
            }
        }
        adapter.setItems(filtered);
    }
}



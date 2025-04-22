package com.example.chipiquizfinal.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.adapter.UserListAdapter;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.User;

import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView userRecyclerView;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        userRecyclerView = findViewById(R.id.userRecyclerView);
        userDao = MyApplication.getDatabase().userDao();

        List<User> users = userDao.getAllUsers();

        UserListAdapter adapter = new UserListAdapter(users);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userRecyclerView.setAdapter(adapter);
    }
}

package com.example.chipiquizfinal.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chipiquizfinal.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);
    @Update
    void update(User user);
    @Delete
    void delete(User user);
    @Query("SELECT * FROM users")
    List<User> getAllUsers();
    @Query("SELECT COUNT(*) FROM users")
    int countUsers();
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    @Query("SELECT * FROM users ORDER BY consecutive_days DESC LIMIT :limit")
    List<User> getTopByStreak(int limit);

    @Query("SELECT * FROM users ORDER BY points DESC LIMIT :limit")
    List<User> getTopByPoints(int limit);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);


    @Query("SELECT * FROM users WHERE username LIKE '%'||:query||'%' OR email LIKE '%'||:query||'%'")
    List<User> searchUsers(String query);
}
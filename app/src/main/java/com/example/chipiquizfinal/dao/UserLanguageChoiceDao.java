package com.example.chipiquizfinal.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.chipiquizfinal.entity.UserLanguageChoice;

import java.util.List;

@Dao
public interface UserLanguageChoiceDao {

    @Insert
    void insert(UserLanguageChoice choice);

    @Query("SELECT * FROM user_language_choices WHERE user_id = :userId LIMIT 1")
    UserLanguageChoice getByUserId(int userId);

    @Query("SELECT * FROM user_language_choices")
    List<UserLanguageChoice> getAll();

    @Query("DELETE FROM user_language_choices")
    void clearAll();
}
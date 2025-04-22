package com.example.chipiquizfinal.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chipiquizfinal.entity.UserProgress;

import java.util.List;

@Dao
public interface UserProgressDao {

    @Insert
    void insert(UserProgress progress);

    @Update
    void update(UserProgress progress);
    @Query("SELECT * FROM user_progress WHERE user_id = :userId")
    List<UserProgress> getByUser(int userId);

    @Query("SELECT * FROM user_progress WHERE user_id = :userId AND exercise_id = :exerciseId LIMIT 1")
    UserProgress getProgress(int userId, int exerciseId);

    @Query("SELECT * FROM user_progress WHERE user_id = :userId AND language_id = :languageId AND is_completed = 1")
    List<UserProgress> getCompletedExercisesForLanguage(int userId, int languageId);

    @Query("SELECT * FROM user_progress WHERE user_id = :userId")
    List<UserProgress> getAllProgressForUser(int userId);
}

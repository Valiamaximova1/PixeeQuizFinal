package com.example.chipiquizfinal.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chipiquizfinal.entity.UserProgress;

import java.util.List;

@Dao
public interface UserExerciseProgressDao {

    @Insert
    void insert(UserProgress progress);

    @Update
    void update(UserProgress progress);

    @Query("SELECT * FROM user_progress WHERE user_id = :userId AND exercise_id = :exerciseId LIMIT 1")
    UserProgress getProgressForExercise(int userId, int exerciseId);

    @Query("SELECT * FROM user_progress WHERE user_id = :userId")
    List<UserProgress> getAllProgressForUser(int userId);

    @Query("SELECT exercise_id FROM user_progress WHERE user_id = :userId AND is_completed = 1")
    List<Integer> getCompletedExerciseIds(int userId);
}

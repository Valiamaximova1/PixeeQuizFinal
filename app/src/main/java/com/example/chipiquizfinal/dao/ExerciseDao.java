package com.example.chipiquizfinal.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chipiquizfinal.entity.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Insert
    void insert(Exercise exercise);

    @Insert
    void insertAll(List<Exercise> exercises);

    @Query("SELECT * FROM exercises WHERE language_id = :languageId AND level = :level ORDER BY position")
    List<Exercise> getExercisesForLevel(int languageId, int level);

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    Exercise getById(int exerciseId);
    @Query("SELECT * FROM exercises WHERE language_id = :langId AND level = :level AND position = :position LIMIT 1")
    Exercise getExerciseByLanguageLevelPosition(int langId, int level, int position);

    @Update
    void update(Exercise exercise);
}

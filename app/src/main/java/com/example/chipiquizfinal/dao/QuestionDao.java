package com.example.chipiquizfinal.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chipiquizfinal.entity.Question;

import java.util.List;

@Dao
public interface QuestionDao {


    @Insert
    long insert(Question question);

    @Update
    void update(Question question);

    @Delete
    void delete(Question question);

    @Query("SELECT * FROM questions WHERE id = :id")
    Question getById(int id);

    @Query("SELECT * FROM questions WHERE exercise_id = :exerciseId")
    List<Question> getQuestionsByExerciseId(int exerciseId);

    @Query("SELECT * FROM questions")
    List<Question> getAllQuestions();

    @Query("SELECT MAX(position) FROM questions WHERE exercise_id = :exerciseId")
    int getLastPositionForExercise(int exerciseId);

    @Query("SELECT * FROM questions WHERE exercise_id = :exerciseId AND position = :position LIMIT 1")
    Question getByExerciseAndPosition(int exerciseId, int position);


}

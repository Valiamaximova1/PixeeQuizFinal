
package com.example.chipiquizfinal.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.chipiquizfinal.entity.QuestionAnswerOption;

import java.util.List;

@Dao
public interface QuestionAnswerOptionDao {

    @Insert
    long insert(QuestionAnswerOption option);

    @Insert
    List<Long> insertAll(List<QuestionAnswerOption> options);

    @Update
    void update(QuestionAnswerOption option);

    @Delete
    void delete(QuestionAnswerOption option);

    @Query("SELECT * FROM question_answer_options WHERE question_id = :questionId")
    List<QuestionAnswerOption> getOptionsForQuestion(int questionId);

    @Query("SELECT * FROM question_answer_options WHERE id = :id")
    QuestionAnswerOption getById(int id);

    @Query("DELETE FROM question_answer_options WHERE question_id = :questionId")
    void deleteByQuestionId(int questionId);
}

package com.example.chipiquizfinal.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.chipiquizfinal.entity.QuestionTranslation;

import java.util.List;

@Dao
public interface QuestionTranslationDao {

    @Insert
    void insert(QuestionTranslation translation);
    @Query("SELECT * FROM question_translations WHERE question_id = :questionId AND language = :langCode LIMIT 1")
    QuestionTranslation getTranslation(int questionId, String langCode);

    @Query("SELECT * FROM question_translations WHERE question_id = :questionId AND language = :language LIMIT 1")
    QuestionTranslation getByQuestionIdAndLanguage(int questionId, String language);

    @Query("SELECT * FROM question_translations WHERE question_id = :questionId")
    List<QuestionTranslation> getAllByQuestionId(int questionId);
}

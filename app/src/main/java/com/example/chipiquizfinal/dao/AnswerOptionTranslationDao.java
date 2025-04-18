package com.example.chipiquizfinal.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.chipiquizfinal.entity.AnswerOptionTranslation;

import java.util.List;

@Dao
public interface AnswerOptionTranslationDao {

    @Insert
    void insert(AnswerOptionTranslation translation);

    @Query("SELECT * FROM answer_option_translations WHERE option_id = :optionId AND language = :language LIMIT 1")
    AnswerOptionTranslation getByOptionIdAndLanguage(int optionId, String language);

       @Query("SELECT * FROM answer_option_translations WHERE option_id = :optionId")
    List<AnswerOptionTranslation> getAllByOptionId(int optionId);
}

package com.example.chipiquizfinal.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.chipiquizfinal.entity.ProgrammingLanguage;

import java.util.List;

@Dao
public interface ProgrammingLanguageDao {

    @Insert
    void insert(ProgrammingLanguage language);

    @Query("SELECT * FROM programming_languages")
    List<ProgrammingLanguage> getAllLanguages();

    @Query("SELECT * FROM programming_languages WHERE id = :id LIMIT 1")
    ProgrammingLanguage getById(int id);

    @Query("SELECT * FROM programming_languages WHERE name = :name LIMIT 1")
    ProgrammingLanguage getByName(String name);
}

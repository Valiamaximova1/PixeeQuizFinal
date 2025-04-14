package com.example.chipiquizfinal.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "programming_languages")
public class ProgrammingLanguage {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    // Гетъри и сетъри

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

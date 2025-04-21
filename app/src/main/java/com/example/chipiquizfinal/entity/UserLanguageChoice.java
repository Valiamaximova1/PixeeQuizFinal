package com.example.chipiquizfinal.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "user_language_choices",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",         // <- това е от User.java
                        childColumns = "user_id",     // <- това е от текущия клас!
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ProgrammingLanguage.class,
                        parentColumns = "id",
                        childColumns = "language_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class UserLanguageChoice {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "language_id")
    private int languageId;

    private int level;

    @ColumnInfo(name = "daily_practice")
    private int dailyPractice;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getLanguageId() { return languageId; }
    public void setLanguageId(int languageId) { this.languageId = languageId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getDailyPractice() { return dailyPractice; }
    public void setDailyPractice(int dailyPractice) { this.dailyPractice = dailyPractice; }
}

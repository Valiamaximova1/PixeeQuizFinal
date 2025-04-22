package com.example.chipiquizfinal.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "exercises",
        foreignKeys = @ForeignKey(
                entity = ProgrammingLanguage.class,
                parentColumns = "id",
                childColumns = "language_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Exercise {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "language_id")
    public int languageId;

    @ColumnInfo(name = "level")
    public int level;

    @ColumnInfo(name = "position")
    public int position;

    @ColumnInfo(name = "is_unlocked")
    public boolean isUnlocked = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLanguageId() {
        return languageId;
    }

    public void setLanguageId(int languageId) {
        this.languageId = languageId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }
}

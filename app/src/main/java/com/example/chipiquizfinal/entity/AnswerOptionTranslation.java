package com.example.chipiquizfinal.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "answer_option_translations",
        foreignKeys = @ForeignKey(
                entity = QuestionAnswerOption.class,
                parentColumns = "id",
                childColumns = "option_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class AnswerOptionTranslation {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "option_id")
    private int optionId;

    @ColumnInfo(name = "language")
    private String language;

    @ColumnInfo(name = "text")
    private String text;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOptionId() {
        return optionId;
    }

    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

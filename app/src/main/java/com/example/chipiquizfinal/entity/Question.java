package com.example.chipiquizfinal.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "questions",
        foreignKeys = {
                @ForeignKey(
                        entity = com.example.chipiquizfinal.entity.Exercise.class,
                        parentColumns = "id",
                        childColumns = "exercise_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = com.example.chipiquizfinal.entity.ProgrammingLanguage.class,
                        parentColumns = "id",
                        childColumns = "language_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class Question {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "question_text")
    private String questionText;
    @ColumnInfo(name = "question_type")
    private String questionType;
    @ColumnInfo(name = "model_path")
    private String modelPath;
    @ColumnInfo(name = "language_id")
    private int languageId;
    @ColumnInfo(name = "level")
    private int level;
    @ColumnInfo(name = "exercise_id")
    private int exerciseId;

    @ColumnInfo(name = "correct_answer_option_id")
    private Integer correctAnswerOptionId;

    @ColumnInfo(name = "position")
    private int position;




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
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

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Integer getCorrectAnswerOptionId() {
        return correctAnswerOptionId;
    }

    public void setCorrectAnswerOptionId(Integer correctAnswerOptionId) {
        this.correctAnswerOptionId = correctAnswerOptionId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

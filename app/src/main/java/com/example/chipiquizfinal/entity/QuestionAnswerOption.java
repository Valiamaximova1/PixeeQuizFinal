package com.example.chipiquizfinal.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
@Entity(
        tableName = "question_answer_options",
        foreignKeys = {
                @ForeignKey(
                        entity = Question.class,
                        parentColumns = "id",
                        childColumns = "question_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class QuestionAnswerOption {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "question_id")
    private int questionId;

    @ColumnInfo(name = "answer_text")
    private String answerText;

    @ColumnInfo(name = "is_correct")
    private boolean isCorrect;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}

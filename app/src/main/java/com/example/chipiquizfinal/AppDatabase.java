package com.example.chipiquizfinal;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.chipiquizfinal.dao.*;
import com.example.chipiquizfinal.entity.*;

@Database(
        entities = {
                User.class,
                ProgrammingLanguage.class,
                UserLanguageChoice.class,
                Exercise.class,
                Question.class,
                QuestionTranslation.class,
                QuestionAnswerOption.class,
                AnswerOptionTranslation.class,
                UserProgress.class,
                Friendship.class
        },
        version = 10,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ProgrammingLanguageDao programmingLanguageDao();
    public abstract UserLanguageChoiceDao userLanguageChoiceDao();
    public abstract ExerciseDao exerciseDao();
    public abstract QuestionDao questionDao();
    public abstract QuestionTranslationDao questionTranslationDao();

    public abstract QuestionAnswerOptionDao questionAnswerOptionDao();
    public abstract AnswerOptionTranslationDao answerOptionTranslationDao();

    public abstract UserProgressDao userProgressDao();
    public abstract FriendshipDao friendshipDao();
}

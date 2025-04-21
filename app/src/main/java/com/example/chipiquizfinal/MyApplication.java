package com.example.chipiquizfinal;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.example.chipiquizfinal.AppDatabase;
import com.example.chipiquizfinal.dao.*;
import com.example.chipiquizfinal.entity.*;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class MyApplication extends Application {
    private static AppDatabase db;
    private static String loggedEmail;
    private static Context appContext;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        FirebaseApp.initializeApp(this);
        Log.d("APP", ">>> MyApplication.onCreate()");

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "chipiquiz-db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        db.getOpenHelper().getWritableDatabase();

        seedQuestionsAndAnswers();
        preloadLanguages();
        seedDefaultUsers();
    }
    public static Context getContext() { return appContext; }
    public static AppDatabase getDatabase() {
        return db;
    }
    public static void setLoggedEmail(String email) {
        loggedEmail = email;
    }
    public static String getLoggedEmail() {
        return loggedEmail;
    }

    private void preloadLanguages() {
        ProgrammingLanguageDao langDao = db.programmingLanguageDao();
        if (langDao.getAllLanguages().isEmpty()) {
            List<String> languages = Arrays.asList(
                    "Java", "C", "C++", "C#", "JavaScript",
                    "HTML & CSS", "Hardware", "Python"
            );
            for (String name : languages) {
                ProgrammingLanguage lang = new ProgrammingLanguage();
                lang.setName(name);
                langDao.insert(lang);
            }
        }
    }
    private void seedQuestionsAndAnswers() {
        Log.d("SEED", ">>> seedQuestionsAndAnswers() start");
        String json = loadJSONFromAsset("questions_with_answers.json");
        if (json.isEmpty()) {
            Log.e("SEED", "JSON от assets не е намерен или е празен!");
            return;
        }

        QuestionDao questionDao = db.questionDao();
        if (questionDao.countQuestions() > 0) {
            Log.d("SEED", "Вече има seed-нати въпроси, пропускам.");
            return;
        }

        ProgrammingLanguageDao langDao = db.programmingLanguageDao();
        ExerciseDao exerciseDao       = db.exerciseDao();
        QuestionTranslationDao qtDao  = db.questionTranslationDao();
        QuestionAnswerOptionDao optDao= db.questionAnswerOptionDao();
        AnswerOptionTranslationDao trDao = db.answerOptionTranslationDao();

        TypeToken<List<QuestionSeed>> typeToken = new TypeToken<List<QuestionSeed>>() {};
        List<QuestionSeed> seeds = new Gson().fromJson(json, typeToken.getType());

        for (QuestionSeed qs : seeds) {
            ProgrammingLanguage lang = langDao.getByName(qs.language);
            if (lang == null) continue;
            Exercise ex = exerciseDao
                    .getExerciseByLanguageLevelPosition(lang.getId(), qs.level, qs.exercise);
            if (ex == null) continue;

            // 1) Insert Question
            Question q = new Question();
            q.setLanguageId(lang.getId());
            q.setLevel(qs.level);
            q.setExerciseId(ex.getId());
            q.setPosition(qs.position);
            q.setQuestionText(qs.textEn);
            q.setQuestionType(qs.type);
            long qid = questionDao.insert(q);

            // 2) Превод на въпроса
            QuestionTranslation qt = new QuestionTranslation();
            qt.setQuestionId((int) qid);
            qt.setLanguage("bg");
            qt.setText(qs.textBg);
            qtDao.insert(qt);

            // 3) Опции и намиране на правилната
            int correctOptionId = -1;
            for (AnswerSeed a : qs.answers) {
                QuestionAnswerOption opt = new QuestionAnswerOption();
                opt.setQuestionId((int) qid);
                opt.setAnswerText(a.textEn);
                opt.setCorrect(a.correct);
                long oid = optDao.insert(opt);
                if (a.correct) correctOptionId = (int) oid;

                // превод на опция
                AnswerOptionTranslation tr = new AnswerOptionTranslation();
                tr.setOptionId((int) oid);
                tr.setLanguage("bg");
                tr.setText(a.textBg);
                trDao.insert(tr);
            }

            // 4) Update Question с correctAnswerOptionId
            if (correctOptionId != -1) {
                q.setId((int) qid);
                q.setCorrectAnswerOptionId(correctOptionId);
                questionDao.update(q);
            }
        }

        Log.d("SEED", "Seed-ването на въпроси и отговори е завършило успешно");
    }
    private String loadJSONFromAsset(String filename) {
        try (InputStream is = getAssets().open(filename)) {
            byte[] buf = new byte[is.available()];
            is.read(buf);
            return new String(buf, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e("SEED", "Грешка при четене на " + filename, e);
            return "";
        }
    }
    private void seedDefaultUsers() {
        UserDao userDao = db.userDao();
        UserLanguageChoiceDao choiceDao = db.userLanguageChoiceDao();
        ProgrammingLanguageDao langDao = db.programmingLanguageDao();

        if (userDao.getUserByEmail("admin") == null) {
            long id = insertUser(userDao,
                    "admin","1234","Admin","Pixee","Admin","admin","Java");
            insertUserLanguageChoice(choiceDao, langDao, id, "Java", 1, 10);
        }
        if (userDao.getUserByEmail("oli") == null) {
            long id = insertUser(userDao,
                    "oli","1234","Olimpia","Olimpia","Maximova","user","Java");
            insertUserLanguageChoice(choiceDao, langDao, id, "Java", 1, 10);
        }
        if (userDao.getUserByEmail("val") == null) {
            long id = insertUser(userDao,
                    "val","1234","Val","Valentina","Maximova","user","Java");
            insertUserLanguageChoice(choiceDao, langDao, id, "Java", 1, 10);
        }
    }
    private long insertUser(UserDao uDao, String email, String pwd,
                            String username, String first, String last,
                            String role, String langName) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(pwd);
        u.setUsername(username);
        u.setFirstName(first);
        u.setLastName(last);
        u.setRole(role);
        u.setLanguage(langName);
        u.setLevel(1);
        u.setPoints(0);
        u.setLives(5);
        u.setConsecutiveDays(0);
        u.setFreezeDay(false);
        u.setDailyPractice(10);
        return uDao.insert(u);
    }
    private void insertUserLanguageChoice(UserLanguageChoiceDao choiceDao,
                                          ProgrammingLanguageDao langDao,
                                          long userId, String langName,
                                          int level, int practice) {
        ProgrammingLanguage lang = langDao.getByName(langName);
        if (lang == null) return;
        UserLanguageChoice c = new UserLanguageChoice();
        c.setUserId((int) userId);
        c.setLanguageId(lang.getId());
        c.setLevel(level);
        c.setDailyPractice(practice);
        choiceDao.insert(c);
    }

    private static class QuestionSeed {
        String language; int level, exercise, position;
        String type, textEn, textBg;
        List<AnswerSeed> answers;
    }
    private static class AnswerSeed {
        String textEn, textBg; boolean correct;
    }
}

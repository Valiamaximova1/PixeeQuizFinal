package com.example.chipiquizfinal;

import static android.content.ContentValues.TAG;

import static com.example.chipiquizfinal.AppDatabase.MIGRATION_1_2;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.chipiquizfinal.AppDatabase;
import com.example.chipiquizfinal.dao.AnswerOptionTranslationDao;
import com.example.chipiquizfinal.dao.ExerciseDao;
import com.example.chipiquizfinal.dao.ProgrammingLanguageDao;
import com.example.chipiquizfinal.dao.QuestionAnswerOptionDao;
import com.example.chipiquizfinal.dao.QuestionDao;
import com.example.chipiquizfinal.dao.QuestionTranslationDao;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.dao.UserLanguageChoiceDao;
import com.example.chipiquizfinal.entity.AnswerOptionTranslation;
import com.example.chipiquizfinal.entity.Exercise;
import com.example.chipiquizfinal.entity.ProgrammingLanguage;
import com.example.chipiquizfinal.entity.Question;
import com.example.chipiquizfinal.entity.QuestionAnswerOption;
import com.example.chipiquizfinal.entity.QuestionTranslation;
import com.example.chipiquizfinal.entity.User;
import com.example.chipiquizfinal.entity.UserLanguageChoice;
import com.google.common.reflect.TypeToken;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class MyApplication extends Application {

    private static AppDatabase db;
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_SEEDED = "questions_seeded";
    private static String loggedEmail;
    private UserDao userDao;
    private QuestionAnswerOptionDao optionDao;
    private QuestionTranslationDao translationDao;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("APP", ">>> MyApplication.onCreate()");
//
//        // 1) Създаваме/отворяме базата
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "chipiquiz-db")
                .fallbackToDestructiveMigration()  // или осигурете реална миграция до v2
                .allowMainThreadQueries()
                .build();

//        db = Room.databaseBuilder(getApplicationContext(),
//                        AppDatabase.class, "chipiquiz-db")
//                .addMigrations(MIGRATION_1_2)
//                .fallbackToDestructiveMigration()
//                .allowMainThreadQueries()
//                // ПРАВИЛНО:
//                .addCallback(new RoomDatabase.Callback() {
//                    @Override
//                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
//                        super.onCreate(db);
//                        // Тук пускаш seedQuestionsAndAnswers() или каквото ти трябва
//                        Executors.newSingleThreadExecutor().execute(() -> {
//                            seedQuestionsAndAnswers();
//                        });
//                    }
//
//                    @Override
//                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
//                        super.onOpen(db);
//                        // при нужда – логика за open
//                    }
//                })
//                .build();




//        db.getOpenHelper().getWritableDatabase();

        // 2) Директно викаме seed, но той вътре ще провери дали вече има въпроси
        seedQuestionsAndAnswers();


        preloadLanguages(); // 👈 Добавяне на езици ако не съществуват

        seedDefaultUsers();
//        userDao = MyApplication.getDatabase().userDao();
//        replenishLives(userDao);

    }

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
        if (db.programmingLanguageDao().getAllLanguages().isEmpty()) {
            List<String> languages = Arrays.asList(
                    "Java", "C", "C++", "C#", "JavaScript", "HTML & CSS", "Hardware", "Python"
            );

            for (String name : languages) {
                ProgrammingLanguage lang = new ProgrammingLanguage();
                lang.setName(name);
                db.programmingLanguageDao().insert(lang);
            }
        }
    }

    private void seedQuestionsAndAnswers() {
        Log.d("SEED", ">>> seedQuestionsAndAnswers() start");

        // Зареждаме JSON
        String json = loadJSONFromAsset("questions_with_answers.json");
        Log.d("SEED", "Loaded JSON length=" + json.length());
        if (json.isEmpty()) {
            Log.e("SEED", "JSON от assets е празен или не намерен!");
            return;
        }

        QuestionDao questionDao = db.questionDao();
        // Ако вече има въпроси, спираме
        if (questionDao.countQuestions() > 0) {
            Log.d("SEED", "Вече има seed-нати въпроси, пропускаме.");
            return;
        }

        ProgrammingLanguageDao langDao = db.programmingLanguageDao();
        ExerciseDao exerciseDao       = db.exerciseDao();
        QuestionTranslationDao qtDao  = db.questionTranslationDao();
        QuestionAnswerOptionDao optDao= db.questionAnswerOptionDao();
        AnswerOptionTranslationDao trDao = db.answerOptionTranslationDao();

        Type listType = new TypeToken<List<QuestionSeed>>(){}.getType();
        List<QuestionSeed> seeds = new Gson().fromJson(json, listType);

        for (QuestionSeed qs : seeds) {
            // Foreign keys
            ProgrammingLanguage lang = langDao.getByName(qs.language);
            if (lang == null) continue;
            Exercise ex = exerciseDao
                    .getExerciseByLanguageLevelPosition(lang.getId(), qs.level, qs.exercise);
            if (ex == null) continue;

            // 1) Insert Question без correctAnswerOptionId
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

            // 3) Insert опции и намиране на правилната
            int correctOptionId = -1;
            for (AnswerSeed a : qs.answers) {
                QuestionAnswerOption opt = new QuestionAnswerOption();
                opt.setQuestionId((int) qid);
                opt.setAnswerText(a.textEn);
                opt.setCorrect(a.correct);
                long oid = optDao.insert(opt);

                if (a.correct) {
                    correctOptionId = (int) oid;
                }

                // превод на опция
                AnswerOptionTranslation tr = new AnswerOptionTranslation();
                tr.setOptionId((int) oid);
                tr.setLanguage("bg");
                tr.setText(a.textBg);
                trDao.insert(tr);
            }

            // 4) Update Question с correctAnswerOptionId
            if (correctOptionId != -1) {
                q.setId((int) qid);  // уверяваме се, че id-то е сетнато
                q.setCorrectAnswerOptionId(correctOptionId);
                questionDao.update(q);
            }
        }

        Log.d("SEED", "Seed-ването на въпроси и отговори е завършило успешно");
    }


    private String loadJSONFromAsset(String filename) {
        try (InputStream is = getAssets().open(filename)) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String s = new String(buffer, StandardCharsets.UTF_8);
            Log.d("SEED", "Loaded JSON of length " + s.length());
            return s;
        } catch (IOException e) {
            Log.e("SEED", "Грешка при четене на " + filename, e);
            return "";
        }
    }

    // Helper seed‑класове
    static class QuestionSeed {
        String language;
        int level;
        int exercise;
        int position;
        String type;
        String textEn;
        String textBg;
        List<AnswerSeed> answers;
    }

    static class AnswerSeed {
        String textEn;
        String textBg;
        boolean correct;
    }

    private void seedDefaultUsers() {
        UserDao userDao = db.userDao();
        ProgrammingLanguageDao langDao = db.programmingLanguageDao();
        UserLanguageChoiceDao choiceDao = db.userLanguageChoiceDao();

        // -------- Admin --------
        if (userDao.getUserByEmail("admin") == null) {
            long adminId = insertUser(userDao,
                    "admin","1234","Admin","Pixee","Admin","admin","Java");
            insertUserLanguageChoice(choiceDao, langDao, adminId, "Java", 1, 10);
        }

        // ------- Olimpia -------
        if (userDao.getUserByEmail("oli") == null) {
            long oliId = insertUser(userDao,
                    "oli","1234","Olimpia","Olimpia","Maximova","user","Java");
            insertUserLanguageChoice(choiceDao, langDao, oliId, "Java", 1, 10);
        }

        // ------- Valentina -------
        if (userDao.getUserByEmail("val") == null) {
            long valId = insertUser(userDao,
                    "val","1234","Val","Valentina","Maximova","user","Java");
            insertUserLanguageChoice(choiceDao, langDao, valId, "Java", 1, 10);
        }
    }

    /** Създава User и връща новото му ID */
    private long insertUser(UserDao userDao,
                            String email,
                            String password,
                            String username,
                            String firstName,
                            String lastName,
                            String role,
                            String languageName) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(password);
        u.setUsername(username);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setRole(role);
        u.setLanguage(languageName);
        u.setLevel(1);
        u.setPoints(0);
        u.setLives(5);
        u.setConsecutiveDays(0);
        u.setFreezeDay(false);
        u.setDailyPractice(10);
        return userDao.insert(u);
    }

    /** Създава запис в user_language_choices */
    private void insertUserLanguageChoice(UserLanguageChoiceDao choiceDao,
                                          ProgrammingLanguageDao langDao,
                                          long userId,
                                          String languageName,
                                          int level,
                                          int dailyPractice) {
        ProgrammingLanguage lang = langDao.getByName(languageName);
        if (lang == null) return;
        UserLanguageChoice choice = new UserLanguageChoice();
        choice.setUserId((int) userId);
        choice.setLanguageId(lang.getId());
        choice.setLevel(level);
        choice.setDailyPractice(dailyPractice);
        choiceDao.insert(choice);
    }

    public static void replenishLives(UserDao userDao, User u) {
        final int MAX_LIVES = 5;
        long now = System.currentTimeMillis();
        long last = u.getLastLifeTimestamp();
        if (last == 0) {
            // инициализираме първоначално
            u.setLastLifeTimestamp(now);
            userDao.update(u);
            return;
        }
        long hoursPassed = (now - last) / (1000 * 60 * 60);
        if (hoursPassed > 0) {
            int newLives = Math.min(MAX_LIVES, u.getLives() + (int)hoursPassed);
            long newTimestamp = last + hoursPassed * (1000 * 60 * 60);
            u.setLives(newLives);
            u.setLastLifeTimestamp(newTimestamp);
            userDao.update(u);
        }
    }

}

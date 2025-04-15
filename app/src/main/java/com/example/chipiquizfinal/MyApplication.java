package com.example.chipiquizfinal;

import android.app.Application;

import androidx.room.Room;

import com.example.chipiquizfinal.AppDatabase;
import com.example.chipiquizfinal.dao.QuestionAnswerOptionDao;
import com.example.chipiquizfinal.dao.QuestionDao;
import com.example.chipiquizfinal.dao.QuestionTranslationDao;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.AnswerOptionTranslation;
import com.example.chipiquizfinal.entity.ProgrammingLanguage;
import com.example.chipiquizfinal.entity.Question;
import com.example.chipiquizfinal.entity.QuestionAnswerOption;
import com.example.chipiquizfinal.entity.QuestionTranslation;
import com.example.chipiquizfinal.entity.User;

import java.util.Arrays;
import java.util.List;

public class MyApplication extends Application {

    private static AppDatabase db;
    private static String loggedEmail;
    private UserDao userDao;
    private QuestionDao questionDao;
    private QuestionAnswerOptionDao optionDao;
    private QuestionTranslationDao translationDao;

    @Override
    public void onCreate() {
        super.onCreate();

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "chipiquiz-db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        db.query("SELECT 1", null); // Принуждава създаване на базата

        preloadLanguages(); // 👈 Добавяне на езици ако не съществуват


        userDao = MyApplication.getDatabase().userDao();

        User admin = new User();
        admin.setEmail("admin");
//        admin.setEmail("admin@pixee.com");
        admin.setPassword("1234");
        admin.setUsername("Admin");
        admin.setFirstName("Pixee");
        admin.setLastName("Admin");
        admin.setRole("admin");
        admin.setLanguage("Java");
        admin.setLevel(1);
        admin.setPoints(0);
        admin.setLives(5);
        admin.setConsecutiveDays(0);
        admin.setFreezeDay(false);
        admin.setDailyPractice(10);


        User oli = new User();
        oli.setEmail("oli");
//        admin.setEmail("oli@pixee.com");
        oli.setPassword("1234");
        oli.setUsername("Olimpia");
        oli.setFirstName("Olimpia");
        oli.setLastName("Maximova");
        oli.setRole("user");
        oli.setLanguage("Hardware");
        oli.setLevel(1);
        oli.setPoints(0);
        oli.setLives(5);
        oli.setConsecutiveDays(0);
        oli.setFreezeDay(false);
        oli.setDailyPractice(10);
        oli.setSelectedLanguageCode("bg");

        User val = new User();
        val.setEmail("val");
//        val.setEmail("val@pixee.com");
        val.setPassword("1234");
        val.setUsername("Val");
        val.setFirstName("Valentina");
        val.setLastName("Maximova");
        val.setRole("user");
        val.setLanguage("C");
        val.setLevel(1);
        val.setPoints(0);
        val.setLives(5);
        val.setConsecutiveDays(0);
        val.setFreezeDay(false);
        val.setDailyPractice(10);
        val.setSelectedLanguageCode("en");



        if (userDao.getUserByEmail(admin.getEmail()) == null) {
            userDao.insert(admin);
        }
        if (userDao.getUserByEmail(oli.getEmail()) == null) {
            userDao.insert(oli);
        }
        if (userDao.getUserByEmail(val.getEmail()) == null) {
            userDao.insert(val);
        }


//        preloadJavaQuestions();
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

//    private void preloadJavaQuestions(){
//
//        questionDao = MyApplication.getDatabase().questionDao();
//        optionDao = MyApplication.getDatabase().questionAnswerOptionDao();
//        translationDao = MyApplication.getDatabase().questionTranslationDao();
//
//        int javaLangId = db.programmingLanguageDao().getByName("Java").getId();
//
//        for (int exercise = 1; exercise <= 10; exercise++) {
//            for (int i = 0; i < questionList.size(); i++) {
//                Question q = new Question();
//                q.setLanguageId(javaLangId);
//                q.setLevel(1);
//                q.setExerciseId(db.exerciseDao().getExerciseByLanguageLevelPosition(javaLangId, 1, exercise).getId());
//                q.setPosition(i + 1);
//                q.setQuestionText(questionList.get(i).question_text_en);
//                q.setQuestionType("multiple choice");
//
//                long questionId = db.questionDao().insert(q);
//
//                for (int j = 0; j < 4; j++) {
//                    QuestionAnswerOption option = new QuestionAnswerOption();
//                    option.setQuestionId((int) questionId);
//                    option.setAnswerText(questionList.get(i).answers_en[j]);
//                    option.setCorrect(j == questionList.get(i).correct_index);
//                    long optionId = db.questionAnswerOptionDao().insert(option);
//
//                    // Превод на отговорите
//                    AnswerOptionTranslation tr = new AnswerOptionTranslation();
//                    tr.setAnswerOptionId((int) optionId);
//                    tr.setLanguageCode("bg");
//                    tr.setText(questionList.get(i).answers_bg[j]);
//                    db.answerOptionTranslationDao().insert(tr);
//
//                    // Задай правилен отговор
//                    if (j == questionList.get(i).correct_index) {
//                        q.setCorrectAnswerOptionId((int) optionId);
//                        db.questionDao().update(q);
//                    }
//                }
//
//                // Превод на въпроса
//                QuestionTranslation translation = new QuestionTranslation();
//                translation.setQuestionId((int) questionId);
//                translation.setLanguage("bg");
//                translation.setText(questionList.get(i).question_text_bg);
//                db.questionTranslationDao().insert(translation);
//            }
//        }
//
//
//    }
}

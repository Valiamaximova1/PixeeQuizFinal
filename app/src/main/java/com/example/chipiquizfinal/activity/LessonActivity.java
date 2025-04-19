package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.QuestionAnswerOptionDao;
import com.example.chipiquizfinal.dao.QuestionDao;
import com.example.chipiquizfinal.dao.QuestionTranslationDao;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.Question;
import com.example.chipiquizfinal.entity.QuestionAnswerOption;
import com.example.chipiquizfinal.entity.QuestionTranslation;
import com.example.chipiquizfinal.entity.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;

public class LessonActivity extends BaseActivity {
    private TextView questionTextView;
    private Button[] answerButtons;
    private ProgressBar progressBar;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private User currentUser;
    private QuestionDao questionDao;
    private QuestionAnswerOptionDao optionDao;
    private QuestionTranslationDao transDao;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        setupHeader();  // важно!

        // 1) Инициализация на UI
        questionTextView = findViewById(R.id.questionText);
        progressBar      = findViewById(R.id.progressBar);
        answerButtons    = new Button[] {
                findViewById(R.id.answerButton1),
                findViewById(R.id.answerButton2),
                findViewById(R.id.answerButton3),
                findViewById(R.id.answerButton4)
        };

        // 2) DAO-и и потребител
        userDao     = MyApplication.getDatabase().userDao();
        questionDao = MyApplication.getDatabase().questionDao();
        optionDao   = MyApplication.getDatabase().questionAnswerOptionDao();
        transDao    = MyApplication.getDatabase().questionTranslationDao();

        String email = MyApplication.getLoggedEmail();
        currentUser = userDao.getUserByEmail(email);
        if (currentUser == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3) Вземаме ID на упражнение
        int exerciseId = getIntent().getIntExtra("exerciseId", -1);
        if (exerciseId < 0) {
            Toast.makeText(this, "Missing exerciseId!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
//        MyApplication.replenishLives(userDao, currentUser);
        refreshHeaderStats();
        // 4) Зареждаме въпросите
        questionList = questionDao.getQuestionsByExerciseId(exerciseId);
        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(this,
                    "Няма въпроси за това упражнение.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Подреждаме по position и взимаме първите 10, ако са повече
        Collections.sort(questionList,
                (q1, q2) -> Integer.compare(q1.getPosition(), q2.getPosition()));
        if (questionList.size() > 10) {
            questionList = questionList.subList(0, 10);
        }

        // 5) Настройваме ProgressBar
        progressBar.setMax(questionList.size());
        progressBar.setIndeterminate(false);

        // 6) Показваме първия въпрос
        displayQuestion();
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questionList.size()) {
            completeExercise();
            return;
        }

        // Обновяваме прогреса
        progressBar.setProgress(currentQuestionIndex + 1);

        Question q = questionList.get(currentQuestionIndex);
        String lang = getSharedPreferences("prefs", MODE_PRIVATE)
                .getString("lang", "bg");

        // Текст на въпроса (превод)
        QuestionTranslation qt = transDao.getTranslation(q.getId(), lang);
        questionTextView.setText(
                qt != null ? qt.getText() : q.getQuestionText()
        );

        // Зареждаме и разбъркваме опциите
        List<QuestionAnswerOption> opts =
                optionDao.getOptionsForQuestion(q.getId());
        Collections.shuffle(opts);

        // Задаваме бутоните
        for (int i = 0; i < answerButtons.length; i++) {
            Button btn = answerButtons[i];
            if (i < opts.size()) {
                final QuestionAnswerOption opt = opts.get(i);
                btn.setText(opt.getAnswerText());
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(v -> checkAnswer(opt));
            } else {
                btn.setVisibility(View.GONE);
            }
        }
    }

    private void checkAnswer(@NonNull QuestionAnswerOption selected) {
        Question question = questionList.get(currentQuestionIndex);
        boolean correct = selected.getId() == question.getCorrectAnswerOptionId();

        new AlertDialog.Builder(this)
                .setTitle(correct ? "✅ Вярно!" : "❌ Грешно!")
                .setPositiveButton("OK", (dialog, which) -> {
                    if (correct) {
                        currentQuestionIndex++;
                        displayQuestion();
                    } else {
                        // Намаляваме живот
                        int lives = currentUser.getLives();
                        if (lives > 0) {
                            currentUser.setLives(lives - 1);
                            // Обновяваме timestamp, за да почне часовникът за дозареждане
                            currentUser.setLastLifeTimestamp(System.currentTimeMillis());
                            userDao.update(currentUser);
                            updateLivesInCloud(currentUser);
                            refreshHeaderStats();
                        }
                        if (currentUser.getLives() == 0) {
                            // Няма животи – прекъсваме
                            Toast.makeText(this,
                                    "Нямате повече животи – упражнението се прекъсва.",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(this,
                                    "Оставащи животи: " + currentUser.getLives(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void completeExercise() {
        // добавяме прогрес, точки и се връщаме
        // ... същата логика като преди ...
        finish();
    }

    // helper за обновяване в Firestore
    private void updateLivesInCloud(User user) {
        String docId = String.valueOf(user.getId());
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(docId)
                .update(
                        "lives", user.getLives(),
                        "lastLifeTimestamp", user.getLastLifeTimestamp()
                )
                .addOnFailureListener(e -> Log.e("Firestore", "Неуспешен ъпдейт на lives: " + e.getMessage()));
    }
}

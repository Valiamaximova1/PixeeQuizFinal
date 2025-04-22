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
import com.example.chipiquizfinal.dao.ExerciseDao;
import com.example.chipiquizfinal.dao.QuestionAnswerOptionDao;
import com.example.chipiquizfinal.dao.QuestionDao;
import com.example.chipiquizfinal.dao.QuestionTranslationDao;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.dao.UserProgressDao;
import com.example.chipiquizfinal.entity.Exercise;
import com.example.chipiquizfinal.entity.Question;
import com.example.chipiquizfinal.entity.QuestionAnswerOption;
import com.example.chipiquizfinal.entity.QuestionTranslation;
import com.example.chipiquizfinal.entity.User;
import com.example.chipiquizfinal.entity.UserProgress;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LessonActivity extends BaseActivity {
    private TextView questionTextView;
    private Button[] answerButtons;
    private ProgressBar progressBar;
    private int currentExerciseId;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private User currentUser;
    private QuestionDao questionDao;
    private QuestionAnswerOptionDao optionDao;
    private QuestionTranslationDao transDao;
    private UserDao userDao;
    private UserProgressDao progressDao;
    private ExerciseDao exerciseDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        setupHeader();

         questionTextView = findViewById(R.id.questionText);
        progressBar      = findViewById(R.id.progressBar);
        answerButtons    = new Button[] {
                findViewById(R.id.answerButton1),
                findViewById(R.id.answerButton2),
                findViewById(R.id.answerButton3),
                findViewById(R.id.answerButton4)
        };

        currentExerciseId = getIntent().getIntExtra("exerciseId", -1);
        if (currentExerciseId < 0) {
            Toast.makeText(this, "Няма упражнение!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userDao     = MyApplication.getDatabase().userDao();
        questionDao = MyApplication.getDatabase().questionDao();
        optionDao   = MyApplication.getDatabase().questionAnswerOptionDao();
        transDao    = MyApplication.getDatabase().questionTranslationDao();
        exerciseDao = MyApplication.getDatabase().exerciseDao();
        progressDao = MyApplication.getDatabase().userProgressDao();

        String email = MyApplication.getLoggedEmail();
        currentUser = userDao.getUserByEmail(email);
        if (currentUser == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int exerciseId = getIntent().getIntExtra("exerciseId", -1);
        if (exerciseId < 0) {
            Toast.makeText(this, "Missing exerciseId!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        refreshHeaderStats();



        questionList = questionDao.getQuestionsByExerciseId(exerciseId);
        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(this,
                    "Няма въпроси за това упражнение.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Collections.sort(questionList,
                (q1, q2) -> Integer.compare(q1.getPosition(), q2.getPosition()));
        if (questionList.size() > 10) {
            questionList = questionList.subList(0, 10);
        }

        progressBar.setMax(questionList.size());
        progressBar.setIndeterminate(false);

        displayQuestion();
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questionList.size()) {
            completeExercise();
            return;
        }

        progressBar.setProgress(currentQuestionIndex + 1);

        Question q = questionList.get(currentQuestionIndex);
        String lang = getSharedPreferences("prefs", MODE_PRIVATE)
                .getString("lang", "bg");

        QuestionTranslation qt = transDao.getTranslation(q.getId(), lang);
        questionTextView.setText(
                qt != null ? qt.getText() : q.getQuestionText()
        );

        List<QuestionAnswerOption> opts =
                optionDao.getOptionsForQuestion(q.getId());
        Collections.shuffle(opts);

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

                        int lives = currentUser.getLives();
                        if (lives > 0) {
                            currentUser.setLives(lives - 1);
                            currentUser.setLastLifeTimestamp(System.currentTimeMillis());
                            userDao.update(currentUser);
                            updateLivesInCloud(currentUser);
                            refreshHeaderStats();
                        }
                        if (currentUser.getLives() == 0) {
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
        String email = MyApplication.getLoggedEmail();
        UserDao userDao = MyApplication.getDatabase().userDao();
        User user = userDao.getUserByEmail(email);
        Exercise ex = exerciseDao.getById(currentExerciseId);
        if (user == null || ex == null) {
            Toast.makeText(this, "Не е намерен потребител или упражнение", Toast.LENGTH_SHORT).show();
            return;
        }



        UserProgress progress = new UserProgress();
        progress.setUserId(user.getId());
        progress.setExerciseId(currentExerciseId);
        progress.languageId  = ex.getLanguageId();
        progress.setCompletedAt(System.currentTimeMillis());
        progressDao.insert(progress);

        int pointsToAdd = 10;
        user.setPoints(user.getPoints() + pointsToAdd);

        long todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        if (user.getLastExerciseDate() < todayStart) {
            user.setConsecutiveDays(user.getConsecutiveDays() + 1);
            user.setLastExerciseDate(System.currentTimeMillis());
        }

        userDao.update(user);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(String.valueOf(user.getId()))
                .update(new HashMap<String,Object>() {{
                    put("points", user.getPoints());
                    put("streak", user.getConsecutiveDays());
                }})
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "+" + pointsToAdd + " точки!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Грешка при синхронизация: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }



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

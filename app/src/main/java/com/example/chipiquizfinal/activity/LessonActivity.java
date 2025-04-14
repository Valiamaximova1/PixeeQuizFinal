package com.example.chipiquizfinal.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.*;
import com.example.chipiquizfinal.entity.*;

import java.util.Collections;
import java.util.List;

public class LessonActivity extends AppCompatActivity {

    private TextView questionTextView;
    private Button[] answerButtons;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;

    private User currentUser;
    private int exerciseId;

    private QuestionDao questionDao;
    private QuestionAnswerOptionDao optionDao;
    private UserDao userDao;
    private QuestionTranslationDao translationDao;
    private UserProgressDao progressDao;

    private  UserLanguageChoice choice;
    private  UserLanguageChoiceDao userLanguageChoiceDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        questionTextView = findViewById(R.id.questionText);
        answerButtons = new Button[]{
                findViewById(R.id.answerButton1),
                findViewById(R.id.answerButton2),
                findViewById(R.id.answerButton3),
                findViewById(R.id.answerButton4)
        };

        questionDao = MyApplication.getDatabase().questionDao();
        optionDao = MyApplication.getDatabase().questionAnswerOptionDao();
        userDao = MyApplication.getDatabase().userDao();
        translationDao = MyApplication.getDatabase().questionTranslationDao();
        progressDao = MyApplication.getDatabase().userProgressDao();
        userLanguageChoiceDao = MyApplication.getDatabase().userLanguageChoiceDao();

        // 1. Вземи потребителя ПЪРВО
        currentUser = userDao.getUserByEmail(MyApplication.getLoggedEmail());

        if (currentUser == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Сега може да вземеш езиковия избор
        choice = userLanguageChoiceDao.getByUserId(currentUser.getId());

        exerciseId = getIntent().getIntExtra("exerciseId", -1);
        if (exerciseId == -1) {
            Toast.makeText(this, "Missing exercise ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        questionList = questionDao.getQuestionsByExerciseId(exerciseId);
        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(this, "Няма въпроси за това упражнение.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayQuestion();

//        ProgressBar loader = findViewById(R.id.loader);
//        loader.setVisibility(View.GONE);

    }


    private void displayQuestion() {
        if (currentQuestionIndex >= questionList.size()) {
            completeExercise();
            return;
        }

        Question currentQuestion = questionList.get(currentQuestionIndex);

        // превод на въпрос
        String langCode = getSharedPreferences("prefs", MODE_PRIVATE).getString("lang", "en");

        QuestionTranslation translated = translationDao.getTranslation(currentQuestion.getId(), langCode);
        questionTextView.setText(translated != null ? translated.getText() : currentQuestion.getQuestionText());

        // отговори
        List<QuestionAnswerOption> options = optionDao.getOptionsForQuestion(currentQuestion.getId());
        Collections.shuffle(options);

        for (int i = 0; i < answerButtons.length; i++) {
            if (i < options.size()) {
                QuestionAnswerOption option = options.get(i);

                // превод на отговор
                QuestionTranslation optionTranslation = translationDao.getTranslation(option.getId(), langCode);
                answerButtons[i].setText(optionTranslation != null ? optionTranslation.getText() : option.getAnswerText());

                answerButtons[i].setVisibility(Button.VISIBLE);
                answerButtons[i].setOnClickListener(v -> checkAnswer(option));
            } else {
                answerButtons[i].setVisibility(Button.GONE);
            }
        }
    }

    private void checkAnswer(QuestionAnswerOption selectedOption) {
        Question currentQuestion = questionList.get(currentQuestionIndex);
        boolean isCorrect = selectedOption.getId() == currentQuestion.getCorrectAnswerOptionId();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isCorrect ? "✅ Вярно!" : "❌ Грешно!");
        builder.setMessage(isCorrect ? "Поздравления!" : "Опитай пак.");
        builder.setPositiveButton("Продължи", (dialog, which) -> {
            if (isCorrect) {
                currentQuestionIndex++;
                displayQuestion();
            } else {
                int lives = currentUser.getLives();
                if (lives > 0) {
                    currentUser.setLives(lives - 1);
                    userDao.update(currentUser);
                }
                Toast.makeText(this, "Оставащи животи: " + currentUser.getLives(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void completeExercise() {
        // добави прогрес
        UserProgress progress = new UserProgress();
        progress.setUserId(currentUser.getId());
        progress.setExerciseId(exerciseId);
        progress.setCompleted(true);

        if (choice != null) {
            progress.setLanguageId(choice.getLanguageId());
        } else {
            Toast.makeText(this, "User has no language set.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDao.insert(progress);
        // добави точки
        currentUser.setPoints(currentUser.getPoints() + 10);
        userDao.update(currentUser);

        Toast.makeText(this, "Упражнението е завършено! +10 точки", Toast.LENGTH_LONG).show();
        finish(); // връща към MainActivity
    }
}

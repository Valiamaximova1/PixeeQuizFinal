package com.example.chipiquizfinal.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.*;
import com.example.chipiquizfinal.entity.*;

import java.util.Collections;
import java.util.List;

public class ExerciseActivity extends BaseActivity {

    private TextView exerciseTitle, questionText;
    private RadioGroup answerGroup;
    private Button nextBtn;

    private QuestionDao questionDao;
    private QuestionAnswerOptionDao optionDao;
    private QuestionTranslationDao questionTransDao;
    private AnswerOptionTranslationDao answerTransDao;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int exerciseId;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        setupHeader();  // ако искаш header тук също

        exerciseId = getIntent().getIntExtra("exercise_id", -1);
        if (exerciseId == -1) {
            Toast.makeText(this, "Missing exercise ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        exerciseTitle = findViewById(R.id.exerciseTitle);
        questionText  = findViewById(R.id.questionText);
        answerGroup   = findViewById(R.id.answerGroup);
        nextBtn       = findViewById(R.id.nextBtn);

        questionDao      = MyApplication.getDatabase().questionDao();
        optionDao        = MyApplication.getDatabase().questionAnswerOptionDao();
        questionTransDao = MyApplication.getDatabase().questionTranslationDao();
        answerTransDao   = MyApplication.getDatabase().answerOptionTranslationDao();

        exerciseTitle.setText(getString(R.string.exercise_title, exerciseId));

        questionList = questionDao.getQuestionsByExerciseId(exerciseId);
        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(this,
                    "No questions for this exercise.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Collections.sort(questionList,
                (q1, q2) -> Integer.compare(q1.getPosition(), q2.getPosition()));
        if (questionList.size() > 10) {
            questionList = questionList.subList(0, 10);
        }

        loadQuestion();

        nextBtn.setOnClickListener(v -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < questionList.size()) {
                loadQuestion();
            } else {
                Toast.makeText(this,
                        getString(R.string.exercise_completed),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadQuestion() {
        Question q = questionList.get(currentQuestionIndex);

        String lang = getSharedPreferences("prefs", MODE_PRIVATE)
                .getString("lang", "bg");

        QuestionTranslation qt =
                questionTransDao.getTranslation(q.getId(), lang);
        questionText.setText(
                qt != null ? qt.getText() : q.getQuestionText()
        );

        answerGroup.removeAllViews();
        List<QuestionAnswerOption> opts =
                optionDao.getOptionsForQuestion(q.getId());
        Collections.shuffle(opts);
        for (QuestionAnswerOption opt : opts) {
            AnswerOptionTranslation tr =
                    answerTransDao.getByOptionIdAndLanguage(opt.getId(), lang);

            RadioButton rb = new RadioButton(this);
            rb.setText(tr != null ? tr.getText() : opt.getAnswerText());
            rb.setTextSize(16f);
            rb.setPadding(8, 16, 8, 16);
            answerGroup.addView(rb);
        }
    }
}

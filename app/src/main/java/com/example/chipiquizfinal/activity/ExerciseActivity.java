package com.example.chipiquizfinal.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.QuestionAnswerOptionDao;
import com.example.chipiquizfinal.dao.QuestionDao;
import com.example.chipiquizfinal.entity.Question;
import com.example.chipiquizfinal.entity.QuestionAnswerOption;

import java.util.List;

public class ExerciseActivity extends AppCompatActivity {

    private TextView exerciseTitle, questionText;
    private RadioGroup answerGroup;
    private Button nextBtn;

    private QuestionDao questionDao;
    private QuestionAnswerOptionDao optionDao;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        int exerciseId = getIntent().getIntExtra("exercise_id", -1);
        if (exerciseId == -1) {
            Toast.makeText(this, "Missing exercise ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        exerciseTitle = findViewById(R.id.exerciseTitle);
        questionText = findViewById(R.id.questionText);
        answerGroup = findViewById(R.id.answerGroup);
        nextBtn = findViewById(R.id.nextBtn);

        questionDao = MyApplication.getDatabase().questionDao();
        optionDao = MyApplication.getDatabase().questionAnswerOptionDao();

        questionList = questionDao.getQuestionsByExerciseId(exerciseId);
        exerciseTitle.setText("Exercise #" + exerciseId);

        if (questionList.isEmpty()) {
            Toast.makeText(this, "No questions for this exercise.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadQuestion();

        nextBtn.setOnClickListener(v -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < questionList.size()) {
                loadQuestion();
            } else {
                Toast.makeText(this, "Exercise completed!", Toast.LENGTH_SHORT).show();
                finish(); // или можеш да стартираш екрана за резултати
            }
        });
    }

    private void loadQuestion() {
        Question q = questionList.get(currentQuestionIndex);
        questionText.setText(q.getQuestionText());
        answerGroup.removeAllViews();

        List<QuestionAnswerOption> options = optionDao.getOptionsForQuestion(q.getId());
        for (QuestionAnswerOption option : options) {
            RadioButton rb = new RadioButton(this);
            rb.setText(option.getAnswerText());
            rb.setTextSize(16f);
            rb.setPadding(8, 16, 8, 16);
            answerGroup.addView(rb);
        }
    }
}

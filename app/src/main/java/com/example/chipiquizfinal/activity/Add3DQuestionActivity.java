package com.example.chipiquizfinal.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.QuestionAnswerOptionDao;
import com.example.chipiquizfinal.dao.QuestionDao;
import com.example.chipiquizfinal.entity.Question;
import com.example.chipiquizfinal.entity.QuestionAnswerOption;

public class Add3DQuestionActivity extends AppCompatActivity {

    private EditText questionText, modelPathInput, answerInput;
    private Button saveButton;

    private QuestionDao questionDao;
    private QuestionAnswerOptionDao optionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_3d_question);

        questionDao = MyApplication.getDatabase().questionDao();
        optionDao = MyApplication.getDatabase().questionAnswerOptionDao();

        questionText = findViewById(R.id.questionText);
        modelPathInput = findViewById(R.id.modelPathInput);
        answerInput = findViewById(R.id.answerInput);
        saveButton = findViewById(R.id.save3DQuestionBtn);

        saveButton.setOnClickListener(v -> save3DQuestion());
    }

    private void save3DQuestion() {
        String question = questionText.getText().toString().trim();
        String modelPath = modelPathInput.getText().toString().trim();
        String correctAnswer = answerInput.getText().toString().trim();

        if (question.isEmpty() || modelPath.isEmpty() || correctAnswer.isEmpty()) {
            Toast.makeText(this, "Моля, попълни всички полета.", Toast.LENGTH_SHORT).show();
            return;
        }

        Question q = new Question();
        q.setQuestionText(question);
        q.setQuestionType("3d");
        q.setModelPath(modelPath);
        q.setLanguageId(1); // Java например
        q.setLevel(1);
        q.setExerciseId(1);
        q.setPosition(1);

        long questionId = questionDao.insert(q);

        QuestionAnswerOption option = new QuestionAnswerOption();
        option.setQuestionId((int) questionId);
        option.setAnswerText(correctAnswer);
        option.setCorrect(true);
        optionDao.insert(option);

        Toast.makeText(this, "Въпросът с 3D модел е добавен!", Toast.LENGTH_SHORT).show();
        finish();
    }
}

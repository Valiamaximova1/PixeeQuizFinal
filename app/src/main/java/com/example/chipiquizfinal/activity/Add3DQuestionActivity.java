package com.example.chipiquizfinal.activity;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.*;
import com.example.chipiquizfinal.entity.*;

public class Add3DQuestionActivity extends AppCompatActivity {

    private EditText questionTextEn, questionTextBg, modelPathInput;
    private EditText[] answersEn = new EditText[4];
    private EditText[] answersBg = new EditText[4];
    private EditText levelInput, exerciseInput;
    private RadioGroup correctAnswerGroup;
    private Button save3DQuestionBtn;

    private QuestionDao questionDao;
    private QuestionTranslationDao questionTranslationDao;
    private QuestionAnswerOptionDao optionDao;
    private AnswerOptionTranslationDao optionTranslationDao;
    private ExerciseDao exerciseDao;
    private ProgrammingLanguageDao languageDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_3d_question);

        questionDao = MyApplication.getDatabase().questionDao();
        questionTranslationDao = MyApplication.getDatabase().questionTranslationDao();
        optionDao = MyApplication.getDatabase().questionAnswerOptionDao();
        optionTranslationDao = MyApplication.getDatabase().answerOptionTranslationDao();
        exerciseDao = MyApplication.getDatabase().exerciseDao();
        languageDao = MyApplication.getDatabase().programmingLanguageDao();

        modelPathInput = findViewById(R.id.modelPathInput);
        questionTextEn = findViewById(R.id.questionTextEn);
        questionTextBg = findViewById(R.id.questionTextBg);

        answersEn[0] = findViewById(R.id.answer1En);
        answersEn[1] = findViewById(R.id.answer2En);
        answersEn[2] = findViewById(R.id.answer3En);
        answersEn[3] = findViewById(R.id.answer4En);

        answersBg[0] = findViewById(R.id.answer1Bg);
        answersBg[1] = findViewById(R.id.answer2Bg);
        answersBg[2] = findViewById(R.id.answer3Bg);
        answersBg[3] = findViewById(R.id.answer4Bg);

        correctAnswerGroup = findViewById(R.id.correctAnswerRadioGroup);
        levelInput = findViewById(R.id.levelInput);
        exerciseInput = findViewById(R.id.exerciseInput);
        save3DQuestionBtn = findViewById(R.id.save3DQuestionBtn);

        save3DQuestionBtn.setOnClickListener(v -> saveQuestion());
    }

    private void saveQuestion() {
        String textEn = questionTextEn.getText().toString().trim();
        String textBg = questionTextBg.getText().toString().trim();
        String modelPath = modelPathInput.getText().toString().trim();
        String levelStr = levelInput.getText().toString().trim();
        String exerciseStr = exerciseInput.getText().toString().trim();

        if (textEn.isEmpty() || textBg.isEmpty() || modelPath.isEmpty() || levelStr.isEmpty() || exerciseStr.isEmpty()) {
            Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = correctAnswerGroup.getCheckedRadioButtonId();
        int correctIndex = -1;
        if (selectedId == R.id.correct1) correctIndex = 0;
        else if (selectedId == R.id.correct2) correctIndex = 1;
        else if (selectedId == R.id.correct3) correctIndex = 2;
        else if (selectedId == R.id.correct4) correctIndex = 3;

        if (correctIndex == -1) {
            Toast.makeText(this, "Please select a correct answer!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] answersEnText = new String[4];
        String[] answersBgText = new String[4];

        for (int i = 0; i < 4; i++) {
            answersEnText[i] = answersEn[i].getText().toString().trim();
            answersBgText[i] = answersBg[i].getText().toString().trim();
            if (answersEnText[i].isEmpty() || answersBgText[i].isEmpty()) {
                Toast.makeText(this, "Fill all answer fields!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int level = Integer.parseInt(levelStr);
        int position = Integer.parseInt(exerciseStr);

        ProgrammingLanguage hardwareLang = languageDao.getByName("Hardware");
        if (hardwareLang == null) {
            Toast.makeText(this, "Hardware language not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Exercise ex = exerciseDao.getExerciseByLanguageLevelPosition(hardwareLang.getId(), level, position);
        if (ex == null) {
            Toast.makeText(this, "Exercise not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Question question = new Question();
        question.setQuestionText(textEn);
        question.setQuestionType("3d");
        question.setLanguageId(hardwareLang.getId());
        question.setLevel(level);
        question.setExerciseId(ex.getId());
        question.setModelPath(modelPath);
        int lastPos = questionDao.getLastPositionForExercise(ex.getId());
        question.setPosition(lastPos + 1);
        long questionId = questionDao.insert(question);

        // Превод на въпроса
        QuestionTranslation translation = new QuestionTranslation();
        translation.setQuestionId((int) questionId);
        translation.setLanguage("bg");
        translation.setText(textBg);
        questionTranslationDao.insert(translation);

        int correctOptionId = -1;
        for (int i = 0; i < 4; i++) {
            QuestionAnswerOption option = new QuestionAnswerOption();
            option.setQuestionId((int) questionId);
            option.setAnswerText(answersEnText[i]);
            option.setCorrect(i == correctIndex);
            long optionId = optionDao.insert(option);

            AnswerOptionTranslation trans = new AnswerOptionTranslation();
            trans.setOptionId((int) optionId);
            trans.setLanguage("bg");
            trans.setText(answersBgText[i]);
            optionTranslationDao.insert(trans);

            if (i == correctIndex) {
                correctOptionId = (int) optionId;
            }
        }

        question.setCorrectAnswerOptionId(correctOptionId);
        questionDao.update(question);

        Toast.makeText(this, "3D въпросът е запазен!", Toast.LENGTH_SHORT).show();
        finish();
    }
}

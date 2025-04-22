package com.example.chipiquizfinal.activity;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.*;
import com.example.chipiquizfinal.entity.*;

import java.util.List;

public class AddMultipleChoiceQuestionActivity extends AppCompatActivity {

    private EditText questionInputEn, questionInputBg;
    private EditText[] answerInputsEn = new EditText[4];
    private EditText[] answerInputsBg = new EditText[4];
    private RadioGroup correctAnswerGroup;
    private Spinner languageSpinner;
    private EditText levelInput, exerciseInput;
    private Button saveQuestionBtn;

    private QuestionDao questionDao;
    private QuestionTranslationDao translationDao;
    private QuestionAnswerOptionDao optionDao;
    private AnswerOptionTranslationDao optionTranslationDao;
    private ExerciseDao exerciseDao;
    private ProgrammingLanguageDao languageDao;
    private List<ProgrammingLanguage> languageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_multiple_choice_question);

        questionDao = MyApplication.getDatabase().questionDao();
        translationDao = MyApplication.getDatabase().questionTranslationDao();
        optionDao = MyApplication.getDatabase().questionAnswerOptionDao();
        optionTranslationDao = MyApplication.getDatabase().answerOptionTranslationDao();
        exerciseDao = MyApplication.getDatabase().exerciseDao();
        languageDao = MyApplication.getDatabase().programmingLanguageDao();

        questionInputEn = findViewById(R.id.questionInputEn);
        questionInputBg = findViewById(R.id.questionInputBg);

        answerInputsEn[0] = findViewById(R.id.answer1En);
        answerInputsEn[1] = findViewById(R.id.answer2En);
        answerInputsEn[2] = findViewById(R.id.answer3En);
        answerInputsEn[3] = findViewById(R.id.answer4En);

        answerInputsBg[0] = findViewById(R.id.answer1Bg);
        answerInputsBg[1] = findViewById(R.id.answer2Bg);
        answerInputsBg[2] = findViewById(R.id.answer3Bg);
        answerInputsBg[3] = findViewById(R.id.answer4Bg);

        correctAnswerGroup = findViewById(R.id.correctAnswerRadioGroup);
        levelInput = findViewById(R.id.levelInput);
        exerciseInput = findViewById(R.id.exerciseInput);
        languageSpinner = findViewById(R.id.languageSpinner);
        saveQuestionBtn = findViewById(R.id.saveQuestionBtn);

        setupLanguageSpinner();
        saveQuestionBtn.setOnClickListener(v -> saveQuestion());
    }

    private void setupLanguageSpinner() {
        languageList = languageDao.getAllLanguages();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getLanguageNames(languageList)
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
    }

    private String[] getLanguageNames(List<ProgrammingLanguage> languages) {
        String[] names = new String[languages.size()];
        for (int i = 0; i < languages.size(); i++) {
            names[i] = languages.get(i).getName();
        }
        return names;
    }

    private void saveQuestion() {
        String questionEn = questionInputEn.getText().toString().trim();
        String questionBg = questionInputBg.getText().toString().trim();
        String levelStr = levelInput.getText().toString().trim();
        String exerciseStr = exerciseInput.getText().toString().trim();

        if (questionEn.isEmpty() || questionBg.isEmpty() || levelStr.isEmpty() || exerciseStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all question and metadata fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = correctAnswerGroup.getCheckedRadioButtonId();
        int correctIndex = -1;
        if (selectedId == R.id.correct1) correctIndex = 0;
        else if (selectedId == R.id.correct2) correctIndex = 1;
        else if (selectedId == R.id.correct3) correctIndex = 2;
        else if (selectedId == R.id.correct4) correctIndex = 3;

        if (correctIndex == -1) {
            Toast.makeText(this, "Please select a correct answer.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] answersEn = new String[4];
        String[] answersBg = new String[4];
        for (int i = 0; i < 4; i++) {
            answersEn[i] = answerInputsEn[i].getText().toString().trim();
            answersBg[i] = answerInputsBg[i].getText().toString().trim();
            if (answersEn[i].isEmpty() || answersBg[i].isEmpty()) {
                Toast.makeText(this, "Fill all answer fields!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int level = Integer.parseInt(levelStr);
        int exercisePosition = Integer.parseInt(exerciseStr);
        ProgrammingLanguage selectedLang = languageList.get(languageSpinner.getSelectedItemPosition());
        Exercise ex = exerciseDao.getExerciseByLanguageLevelPosition(selectedLang.getId(), level, exercisePosition);

        if (ex == null) {
            Toast.makeText(this, "Exercise not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Question question = new Question();
        question.setQuestionText(questionEn);
        question.setQuestionType("multiple choice");
        question.setLanguageId(selectedLang.getId());
        question.setLevel(level);
        question.setExerciseId(ex.getId());
        int lastPos = questionDao.getLastPositionForExercise(ex.getId());
        question.setPosition(lastPos + 1);
        long questionId = questionDao.insert(question);

        QuestionTranslation translation = new QuestionTranslation();
        translation.setQuestionId((int) questionId);
        translation.setLanguage("bg");
        translation.setText(questionBg);
        translationDao.insert(translation);

        int correctOptionId = -1;
        for (int i = 0; i < 4; i++) {
            QuestionAnswerOption option = new QuestionAnswerOption();
            option.setQuestionId((int) questionId);
            option.setAnswerText(answersEn[i]);
            option.setCorrect(i == correctIndex);
            long optionId = optionDao.insert(option);

            AnswerOptionTranslation trans = new AnswerOptionTranslation();
            trans.setOptionId((int) optionId);
            trans.setLanguage("bg");
            trans.setText(answersBg[i]);
            optionTranslationDao.insert(trans);

            if (i == correctIndex) {
                correctOptionId = (int) optionId;
            }
        }

        question.setId((int) questionId);
        question.setCorrectAnswerOptionId(correctOptionId);
        questionDao.update(question);

        Toast.makeText(this, "Question saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}

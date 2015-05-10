package bohonos.demski.mieldzioc.fillingSurvey;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;

import bohonos.demski.mieldzioc.application.ApplicationState;
import bohonos.demski.mieldzioc.constraints.IConstraint;
import bohonos.demski.mieldzioc.constraints.NumberConstraint;
import bohonos.demski.mieldzioc.constraints.TextConstraint;
import bohonos.demski.mieldzioc.controls.AnsweringSurveyControl;
import bohonos.demski.mieldzioc.creatingAndEditingSurvey.R;
import bohonos.demski.mieldzioc.questions.Question;
import bohonos.demski.mieldzioc.questions.TextQuestion;

public class AnswerTextQuestionActivity extends ActionBarActivity {

    private AnsweringSurveyControl answeringSurveyControl = ApplicationState.getInstance(this).
            getAnsweringSurveyControl();
    private Question question;
    private EditText answer;
    private int myQuestionNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_text_question);

        myQuestionNumber = getIntent().getIntExtra("QUESTION_NUMBER", 0);
        question = answeringSurveyControl.getQuestion(myQuestionNumber);
        Log.d("WYPELNIANIE_ANKIETY", AnswerTextQuestionActivity.class + " nr pytania: " +
                myQuestionNumber);
        if(!question.isObligatory()) {
            TextView obligatoryText = (TextView) findViewById(R.id.answer_obligatory_short_text);
            obligatoryText.setVisibility(View.INVISIBLE);
        }

        TextView questionText = (TextView) findViewById(R.id.answer_question_short_text);
        questionText.setText(question.getQuestion());

        TextView questionHint = (TextView) findViewById(R.id.answer_hint_short_text);
        questionHint.setText(question.getHint());

        answer = (EditText) findViewById(R.id.answer_answer_short_text);
        int maxLength = -1;
        TextQuestion txtQuestion = (TextQuestion) question;
        IConstraint constraint = txtQuestion.getConstraint();
        if(constraint != null) {
            if (constraint instanceof NumberConstraint) {
                NumberConstraint numberConstraint = (NumberConstraint) constraint;
                if(numberConstraint.isMustBeInteger()){
                    answer.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                else answer.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
            else if (constraint instanceof TextConstraint) {
                TextConstraint textConstraint = (TextConstraint) constraint;
                if (textConstraint.getMaxLength() != null)
                    maxLength = textConstraint.getMaxLength();
            }
        }
        if(maxLength == -1) maxLength = TextQuestion.SHORT_ANSWER_MAX_LENGTH;
        answer.setFilters(new InputFilter[]{new InputFilter.LengthFilter
                (maxLength)});

        ImageButton nextButton = (ImageButton) findViewById(R.id.next_question_button);
        Button finishButton = (Button) findViewById(R.id.end_filling_button);
        if(answeringSurveyControl.getNumberOfQuestions() - 1 > myQuestionNumber) {  //je�li to nie jest ostatnie pytanie
            finishButton.setVisibility(View.INVISIBLE);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (setUserAnswer())
                        goToNextActivity();
                }
            });
        }
        else{
            nextButton.setVisibility(View.INVISIBLE);
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (setUserAnswer()){
                        if(answeringSurveyControl.finishAnswering(ApplicationState.
                                getInstance(getApplicationContext()).getSurveysRepository())){
                            Intent intent = new Intent(AnswerTextQuestionActivity.this, SurveysSummary.class);
                            intent.putExtra("SURVEY_SUMMARY", getIntent().getStringExtra("SURVEY_SUMMARY"));
                            startActivity(intent);
                            finish();
                        }
                        else Toast.makeText(getApplicationContext(), "Nie mo�na zako�czy� ankiety", Toast.LENGTH_SHORT);
                    }
                }
            });
        }
    }


    private void goToNextActivity() {
        AnsweringSurveyControl control = ApplicationState.
                getInstance(AnswerTextQuestionActivity.this).getAnsweringSurveyControl();
        if(control.getNumberOfQuestions() - 1 > myQuestionNumber){
            Question question = control.getQuestion(myQuestionNumber + 1);
            int questionType = question.getQuestionType();
            Intent intent;
            if(questionType == Question.ONE_CHOICE_QUESTION){
                intent = new Intent(AnswerTextQuestionActivity.this,
                        AnswerOneChoiceQuestionActivity.class);
            }
            else if(questionType == Question.MULTIPLE_CHOICE_QUESTION){
                intent = new Intent(AnswerTextQuestionActivity.this,
                        AnswerMultipleChoiceQuestionActivity.class);
            }
            else if(questionType == Question.DROP_DOWN_QUESTION){
                intent = new Intent(AnswerTextQuestionActivity.this, AnswerDropDownListQuestionActivity.class);
            }
            else if(questionType == Question.SCALE_QUESTION){
                intent = new Intent(AnswerTextQuestionActivity.this, AnswerScaleQuestionActivity.class);
            }
            else if(questionType == Question.DATE_QUESTION){
                intent = new Intent(AnswerTextQuestionActivity.this, AnswerDateQuestionActivity.class);
            }
            else if(questionType == Question.TIME_QUESTION){
                intent = new Intent(AnswerTextQuestionActivity.this, AnswerTimeQuestionActivity.class);
            }
            else if(questionType == Question.GRID_QUESTION){
                intent = new Intent(AnswerTextQuestionActivity.this, AnswerGridQuestionActivity.class);
            }
            else if(questionType == Question.TEXT_QUESTION){
                intent = new Intent(AnswerTextQuestionActivity.this, AnswerTextQuestionActivity.class);
            }
            else intent = new Intent(AnswerTextQuestionActivity.this, SurveysSummary.class);
            intent.putExtra("QUESTION_NUMBER", myQuestionNumber + 1);
            intent.putExtra("SURVEY_SUMMARY", getIntent().getStringExtra("SURVEY_SUMMARY"));
            startActivity(intent);
        }
    }

    private boolean setUserAnswer(){
        AnsweringSurveyControl control = answeringSurveyControl;
        if(question.isObligatory()){
            if(answer == null || answer.getText().toString().trim().equals("")){ //je�li pytanie jest obowi�zkowe i nic nie dodano
                answer.setError("To pytanie jest obowi�zkowe, podaj odpowied�!");
                return false;
            }
        }
        if(answer != null && ! (answer.getText().toString().trim().equals(""))){
            String ans = answer.getText().toString();
            if(control.setTextQuestionAnswer(myQuestionNumber, ans))
                return true;
            else{
                TextQuestion textQuestion = (TextQuestion) question;
                if(textQuestion.getConstraint() instanceof  NumberConstraint){
                    NumberConstraint numberConstraint = (NumberConstraint) textQuestion.getConstraint();
                    Double toCheck = 0.0;
                    try{
                        toCheck = Double.valueOf(ans);
                    }
                    catch(NumberFormatException e){
                        answer.setError("Odpowied� powinna by� liczb�.");
                        return false;
                    }
                    if(numberConstraint.getNotEquals() != null){
                        if(toCheck.equals(numberConstraint.getNotEquals() )){
                            answer.setError("Odpowied� musi by� r�na od " + numberConstraint.getNotEquals());
                            return false;
                        }
                    }
                    if(! numberConstraint.isNotBetweenMaxAndMinValue()){
                        if(numberConstraint.getMinValue() != null){
                            if(toCheck.compareTo(numberConstraint.getMinValue()) < 0){
                                answer.setError("Odpowied� musi by� wi�ksza od " + numberConstraint.getMinValue());
                                return false;
                            }
                        }
                        if(numberConstraint.getMaxValue() != null){
                            if(toCheck.compareTo(numberConstraint.getMaxValue()) > 0){
                                answer.setError("Odpowied� musi by� wi�ksza od " + numberConstraint.getMaxValue());
                                return false;
                            }
                        }
                    }
                    else{
                        if(numberConstraint.getMinValue() != null && numberConstraint.getMaxValue() != null){
                            if(toCheck.compareTo(numberConstraint.getMinValue()) >= 0
                                    && toCheck.compareTo(numberConstraint.getMaxValue()) <= 0){
                                answer.setError("Odpowied� nie powinna si� znajdowa� pomi�dzy " +
                                        numberConstraint.getMinValue() + " i " + numberConstraint.getMaxValue());
                                return false;
                            }
                        }
                        else{
                            if(numberConstraint.getMinValue() != null){
                                if(toCheck.compareTo(numberConstraint.getMinValue()) >= 0){
                                    answer.setError("Odpowied� powinna by� mniejsza od " +
                                            numberConstraint.getMinValue());
                                    return false;
                                }
                            }
                            if(numberConstraint.getMaxValue() != null){
                                if(toCheck.compareTo(numberConstraint.getMaxValue()) <= 0){
                                    answer.setError("Odpowied� powinna by� wi�ksza od " +
                                            numberConstraint.getMaxValue());
                                    return false;
                                }
                            }
                        }
                    }
                    if(numberConstraint.isMustBeInteger()){
                        if((toCheck % (toCheck.intValue())) != 0){
                            answer.setError("Odpowied� powinna by� liczb� ca�kowit�.");
                            return false;
                        }
                    }
                }
                else if(textQuestion.getConstraint() instanceof TextConstraint){
                    TextConstraint textConstraint = (TextConstraint) textQuestion.getConstraint();
                    if(textConstraint.getMinLength() != null){
                        if(ans.length() < textConstraint.getMinLength()){
                            answer.setError("Odpowied� powinna mie� co najmniej " + textConstraint.getMinLength()
                            + " znak�w");
                            return false;
                        }
                    }
                    if(textConstraint.getRegex() != null){
                        Matcher matcher = textConstraint.getRegex().matcher(ans);
                        if(!matcher.matches()){
                            answer.setError("Odpowied� powinna by� postaci: " +
                                    textConstraint.getRegex().pattern());
                            return false;
                        }
                    }
                }
                answer.setError("Nie mo�na doda� odpowiedzi - nieznany b��d");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_answer_short_text_question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

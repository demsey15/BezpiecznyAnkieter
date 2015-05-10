package bohonos.demski.mieldzioc.fillingSurvey;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bohonos.demski.mieldzioc.application.ApplicationState;
import bohonos.demski.mieldzioc.controls.AnsweringSurveyControl;
import bohonos.demski.mieldzioc.creatingAndEditingSurvey.GenerateId;
import bohonos.demski.mieldzioc.creatingAndEditingSurvey.R;
import bohonos.demski.mieldzioc.questions.Question;

public class AnswerOneChoiceQuestionActivity extends ActionBarActivity {

    private AnsweringSurveyControl answeringSurveyControl = ApplicationState.getInstance(this).
            getAnsweringSurveyControl();
    private Question question;
    private List<Button> answers = new ArrayList<>();
    private Button chosenAnswer;
    private int myQuestionNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_one_choice_question);

        myQuestionNumber = getIntent().getIntExtra("QUESTION_NUMBER", 0);
        question = answeringSurveyControl.getQuestion(myQuestionNumber);
        Log.d("WYPELNIANIE_ANKIETY", AnswerOneChoiceQuestionActivity.class + " nr pytania: " +
                myQuestionNumber);
        if(!question.isObligatory()) {
            TextView obligatoryText = (TextView) findViewById(R.id.answer_obligatory_one_choice);
            obligatoryText.setVisibility(View.INVISIBLE);
        }

        TextView questionText = (TextView) findViewById(R.id.answer_question_one_choice);
        questionText.setText(question.getQuestion());

        TextView questionHint = (TextView) findViewById(R.id.answer_hint_one_choice);
        questionHint.setText(question.getHint());

        List<String> answerList = question.getAnswersAsStringList();

        LinearLayout answersLinear = (LinearLayout) findViewById(R.id.answer_list_answers_one_choice);

        for(String ans : answerList){
            Button button = new Button(this);
            button.setText(ans);
            button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setId(GenerateId.generateViewId());
            button.setBackgroundColor(getResources().getColor(R.color.pomaranczowy));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {  //po klikni�ciu zmie� kolor odpowiedzi na czarny (
                    // reszt� na ponara�czowy)
                    if (chosenAnswer != null && chosenAnswer.getId() == ((Button) v).getId()) { //je�li klikni�to na zaznaczon� ju� odpowied�
                        v.setBackgroundColor(getResources().getColor(R.color.pomaranczowy)); //odznacz j�
                        Log.d("WYPELNIANIE_ANKIETY", "Odznaczam odpowiedz jednokrotnego wyboru");
                        chosenAnswer = null;
                    } else {
                        if (chosenAnswer != null) {     //je�eli jaka� odpowied� ju� jest zaznaczona,
                            for (Button butt : answers) {        //odznacz j�
                                butt.setBackgroundColor(getResources().getColor(R.color.pomaranczowy));
                            }
                        }
                        v.setBackgroundColor(getResources().getColor(R.color.black)); //zaznacz wybran� odpowied�
                        chosenAnswer = (Button) v;
                    }
                }
            });
            answers.add(button);
            answersLinear.addView(button);
        }

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
                            Intent intent = new Intent(AnswerOneChoiceQuestionActivity.this, SurveysSummary.class);
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
                getInstance(AnswerOneChoiceQuestionActivity.this).getAnsweringSurveyControl();
        if(control.getNumberOfQuestions() - 1 > myQuestionNumber){
            Question question = control.getQuestion(myQuestionNumber + 1);
            int questionType = question.getQuestionType();
            Intent intent;
            if(questionType == Question.ONE_CHOICE_QUESTION){
                intent = new Intent(AnswerOneChoiceQuestionActivity.this,
                        AnswerOneChoiceQuestionActivity.class);
            }
            else if(questionType == Question.MULTIPLE_CHOICE_QUESTION){
                intent = new Intent(AnswerOneChoiceQuestionActivity.this,
                        AnswerMultipleChoiceQuestionActivity.class);
            }
            else if(questionType == Question.DROP_DOWN_QUESTION){
                intent = new Intent(AnswerOneChoiceQuestionActivity.this, AnswerDropDownListQuestionActivity.class);
            }
            else if(questionType == Question.SCALE_QUESTION){
                intent = new Intent(AnswerOneChoiceQuestionActivity.this, AnswerScaleQuestionActivity.class);
            }
            else if(questionType == Question.DATE_QUESTION){
                intent = new Intent(AnswerOneChoiceQuestionActivity.this, AnswerDateQuestionActivity.class);
            }
            else if(questionType == Question.TIME_QUESTION){
                intent = new Intent(AnswerOneChoiceQuestionActivity.this, AnswerTimeQuestionActivity.class);
            }
            else if(questionType == Question.GRID_QUESTION){
                intent = new Intent(AnswerOneChoiceQuestionActivity.this, AnswerGridQuestionActivity.class);
            }
            else if(questionType == Question.TEXT_QUESTION){
                intent = new Intent(AnswerOneChoiceQuestionActivity.this, AnswerTextQuestionActivity.class);
            }
            else intent = new Intent(AnswerOneChoiceQuestionActivity.this, SurveysSummary.class);
            intent.putExtra("QUESTION_NUMBER", myQuestionNumber + 1);
            intent.putExtra("SURVEY_SUMMARY", getIntent().getStringExtra("SURVEY_SUMMARY"));
            startActivity(intent);
        }
    }

    private boolean setUserAnswer(){
        AnsweringSurveyControl control = ApplicationState.
                getInstance(AnswerOneChoiceQuestionActivity.this).getAnsweringSurveyControl();
        if(question.isObligatory()){
            if(chosenAnswer != null){     //je�li pytanie jest obowi�zkowe i nic nie dodano
                Toast.makeText(AnswerOneChoiceQuestionActivity.this,
                        "To pytanie jest obowi�zkowe, podaj odpowied�!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if(chosenAnswer != null){
            if(control.setOneChoiceQuestionAnswer(myQuestionNumber, chosenAnswer.getText().toString()))
                return true;
            else{
                Toast.makeText(AnswerOneChoiceQuestionActivity.this,
                        "Co� posz�o nie tak, nie dodano odpowiedzi.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_answer_one_choice_question, menu);
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

package bohonos.demski.mieldzioc.fillingSurvey;

import android.content.Intent;
import android.support.v7.app.ActionBar;
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
import bohonos.demski.mieldzioc.application.NetworkIssuesControl;
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

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(answeringSurveyControl.getSurveysTitle());


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
            button.setBackgroundColor(getResources().getColor(R.color.odpowiedz_button));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {  //po kliknięciu zmień kolor odpowiedzi na czarny (
                    // resztę na ponarańczowy)
                    if (chosenAnswer != null && chosenAnswer.getId() == ((Button) v).getId()) { //jeśli kliknięto na zaznaczoną już odpowiedź
                        v.setBackgroundColor(getResources().getColor(R.color.odpowiedz_button)); //odznacz ją
                        Log.d("WYPELNIANIE_ANKIETY", "Odznaczam odpowiedz jednokrotnego wyboru");
                        chosenAnswer = null;
                    } else {
                        if (chosenAnswer != null) {     //jeżeli jakaś odpowiedź już jest zaznaczona,
                            for (Button butt : answers) {        //odznacz ją
                                butt.setBackgroundColor(getResources().getColor(R.color.odpowiedz_button));
                            }
                        }
                        v.setBackgroundColor(getResources().getColor(R.color.chosen_answer_button)); //zaznacz wybraną odpowiedź
                        chosenAnswer = (Button) v;
                    }
                }
            });
            answers.add(button);
            answersLinear.addView(button);
        }

        ImageButton nextButton = (ImageButton) findViewById(R.id.next_question_button);
        Button finishButton = (Button) findViewById(R.id.end_filling_button);
        Button finishAndStartButton = (Button) findViewById(R.id.end_and_start_filling_button);
        if(answeringSurveyControl.getNumberOfQuestions() - 1 > myQuestionNumber) {  //jeśli to nie jest ostatnie pytanie
            finishButton.setVisibility(View.INVISIBLE);
            finishAndStartButton.setVisibility(View.INVISIBLE);
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
                    if (setUserAnswer()) {
                        if (answeringSurveyControl.finishAnswering(ApplicationState.
                                getInstance(getApplicationContext()).getSurveysRepository())) {
                            Intent intent = new Intent(AnswerOneChoiceQuestionActivity.this, SurveysSummary.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("SURVEY_SUMMARY", getIntent().getStringExtra("SURVEY_SUMMARY"));
                            startActivity(intent);
                            finish();
                        } else
                            Toast.makeText(getApplicationContext(), "Nie można zakończyć ankiety", Toast.LENGTH_SHORT);
                    }
                }
            });
            finishAndStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (setUserAnswer()) {
                        String idOfSurveys = answeringSurveyControl.getIdOfSurveysFillingSurvey(); //id wypełnianej ankiety
                        if (answeringSurveyControl.finishAnswering(ApplicationState.
                                getInstance(getApplicationContext()).getSurveysRepository())) {
                            Intent intent = new Intent(getApplicationContext(), WelcomeFillingActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            answeringSurveyControl.startAnswering(idOfSurveys,          //rozpocznij wypełnianie nowej ankiety
                                    ApplicationState.getInstance(getApplicationContext()).getLoggedInterviewer());
                            intent.putExtra("SURVEY_TITLE", answeringSurveyControl.getSurveysTitle());
                            intent.putExtra("SURVEY_DESCRIPTION", answeringSurveyControl.getSurveysDescription());
                            intent.putExtra("SURVEY_SUMMARY", answeringSurveyControl.getSurveysSummary());
                            startActivity(intent);
                            finish();
                        } else
                            Toast.makeText(getApplicationContext(), "Nie można zakończyć ankiety", Toast.LENGTH_SHORT);
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
            else{
                intent = new Intent(AnswerOneChoiceQuestionActivity.this, SurveysSummary.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            intent.putExtra("QUESTION_NUMBER", myQuestionNumber + 1);
            intent.putExtra("SURVEY_SUMMARY", getIntent().getStringExtra("SURVEY_SUMMARY"));
            startActivity(intent);
        }
    }

    private boolean setUserAnswer(){
        AnsweringSurveyControl control = ApplicationState.
                getInstance(AnswerOneChoiceQuestionActivity.this).getAnsweringSurveyControl();
        if(question.isObligatory()){
            if(chosenAnswer == null){     //jeśli pytanie jest obowiązkowe i nic nie dodano
                Toast.makeText(AnswerOneChoiceQuestionActivity.this,
                        "To pytanie jest obowiązkowe, podaj odpowiedź!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if(chosenAnswer != null){
            if(control.setOneChoiceQuestionAnswer(myQuestionNumber, chosenAnswer.getText().toString()))
                return true;
            else{
                Toast.makeText(AnswerOneChoiceQuestionActivity.this,
                        "Coś poszło nie tak, nie dodano odpowiedzi.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem autoSending = menu.findItem(R.id.auto_sending);
        NetworkIssuesControl networkIssuesControl = new NetworkIssuesControl(getApplicationContext());
        if(autoSending == null) Log.d("NULL_DD", "null");
        if(networkIssuesControl.isNetworkAvailable()){
            ApplicationState applicationState = ApplicationState.getInstance(getApplicationContext());
            if(applicationState.isAutoSending()) {
                autoSending.setIcon(R.drawable.send_green);
                autoSending.setTitle("Ustawione automatyczne wysyłanie wypełnionych ankiet.");
            }
            else{
                autoSending.setIcon(R.drawable.send_red);
                autoSending.setTitle("Nie ustawiono automatycznego wysyłania wypełnionych ankiet.");
            }
        }
        else{
            autoSending.setIcon(R.drawable.send_inactive);
            autoSending.setTitle("Automatyczne wysyłanie niemożliwe - brak połączenia z internetem.");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.auto_sending:
                NetworkIssuesControl networkIssuesControl = new NetworkIssuesControl(getApplicationContext());
                ApplicationState applicationState = ApplicationState.getInstance(getApplicationContext());
                if (networkIssuesControl.isNetworkAvailable()) {
                    if (applicationState.isAutoSending()) {
                        applicationState.changeAutoSending();
                        item.setIcon(R.drawable.send_red);
                        item.setTitle("Nie ustawiono automatycznego wysyłania wypełnionych ankiet.");
                    } else {
                        applicationState.changeAutoSending();
                        item.setIcon(R.drawable.send_green);
                        item.setTitle("Ustawione automatyczne wysyłanie wypełnionych ankiet.");
                    }
                } else {
                    item.setIcon(R.drawable.send_inactive);
                    item.setTitle("Automatyczne wysyłanie niemożliwe - brak połączenia z internetem.");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

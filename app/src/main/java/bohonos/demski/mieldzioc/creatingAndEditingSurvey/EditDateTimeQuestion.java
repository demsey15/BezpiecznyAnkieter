package bohonos.demski.mieldzioc.creatingAndEditingSurvey;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import bohonos.demski.mieldzioc.controls.CreatingSurveyControl;
import bohonos.demski.mieldzioc.questions.Question;


public class EditDateTimeQuestion extends ActionBarActivity {

    private Question question;
    private int questionNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_date_time_question);
        Intent intent = getIntent();
        question = (Question) intent.getSerializableExtra("QUESTION");
        questionNumber = intent.getIntExtra("QUESTION_NUMBER", 0);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        final EditText titleTxt = (EditText) findViewById(R.id.question_date_time_text);
        final EditText hintTxt = (EditText) findViewById(R.id.hint_date_time_text);
        final EditText errorTxt = (EditText) findViewById(R.id.error_date_time_text);
        final CheckBox obligatory = (CheckBox) findViewById(R.id.obligatory_checkbox);
        Button addButton = (Button) findViewById(R.id.add_data_time_question);

        if(question.getQuestion() != null) titleTxt.setText(question.getQuestion());
        if(question.getHint() != null) hintTxt.setText(question.getHint());
        if(question.getErrorMessage() != null) errorTxt.setText(question.getErrorMessage());
        obligatory.setChecked(question.isObligatory());

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreatingSurveyControl control = CreatingSurveyControl.getInstance();
                control.setQuestionText(questionNumber, titleTxt.getText().toString());
                control.setQuestionHint(questionNumber, hintTxt.getText().toString());
                control.setQuestionErrorMessage(questionNumber, errorTxt.getText().toString());
                control.setQuestionObligatory(questionNumber, obligatory.isChecked());
                setResult(RESULT_OK);
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_date_time_question, menu);
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

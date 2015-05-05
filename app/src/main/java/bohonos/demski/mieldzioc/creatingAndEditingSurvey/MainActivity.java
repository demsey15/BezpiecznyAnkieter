package bohonos.demski.mieldzioc.creatingAndEditingSurvey;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.GregorianCalendar;

import bohonos.demski.mieldzioc.application.ApplicationState;
import bohonos.demski.mieldzioc.dataBase.InterviewerDBAdapter;
import bohonos.demski.mieldzioc.fillingSurvey.ChooseSurveyAdapter;
import bohonos.demski.mieldzioc.fillingSurvey.ChooseSurveyToFillActivity;
import bohonos.demski.mieldzioc.interviewer.Interviewer;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button newSurveyButt = (Button) findViewById(R.id.new_survey_button);
        newSurveyButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, create_new_survey.class);
                startActivity(intent);
            }
        });
        Interviewer interviewer;
        ApplicationState.getInstance(getApplicationContext()).setLoggedInterviewer((interviewer =
                new Interviewer("Dominik", "Demski", "92110908338", new GregorianCalendar())));
        InterviewerDBAdapter db = new InterviewerDBAdapter(getApplicationContext());
        db.addInterviewer(interviewer);

        Button fillSurveyButton = (Button) findViewById(R.id.fill_survey_button);
        fillSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChooseSurveyToFillActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

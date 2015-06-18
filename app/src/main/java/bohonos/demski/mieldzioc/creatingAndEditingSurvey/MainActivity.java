package bohonos.demski.mieldzioc.creatingAndEditingSurvey;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.util.List;

import bohonos.demski.mieldzioc.application.ApplicationState;
import bohonos.demski.mieldzioc.application.NetworkIssuesControl;
import bohonos.demski.mieldzioc.dataBase.AnsweringSurveyDBAdapter;
import bohonos.demski.mieldzioc.fillingSurvey.ChooseSurveyToFillActivity;
import bohonos.demski.mieldzioc.sendingSurvey.SendSurveysTemplateActivity;
import bohonos.demski.mieldzioc.survey.Survey;

/**
 * Aktywność z różnymi akcjami do wyboru.
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prepareSendFilledSurveysButton();

        Button newSurveyButt = (Button) findViewById(R.id.new_survey_button);

        if(ApplicationState.getInstance(getApplicationContext()).getLoggedInterviewer().
                getInterviewerPrivileges()) {
            newSurveyButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, CreateNewSurvey.class);
                    startActivity(intent);
                }
            });
        }
        else{
            newSurveyButt.setActivated(false);
            newSurveyButt.setBackgroundColor(getResources().getColor(R.color.inactive_button_color));
        }

        Button sendSurveyButton = (Button) findViewById(R.id.send_survey_button);

        sendSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendSurveysTemplateActivity.class);
                startActivity(intent);
            }
        });
        Button fillSurveyButton = (Button) findViewById(R.id.fill_survey_button);
        fillSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChooseSurveyToFillActivity.class);
                startActivity(intent);
            }
        });
    }


    private void prepareSendFilledSurveysButton(){
        Button sendFilledSurveysButton = (Button) findViewById(R.id.send_filled_survey_button);
        final ViewAnimator animator = (ViewAnimator) findViewById(R.id.main_animator);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.sending_survey_progress);

        sendFilledSurveysButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                (new AsyncTask<Void, Integer, Integer>(){

                    @Override
                    protected Integer doInBackground(Void... params) {
                        publishProgress(new Integer[] {-1, -1});
                        List<Survey> toSend = (new AnsweringSurveyDBAdapter(getApplicationContext()))
                                .getAnswersForInterviewer(ApplicationState.
                                        getInstance(getApplicationContext()).getLoggedInterviewer());

                        NetworkIssuesControl networkIssuesControl =
                                new NetworkIssuesControl(getApplicationContext());
                        for(int i = 0; i < toSend.size(); i++){
                            int result = networkIssuesControl.sendFilledSurvey(toSend.get(i));
                            if(result == NetworkIssuesControl.NO_NETWORK_CONNECTION){
                                return -1;
                            }
                            publishProgress(new Integer[] {i, toSend.size()});
                        }
                        return toSend.size();
                    }


                    @Override
                    protected void onPostExecute(Integer integer) {
                        String text;
                        animator.setDisplayedChild(0);
                        if(integer == -1)
                            text = "Brak połączenia z internetem. Spróbuj ponownie później.";
                        else{
                            text = "Ankiety zostały wysłane.";
                        }
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        int i = values[0];
                        int size = values[1];
                        float progress = (i == -1)? 0 : ((i + 1) / size * 100);

                        if(progress == 0) {
                            animator.setDisplayedChild(1);
                            progressBar.setProgress(0);
                        }
                        else{
                            progressBar.setMax(size);
                            progressBar.setProgress(i + 1);
                        }
                    }
                }).execute();
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

package bohonos.demski.mieldzioc.dataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;

import bohonos.demski.mieldzioc.application.ApplicationState;
import bohonos.demski.mieldzioc.application.DateAndTimeService;
import bohonos.demski.mieldzioc.constraints.IConstraint;
import bohonos.demski.mieldzioc.constraints.NumberConstraint;
import bohonos.demski.mieldzioc.constraints.TextConstraint;
import bohonos.demski.mieldzioc.questions.GridQuestion;
import bohonos.demski.mieldzioc.questions.Question;
import bohonos.demski.mieldzioc.questions.ScaleQuestion;
import bohonos.demski.mieldzioc.questions.TextQuestion;
import bohonos.demski.mieldzioc.survey.Survey;

/**
 * Created by Dominik Demski on 2015-05-02.
 */
public class DataBaseAdapter {

    private static final String DEBUG_TAG = "SqLiteSurveyDB";

    private static final int DB_VERSION = 1;


    private SQLiteDatabase db;
    private Context context;
    private DatabaseHelper dbHelper;

    public DataBaseAdapter(Context context) {
        this.context = context;
    }

    /**
     * T� metod� wywo�a� przed wszystkimi innymi.
     * @return
     */
    public DataBaseAdapter open(){
        dbHelper = new DatabaseHelper(context, DB_VERSION);
        Log.d("Otwieram", "Otwieram po��czenie z baz�!");
            db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    /**
     * Dodaj ankiet� do bazy danych.
     * Metoda sama dba o otwarcie i zamkni�cie po��czenia z baz� danych.
     * @param survey ankieta do dodania.
     * @return
     */
    public boolean addSurveyTemplate(Survey survey, int status){
        open();
        String idOfSurveys = survey.getIdOfSurveys();
        int size = survey.questionListSize();

        ContentValues templateValues = new ContentValues();
        templateValues.put(DatabaseHelper.KEY_ID, idOfSurveys);
        templateValues.put(DatabaseHelper.KEY_STATUS, status);
        templateValues.put(DatabaseHelper.KEY_INTERVIEWER, survey.getInterviewer().getId());
        templateValues.put(DatabaseHelper.KEY_CREATED_DATE, DateAndTimeService.getToday());
        templateValues.put(DatabaseHelper.KEY_MODIFICATION_DATE, DateAndTimeService.getToday());
        templateValues.put(DatabaseHelper.KEY_MODIFIED_BY, ApplicationState.getInstance(context).
                getLoggedInterviewer().getId());
        db.insert(DatabaseHelper.SURVEY_TEMPLATE_TABLE, null, templateValues);

        for(int i = 0; i < size; i++){
            Question question = survey.getQuestion(i);
            String questionNumber = "" + idOfSurveys + i;
            if(addQuestion(question, idOfSurveys, questionNumber) == -1) return false;
            int questionType = question.getQuestionType();
            if(questionType == Question.DROP_DOWN_QUESTION ||
                    questionType == Question.MULTIPLE_CHOICE_QUESTION || questionType ==
                    Question.ONE_CHOICE_QUESTION){
                if(addChoiceAnswers(question, idOfSurveys, questionNumber) == -1)
                    return false;
            }
            else if(questionType == Question.GRID_QUESTION){
                if(addGridAnswers((GridQuestion) question, idOfSurveys, questionNumber) == -1)
                    return false;
            }
            else if(questionType == Question.SCALE_QUESTION){
                if(addScaleAnswers((ScaleQuestion) question, idOfSurveys, questionNumber) == -1)
                    return false;
            }
            else if(questionType == Question.TEXT_QUESTION){
                TextQuestion textQuestion = (TextQuestion) question;
                IConstraint constraint = textQuestion.getConstraint();
                if(constraint instanceof TextConstraint){
                    if(addTextConstraints((TextConstraint) constraint, idOfSurveys, questionNumber)
                        == -1)
                        return false;
                }
                else if(constraint instanceof  NumberConstraint){
                    if(addNumberConstraints((NumberConstraint) constraint, idOfSurveys,
                            questionNumber) == -1)
                        return false;
                }
            }
        }
        close();
        Log.d(DEBUG_TAG, "Dodano ankiet� do bazy danych; id: " + survey.getIdOfSurveys() +
                ", tytu�: " + survey.getTitle());
        return true;
    }

    public long addQuestion(Question question, String idOfSurveys, String questionNumber){
        ContentValues questionValues = new ContentValues();
        questionValues.put(DatabaseHelper.KEY_ID_SURVEY_QDB, idOfSurveys);
        questionValues.put(DatabaseHelper.KEY_QUESTION_NUMBER_QDB, questionNumber);
        questionValues.put(DatabaseHelper.KEY_QUESTION_QDB, question.getQuestion());
        questionValues.put(DatabaseHelper.KEY_OBLIGATORY_QDB, (question.isObligatory())? 1 : 0);
        questionValues.put(DatabaseHelper.KEY_HINT_QDB, question.getHint());
        questionValues.put(DatabaseHelper.KEY_ERROR_QDB, question.getErrorMessage());
        questionValues.put(DatabaseHelper.KEY_URL_QDB, question.getPictureURL());
        questionValues.put(DatabaseHelper.KEY_TYPE_QDB, question.getQuestionType());
        questionValues.put(DatabaseHelper.KEY_CREATED_DATE_QDB, DateAndTimeService.getToday());
        questionValues.put(DatabaseHelper.KEY_MODIFICATION_DATE_QDB, DateAndTimeService.getToday());
        questionValues.put(DatabaseHelper.KEY_INTERVIEWER_QDB,
                ApplicationState.getInstance(context).getLoggedInterviewer().getId());
        questionValues.put(DatabaseHelper.KEY_MODIFIED_BY_QDB, String.valueOf(
                ApplicationState.getInstance(context).getLoggedInterviewer().getId()));
        return db.insert(DatabaseHelper.QUESTIONS_TABLE, null, questionValues);
    }
    public long addChoiceAnswers(Question question, String surveyId, String questionNumber){
        List<String> answers = question.getAnswersAsStringList();

        int answersSize = answers.size();

        for(int i = 0; i < answersSize; i++){
            ContentValues answersValues = new ContentValues();
            answersValues.put(DatabaseHelper.KEY_SURVEY_CHADB, surveyId);
            answersValues.put(DatabaseHelper.KEY_QUESTION_CHADB, questionNumber);
            String answerNo = "" + questionNumber + i;
            answersValues.put(DatabaseHelper.KEY_ANSWER_NUMBER_CHADB, answerNo);
            answersValues.put(DatabaseHelper.KEY_ANSWER_CHADB, answers.get(i));
            if(db.insert(DatabaseHelper.CHOICE_ANSWERS_TABLE, null, answersValues) == -1)
                return -1;
        }
        return answersSize;
    }

    public long addScaleAnswers(ScaleQuestion question, String surveyId, String questionNumber) {
        ContentValues answersValues = new ContentValues();
        answersValues.put(DatabaseHelper.KEY_SURVEY_SCDB, surveyId);
        answersValues.put(DatabaseHelper.KEY_QUESTION_SCDB, questionNumber);
        String answerNo = "" + questionNumber;
        answersValues.put(DatabaseHelper.KEY_ANSWER_NUMBER_SCDB, answerNo);
        answersValues.put(DatabaseHelper.KEY_MIN_LAB_SCDB, question.getMinLabel());
        answersValues.put(DatabaseHelper.KEY_MAX_LABEL_SCDB, question.getMaxLabel());
        answersValues.put(DatabaseHelper.KEY_MIN_VALUE_SCDB, question.getMinValue());
        answersValues.put(DatabaseHelper.KEY_MAX_VALUE_SCDB, question.getMaxValue());
        return db.insert(DatabaseHelper.SCALE_ANSWERS_TABLE, null, answersValues);
    }

    public long addGridAnswers(GridQuestion question, String surveyId, String questionNumber) {

        ContentValues columnsValues = new ContentValues();
        List<String> rows = question.getRowLabels();
        List<String> columns = question.getColumnLabels();
        int rowsSize = rows.size();
        int columnsSize = columns.size();

        for(int i = 0; i < rowsSize; i++){
            ContentValues answersValues = new ContentValues();
            answersValues.put(DatabaseHelper.KEY_SURVEY_GRDB, surveyId);
            answersValues.put(DatabaseHelper.KEY_QUESTION_GRDB, questionNumber);
            String answerNo = questionNumber + i;
            answersValues.put(DatabaseHelper.KEY_ANSWER_NUMBER_GRDB, answerNo);
            answersValues.put(DatabaseHelper.KEY_ANSWER_GRDB, rows.get(i));
            if(db.insert(DatabaseHelper.GRID_ROW_ANSWERS_TABLE, null, answersValues) == -1)
                return -1;
        }

        for(int i = 0; i < columnsSize; i++){
            ContentValues answersValues = new ContentValues();
            answersValues.put(DatabaseHelper.KEY_SURVEY_GCDB, surveyId);
            answersValues.put(DatabaseHelper.KEY_QUESTION_GCDB, questionNumber);
            String answerNo = questionNumber + i;
            answersValues.put(DatabaseHelper.KEY_ANSWER_NUMBER_GCDB, answerNo);
            answersValues.put(DatabaseHelper.KEY_ANSWER_GCDB, columns.get(i));
            if(db.insert(DatabaseHelper.GRID_COLUMN_ANSWERS_TABLE, null, answersValues) == -1)
                return -1;
        }
        return columnsSize + rowsSize;
    }

    public long addNumberConstraints(NumberConstraint constraint, String surveyId,
                                     String questionNumber){
        ContentValues constraintsValues = new ContentValues();
        constraintsValues.put(DatabaseHelper.KEY_SURVEY_NCDB, surveyId);
        constraintsValues.put(DatabaseHelper.KEY_QUESTION_NCDB, questionNumber);
        constraintsValues.put(DatabaseHelper.KEY_ANSWER_NUMBER_NCDB, questionNumber);
        constraintsValues.put(DatabaseHelper.KEY_MIN_VALUE_NCDB, constraint.getMinValue());
        constraintsValues.put(DatabaseHelper.KEY_MAX_VALUE_NCDB, constraint.getMaxValue());
        constraintsValues.put(DatabaseHelper.KEY_MUST_BE_INTEGER_NCDB, constraint.isMustBeInteger());
        constraintsValues.put(DatabaseHelper.KEY_NOT_EQUALS_NCDB, constraint.getNotEquals());
        constraintsValues.put(DatabaseHelper.KEY_NOT_BETWEEN_NCDB,
                constraint.isNotBetweenMaxAndMinValue());
        return db.insert(DatabaseHelper.NUMBER_CONSTRAINTS_TABLE, null, constraintsValues);
    }

    public long addTextConstraints(TextConstraint constraint, String surveyId,
                                     String questionNumber){
        ContentValues constraintsValues = new ContentValues();
        constraintsValues.put(DatabaseHelper.KEY_SURVEY_TCDB, surveyId);
        constraintsValues.put(DatabaseHelper.KEY_QUESTION_TCDB, questionNumber);
        constraintsValues.put(DatabaseHelper.KEY_ANSWER_NUMBER_TCDB, questionNumber);
        constraintsValues.put(DatabaseHelper.KEY_MIN_LENGTH_TCDB, constraint.getMinLength());
        constraintsValues.put(DatabaseHelper.KEY_MAX_LENGTH_TCDB, constraint.getMaxLength());
        constraintsValues.put(DatabaseHelper.KEY_REGEX_TCDB, constraint.getRegex().pattern());
        return db.insert(DatabaseHelper.TEXT_CONSTRAINTS_TABLE, null, constraintsValues);
    }
}

package bohonos.demski.mieldzioc.application;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import bohonos.demski.mieldzioc.dataBase.DataBaseAdapter;
import bohonos.demski.mieldzioc.survey.Survey;
import bohonos.demski.mieldzioc.survey.SurveyHandler;

/**
 * Created by Dominik on 2015-05-04.
 */
public class SurveyHandlerMobile extends SurveyHandler {

    private Context context;
    private DataBaseAdapter db;

    /**
     *
     * @param context
     * @param lastSurveysId ostatnio przyznane id grupy ankiet dla tego ankietera.
     */
    public SurveyHandlerMobile(Context context, int lastSurveysId) {
        super(lastSurveysId);
        this.context = context;
        db = new DataBaseAdapter(context);
        db.open();
        HashMap<Survey, Integer> surveys = db.getAllSurveyTemplates();
        db.close();
        for(Survey survey : surveys.keySet()){
            super.loadSurveyTemplate(survey, surveys.get(survey));
        }
    }

    /**
     *
     * @param context
     * @param lastSurveysId ostatnio przyznane id grupy ankiet dla tego ankietera.
     * @param toFillSurveys lista szablonów ankiet do dodania ze statusem aktywna.
     */
    public SurveyHandlerMobile(Context context, int lastSurveysId, List<Survey> toFillSurveys) {
        super(lastSurveysId);
        this.context = context;
        db = new DataBaseAdapter(context);
        for(Survey survey : toFillSurveys){
            super.loadSurveyTemplate(survey, SurveyHandler.ACTIVE);
        }
    }

    public void setContext(Context context) {
        this.context = context;
        db = new DataBaseAdapter(context);
    }

    /**
     * Dodaje nowy szablon ankiety do klasy SurveyTemplate i do bazy danych.
     * @param survey ankieta do dodania.
     * @return id dodanego szablonu (id grupy ankiet), jeśli nie udało się dodać szablonu do bazy
     * danych, zwraca -1.
     */
    @Override
    public String addNewSurveyTemplate(Survey survey) {
        if(survey == null) throw new NullPointerException("Przekazana ankieta nie może być nullem " +
                "- próba dodania ankiety do bazy danych");
        String id =  super.addNewSurveyTemplate(survey);
        super.setSurveyStatus(survey, SurveyHandler.IN_PROGRESS);

        Log.d("DODANIE_SZABLONU_BAZA", String.valueOf(super.getSurveyStatus(survey.getIdOfSurveys())));
        if(!db.addSurveyTemplate(survey, super.getSurveyStatus(survey.getIdOfSurveys()), false)) return null;
        ApplicationState.getInstance(context).saveLastAddedSurveyTemplateNumber(super.getMaxSurveysId());
        return id;
    }
}

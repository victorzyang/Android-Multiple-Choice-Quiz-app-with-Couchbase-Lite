package com.example.androidcouchbaselitesetup;

public class Question {

    private String mQuestionString; //id of string resource representing the question
    private String[] mOptions; //the 5 possible options for each question
    private String mContributor; //author or contributor of the question
    private int Button; //when button is clicked, it will be assigned an int from 1 to 10

    public Question(String aQuestion, String[] someOptions){ //it IS valid for constructor to take an array as a parameter
        mQuestionString = aQuestion;
        mOptions = someOptions;
        mContributor = "anonymous";
        Button = 0;
    }
    public Question(String aQuestion, String[] someOptions, String contributer){
        mQuestionString = aQuestion;
        mOptions = someOptions;
        if(contributer != null && !contributer.isEmpty())
            mContributor = contributer;
        else
            mContributor = "anonymous";
        Button = 0;
    }

    public String getQuestionString(){return mQuestionString;}

    public String[] getQuestionOptions(){
        return mOptions;
    }

    public String getContributer(){return mContributor;}

    public String toString(){ //displays the current question and options to UI
        String toReturn = "";
        if(mContributor != null && !mContributor.isEmpty())
            toReturn += "[" + mContributor + "] ";
        toReturn += mQuestionString;
        toReturn += " A) " + mOptions[0];
        toReturn += " B) " + mOptions[1];
        toReturn += " C) " + mOptions[2];
        toReturn += " D) " + mOptions[3];
        toReturn += " E) " + mOptions[4];
        return toReturn;
    }

    public int getButton(){
        return Button;
    }
    public void setButton(int Button){
        this.Button=Button;
    }

}

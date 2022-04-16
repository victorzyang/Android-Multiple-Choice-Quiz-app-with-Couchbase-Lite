package com.example.androidcouchbaselitesetup;

//This class was added for thesis
public class Answer {

    private String mAnswerString;
    private String mContributor; //author or contributor of the question

    public Answer(String anAnswer){
        mAnswerString = anAnswer;
        mContributor = "anonymous";
    }

    public Answer(String anAnswer, String contributer){
        mAnswerString = anAnswer;
        if(contributer != null && !contributer.isEmpty())
            mContributor = contributer;
        else
            mContributor = "anonymous";
    }

    //getter for the Answer
    public String getAnswerString(){return mAnswerString;}

    public String toString(){ //displays the current question answer to UI
        String toReturn = "";
        if(mContributor != null && !mContributor.isEmpty())
            toReturn += "[" + mContributor + "] ";
        toReturn += mAnswerString;
        return toReturn;
    }

}

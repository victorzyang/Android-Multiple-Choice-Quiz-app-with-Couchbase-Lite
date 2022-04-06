package com.example.androidcouchbaselitesetup;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.util.ArrayList;

public class AnswerKey {

    public static final String XML_ANSWER_KEY = "answer_key";

    public static ArrayList<Answer> pullParseFrom(BufferedReader reader){
        ArrayList<Answer> answers = new ArrayList<>();

        // Get our factory and create a PullParser
        XmlPullParserFactory factory = null; //sets up an instance of the XmlPullParser
        try{
            factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(reader); //set input file for parser
            int eventType = xpp.getEventType(); //get initial eventType

            Answer answer;
            boolean thisIsAnAnswer = false;
            String answer_text = "";
            String contributorName = "";

            //Loop through pull events until we reach END_DOCUMENT
            while(eventType != XmlPullParser.END_DOCUMENT){

                //handle the xml tags encountered
                switch(eventType){
                    case XmlPullParser.START_TAG: //XML opening tags
                        Log.i(XML_ANSWER_KEY, "START_TAG: " + xpp.getName());
                        if (xpp.getName().equals("question")){
                            for (int i=0; i<xpp.getAttributeCount(); i++){
                                Log.i(XML_ANSWER_KEY, "Attribute name: " + xpp.getAttributeName(i));
                                Log.i(XML_ANSWER_KEY, "Attribute value: " + xpp.getAttributeValue(i));
                                contributorName = xpp.getAttributeValue(i);
                            }
                        }else if (xpp.getName().equals("answer")){
                            thisIsAnAnswer = true;
                            Log.i(XML_ANSWER_KEY, "thisIsAnAnswer is set to true");
                        }
                    case XmlPullParser.TEXT:
                        Log.i(XML_ANSWER_KEY, "TEXT: " + xpp.getText());
                        Log.i(XML_ANSWER_KEY, "Is this an answer? " + thisIsAnAnswer);
                        if(thisIsAnAnswer == true && xpp.getText() != null){
                            answer_text = xpp.getText();

                            thisIsAnAnswer = false;
                            Log.i(XML_ANSWER_KEY, "thisIsAnAnswer is reset to false");

                            answer = new Answer(answer_text, contributorName);
                            Log.i(XML_ANSWER_KEY, "The string of the answer to be added is: " + answer.getAnswerString());
                            answers.add(answer);
                        }
                        break;
                    case XmlPullParser.END_TAG: //XML closing tags
                        Log.i(XML_ANSWER_KEY, "END_TAG: " + xpp.getName());
                        break;
                    default:
                        break;
                }
                //iterate
                eventType = xpp.next();
            }

        }catch(XmlPullParserException e){
            e.printStackTrace();
        }catch(java.io.IOException e){
            e.printStackTrace();
        }

        //print out answers
        for (int i = 0; i < answers.size(); i++) {
            String answerText = answers.get(i).getAnswerString();
            Log.i(XML_ANSWER_KEY, "answerText at index " + i + " is: " + answerText);
        }

        return answers;
    }

}

package com.example.androidcouchbaselitesetup;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class Exam {

    //XML tags used to define an exam of multiple choice questions.
    public static final String XML_EXAM = "exam";

    private static String emailText = "";

    public static ArrayList<Question> pullParseFrom(BufferedReader reader){

        ArrayList<Question> questions = new ArrayList<>();

        // Get our factory and create a PullParser
        XmlPullParserFactory factory = null; //sets up an instance of the XmlPullParser
        try{
            factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(reader); //set input file for parser
            int eventType = xpp.getEventType(); //get initial eventType

            boolean thisIsAnEmail = false;
            Email email;
            Question question;
            boolean thisIsAQuestion = false;
            String question_text = "";
            boolean thisIsAnOption = false;
            String option_text = "";
            int option_counter = 0; //used to determine if all 5 options have been added for each Question element in array
            String[] options = new String[5]; //an array to hold all 5 options
            String contributorName = "";

            //Loop through pull events until we reach END_DOCUMENT
            while(eventType != XmlPullParser.END_DOCUMENT){

                //handle the xml tags encountered
                switch(eventType){
                    case XmlPullParser.START_TAG: //XML opening tags
                        Log.i(XML_EXAM, "START_TAG: " + xpp.getName());
                        if(xpp.getName().equals("email")){
                            thisIsAnEmail = true;
                        }else if(xpp.getName().equals("question")){
                            for (int i=0; i<xpp.getAttributeCount(); i++){
                                Log.i(XML_EXAM, "Attribute name: " + xpp.getAttributeName(i));
                                Log.i(XML_EXAM, "Attribute value: " + xpp.getAttributeValue(i));
                                contributorName = xpp.getAttributeValue(i);
                            }
                        }else if(xpp.getName().equals("question_text")){
                            thisIsAQuestion = true;
                        }else if(xpp.getName().equals("option")){
                            thisIsAnOption = true;
                        }

                        break;

                    case XmlPullParser.TEXT:
                        Log.i(XML_EXAM, "TEXT: " + xpp.getText());
                        if(thisIsAnEmail == true){
                            emailText = xpp.getText();
                            //email = new Email(emailText);
                            thisIsAnEmail = false;
                        }else if(thisIsAQuestion == true){
                            question_text = xpp.getText();

                            thisIsAQuestion = false; //reset back to false
                        }else if(thisIsAnOption == true){
                            option_text = xpp.getText();

                            options[option_counter] = option_text; //adds a new option to the array of options
                            option_counter++;

                            thisIsAnOption = false;
                        }

                        if(option_counter==5){
                            question = new Question(question_text, options, contributorName);
                            questions.add(question);
                            option_counter = 0; //reset the option counter back to 0
                            options = new String[5];
                            thisIsAnOption = false;
                        }

                        break;

                    case XmlPullParser.END_TAG: //XML closing tags
                        Log.i(XML_EXAM, "END_TAG: " + xpp.getName());

                        break;

                    default:
                        break;
                }
                //iterate
                eventType = xpp.next();
            }

        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch(java.io.IOException e){
            e.printStackTrace();
        }

        return questions;
    }

    public String getEmail(){
        return emailText;
    }

}

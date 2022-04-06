package com.example.androidcouchbaselitesetup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDictionary;
import com.couchbase.lite.Query;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Expression;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.Result;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.URLEndpoint;

// import com.couchbase.lite.ReplicatorType;
import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.DataSource;
import android.util.Log;
import com.couchbase.lite.internal.CouchbaseLiteInternal.*;
import java.net.URI;
import java.net.URISyntaxException;

import com.couchbase.lite.Database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG =  MainActivity.class.getSimpleName();
    private static final int RESULT = 0;
    public static final String EMAIL_KEY = "EMAIL_KEY";
    public static final String EXAM_KEY = "EXAM_KEY";
    public static final String DATABASE = "DATABASE";

    //all the buttons
    private Button mButtonA;
    private Button mButtonB;
    private Button mButtonC;
    private Button mButtonD;
    private Button mButtonE;
    private Button mNextButton;
    private Button mPrevButton;
    private Button msubmitButton;
    private Button mViewQuestionData;
    private Button mViewAnswerKeyData;
    private Button mViewTestData;

    private TextView mQuestionTextView;
    private ArrayList<Question> questions; //Have an arraylist of Questions, and each Question has 5 options and an answer
    private ArrayList<Answer> answers; //Added for thesis

    private int mCurrentQuestionIndex; //used to determine which question user is on
    private static String QUESTION_INDEX_KEY = "question_index";

    private int[] buttons = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; //used to determine which answer user has selected for each of the 10 questions
    private String emailString = "";
    //private int mCurrentSelectedButton; //used to determine which button is selected by the user at current question index
    //private static String BUTTON_KEY = "button";
    //private boolean gameHasStarted = false; //something is wrong here...

    private Context cntx = this; //for database?
    Database database;
    private String[] optionChars = {"A", "B", "C", "D", "E"};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //DO I NEED THIS METHOD???
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            Log.i(TAG, ".launch(intent) was triggered");
                        }
                    }
                });

        /*private val getResult =
                registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                val value = it.data?.getStringExtra("input")
            }
        }*/

        // One-off initialization
        CouchbaseLite.init(cntx);
        Log.i(TAG,"Initialized CBL");

        // Step 1: Create a database (this step looks ok?)
        Log.i(TAG, "Starting DB");
        final DatabaseConfiguration cfg = new DatabaseConfiguration(); //Does this need to be final?
        cfg.setDirectory(cntx.getFilesDir().getAbsolutePath()); //Do I need to have this line?
        database = null;
        try {
            database = new Database(  "testDb2", cfg);
            MutableDocument mutableDoc = //test
                    new MutableDocument().setFloat("version", 2.0f)
                            .setString("type", "SDK");
            database.save(mutableDoc);

            /*Document document =
                    database.getDocument(mutableDoc.getId());*/
            Log.i(TAG, "I LOVE MISS KOBAYASHI");

            //Log.i(TAG, "document.getString() is: " + document.getString("type")); //this works

            ResultSet rs =
                    QueryBuilder.select(
                            SelectResult.property("type"),
                            SelectResult.property("version"))
                            .from(DataSource.database(database))
                            .where(Expression.property("type").equalTo(Expression.string("SDK")))
                            .execute();

            for (Result result : rs) {
                Log.i(TAG, String.format("type is -> %s", result.getString("type")));
                Log.i(TAG, String.format("version is -> %s", result.getFloat("version")));
            }

            /*List<Result> listOfResults = rs.allResults();

            Log.i(TAG,
                    "listOfResults[0].toList() is: " +
                            listOfResults.get(0).toList()/*.getKeys()*/ /*+ ", listOfResults.get(0).toList().get(0) is: "
                            + listOfResults.get(0).toList().get(0));*/
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        // Questions model
        /*
         * questions: {
         *   submission_email: string,
         *   questions: array: dictionary
         * }
         * */

        // Step 2: Create 3 new documents (i.e. a record) in the database. (Both of these docs here look ok according to online document)
        MutableDocument questionsDoc = new MutableDocument(); //for the questions
        MutableDocument answerKeyDoc = new MutableDocument();

        //    MutableDocument mutableDoc =
        //      new MutableDocument().setFloat("version", 2.0f)
        //                            .setString("type", "SDK");

        // Step 4: Retrieve and update a document. (Does this allow me to view database?)
        //    mutableDoc =
        //      database.getDocument(mutableDoc.getId())
        //                            .toMutable()
        //                            .setString("language", "Java");
        //    try {
        //            database.save(mutableDoc);
        //    } catch (CouchbaseLiteException e) {
        //            e.printStackTrace();
        //    }

        // Step 5: Retrieve immutable document and log the document ID
        //    // generated by the database and some document properties
        //    Document document = database.getDocument(mutableDoc.getId());
        //    Log.i(TAG, String.format("Document ID :: %s", document.getId()));
        //    Log.i(TAG, String.format("Learning :: %s:", document.getString("language")));

        // Step 6: Create a query to fetch documents of type SDK.
        //    try {
        //          ResultSet rs =
        //            QueryBuilder.select(SelectResult.all())
        //                  .from(DataSource.database(database))
        //                  .where(Expression.property("type").equalTo(Expression.string("SDK")))
        //                  .execute();
        //          Log.i(TAG,
        //            String.format("Number of rows :: %n",
        //                          rs.allResults().size()));
        //    } catch (CouchbaseLiteException e) {
        //            e.printStackTrace();
        //    }

        //How to view data from database? Maybe look under 'Query' in https://docs.couchbase.com/couchbase-lite/2.7/java-android.html?

        //The SelectResult.all() method can be used to query all the properties of a document

        mButtonA = (Button) findViewById(R.id.a_button);
        mButtonB = (Button) findViewById(R.id.b_button);
        mButtonC = (Button) findViewById(R.id.c_button);
        mButtonD = (Button) findViewById(R.id.d_button);
        mButtonE = (Button) findViewById(R.id.e_button);
        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);
        msubmitButton = (Button) findViewById(R.id.submit_button);
        mViewQuestionData = (Button) findViewById(R.id.question_table_button);
        mViewAnswerKeyData = (Button) findViewById(R.id.answer_key_table_button);
        mViewTestData = (Button) findViewById(R.id.test_table_button);

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);

        mButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button A Clicked"); //print to console for debugging

                questions.get(mCurrentQuestionIndex).setButton(1); //'get' returns the element at the specified position in this list
                //mCurrentSelectedButton = 1;
                buttons[mCurrentQuestionIndex] = 1;
                buttonColour();
            }
        });
        mButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button B Clicked"); //print to console for debugging

                questions.get(mCurrentQuestionIndex).setButton(2);
                buttons[mCurrentQuestionIndex] = 2;
                buttonColour();
            }
        });
        mButtonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button C Clicked"); //print to console for debugging

                questions.get(mCurrentQuestionIndex).setButton(3);
                buttons[mCurrentQuestionIndex] = 3;
                buttonColour();
            }
        });
        mButtonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button D Clicked"); //print to console for debugging

                questions.get(mCurrentQuestionIndex).setButton(4);
                buttons[mCurrentQuestionIndex] = 4;
                buttonColour();
            }
        });
        mButtonE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button E Clicked"); //print to console for debugging

                questions.get(mCurrentQuestionIndex).setButton(5);
                buttons[mCurrentQuestionIndex] = 5;
                buttonColour();
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCurrentQuestionIndex++;
                if(mCurrentQuestionIndex>=questions.size()) mCurrentQuestionIndex = 0; //goes back to first question
                mQuestionTextView.setText("" + (mCurrentQuestionIndex+1) + ") " + questions.get(mCurrentQuestionIndex).toString());
                buttonColour();
            }
        });
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentQuestionIndex--;
                if(mCurrentQuestionIndex<0) mCurrentQuestionIndex = questions.size() - 1; //goes to last question
                mQuestionTextView.setText("" + (mCurrentQuestionIndex+1) + ") " + questions.get(mCurrentQuestionIndex).toString());
                buttonColour();
            }
        });
        msubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Submit Button Clicked"); //print to console for debugging
                Intent intent = new Intent(MainActivity.this, Submission.class); //goes to a new page
                Log.i(TAG, "Submit Button Clicked"); //print to console for debugging
                intent.putExtra(EMAIL_KEY, emailString);

                intent.putExtra(EXAM_KEY, buttons); //have different keys
                //Should have a method for receiving intent from Submission.java class?
                //startActivityForResult(intent, RESULT); //What's the alternative that I should use then?
                startActivity(intent);
                //someActivityResultLauncher.launch(intent);
                Log.i(TAG, "someActivityResultLauncher was called");

                //TODO
                MutableDocument testDoc = new MutableDocument(); //this looks good
                MutableArray answersForTestDoc = new MutableArray(); //this looks good
                for (int i = 0; i < buttons.length; i++){
                    MutableDictionary studentAnswerDict = new MutableDictionary();
                    int question_num = i + 1;
                    studentAnswerDict.setInt("question_num", question_num);
                    if(buttons[i] == 1){
                        studentAnswerDict.setString("answer", "A");
                    }else if (buttons[i]==2){
                        studentAnswerDict.setString("answer", "B");
                    }else if (buttons[i]==3){
                        studentAnswerDict.setString("answer", "C");
                    }else if (buttons[i]==4){
                        studentAnswerDict.setString("answer", "D");
                    }else{
                        studentAnswerDict.setString("answer", "E");
                    }

                    answersForTestDoc.addDictionary(studentAnswerDict);
                }

                testDoc.setString("document_type", "test");
                testDoc.setString("submission_email", emailString);
                testDoc.setArray("answers", answersForTestDoc);

                try {
                    database.save(testDoc);
                    //database.close(); //Do I need to close database?
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }

            }

        });

        mViewQuestionData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Question Table button clicked");

                /*Query query = QueryBuilder
                        //.select(SelectResult.all()) //is there something wrong with SelectResult.all()?
                        .select(SelectResult.all())
                        .from(DataSource.database(database))
                        .where(Expression.property("document_type").equalTo(Expression.string("questions")));*/

                try {
                    ResultSet rs = QueryBuilder
                            .select(
                                    SelectResult.property("submission_email"),
                                    SelectResult.property("questions"))
                            .from(DataSource.database(database))
                            .where(Expression.property("document_type").equalTo(Expression.string("questions")))
                            .execute(); //is there something wrong with query.execute()?
                    //Log.i(TAG, "rs.allResults().get(0) is: " + rs.allResults().get(0));
                    //Log.i(TAG, "submission_email for rs.allResults().get(0) is: " + rs.allResults().get(0).getString("submission_email"));
                    StringBuffer buffer = new StringBuffer();
                    Log.i(TAG, "LOU IS THE BEST");
                    for (Result result : rs.allResults()) {
                        /*Log.i(TAG, "result is: " + result);
                        buffer.append("Document type: " + result.getString("document_type") + "\n");
                        Log.i(TAG, "Document type is: " + result.getString("document_type"));*/
                        buffer.append("Submission Email: " + result.getString("submission_email") + "\n");
                        Log.i(TAG, "Submission email is: " + result.getString("submission_email"));
                        String questionsArrayToString = "[";
                        for (int i = 0; i < result.getArray("questions").count(); i++) {
                            //questionsArrayToString += result.getArray("questions")[i];
                            Log.i(TAG, "Checking result.getArray(questions) at index " + i);
                            questionsArrayToString += "(";
                            questionsArrayToString += result.getArray("questions").getDictionary(i).getInt("question_num");
                            Log.i(TAG, "question_num is: " + result.getArray("questions").getDictionary(i).getInt("question_num"));
                            questionsArrayToString += ", ";
                            questionsArrayToString += result.getArray("questions").getDictionary(i).getDictionary("options");
                            Log.i(TAG, "question_num is: " + result.getArray("questions").getDictionary(i).getDictionary("options"));
                            questionsArrayToString += ", ";
                            questionsArrayToString += result.getArray("questions").getDictionary(i).getString("description");
                            Log.i(TAG, "question_num is: " + result.getArray("questions").getDictionary(i).getString("description"));
                            questionsArrayToString += ")";
                        }
                        questionsArrayToString += "]";
                        buffer.append("Questions: " + questionsArrayToString + "\n\n"); //this is an array of dictionaries
                        Log.i(TAG, "Questions is: " + questionsArrayToString);
                        //buffer.append("Questions: " + result.getArray("questions") + "\n\n"); //this is an array of dictionaries
                        //Log.i(TAG, "Questions is: " + result.getArray("questions"));
                    }

                    //Show all data
                    showMessage("Questions table Data", buffer.toString());
                }catch (CouchbaseLiteException e) {
                    Log.e("Sample", e.getLocalizedMessage());
                }
            }
        });

        mViewAnswerKeyData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Query query = QueryBuilder
                        .select(SelectResult.all())
                        .from(DataSource.database(database))
                        .where(Expression.property("document_type").equalTo(Expression.string("answerKey")));*/

                try {
                    ResultSet rs = QueryBuilder
                            .select(SelectResult.property("answers"))
                            .from(DataSource.database(database))
                            .where(Expression.property("document_type").equalTo(Expression.string("answerKey")))
                            .execute();
                    StringBuffer buffer = new StringBuffer();
                    for (Result result : rs.allResults()) {
                        String answersArrayToString = "[";
                        for (int i = 0; i < result.getArray("answers").count(); i++) {
                            answersArrayToString += "(";
                            answersArrayToString += result.getArray("answers").getDictionary(i).getInt("question_num");
                            answersArrayToString += ", ";
                            answersArrayToString += result.getArray("answers").getDictionary(i).getString("answer");
                            answersArrayToString += ")";
                        }
                        answersArrayToString += "]";
                        buffer.append("Answers: " + answersArrayToString + "\n\n");
                    }

                    //Show all data
                    showMessage("Answer Key Data", buffer.toString());
                }catch (CouchbaseLiteException e) {
                    Log.e("Sample", e.getLocalizedMessage());
                }
            }
        });

        mViewTestData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Query query = QueryBuilder
                        .select(SelectResult.all())
                        .from(DataSource.database(database))
                        .where(Expression.property("document_type").equalTo(Expression.string("test")));*/

                try {
                    ResultSet rs = QueryBuilder
                            .select(
                                    SelectResult.property("submission_email"),
                                    SelectResult.property("answers"))
                            .from(DataSource.database(database))
                            .where(Expression.property("document_type").equalTo(Expression.string("test")))
                            .execute();
                    StringBuffer buffer = new StringBuffer();
                    for (Result result : rs) {
                        buffer.append("Submission Email: " + result.getString("submission_email") + "\n");
                        String answersArrayToString = "[";
                        for (int i = 0; i < result.getArray("answers").count(); i++) {
                            answersArrayToString += "(";
                            answersArrayToString += result.getArray("answers").getDictionary(i).getInt("question_num");
                            answersArrayToString += ", ";
                            answersArrayToString += result.getArray("answers").getDictionary(i).getString("answer");
                            answersArrayToString += ")";
                        }
                        answersArrayToString += "]";
                        buffer.append("Answers: " + answersArrayToString + "\n\n");
                    }

                    //Show all data
                    showMessage("Student Test Data", buffer.toString());
                }catch (CouchbaseLiteException e) {
                    Log.e("Sample", e.getLocalizedMessage());
                }
            }
        });

        //Initialise Data Model objects
        //questions = Question.exampleSet1();
        questions = null;

        mCurrentQuestionIndex = 0;

        //Try to read resource data file with questions
        Exam emailObject = new Exam();
        ArrayList<Question> parsedModel = null;
        try {
            InputStream iStream = getResources().openRawResource(R.raw.comp2601exam);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
            //ArrayList<Question> parsedModel = Exam.parseFrom(bs);
            //emailString = Exam.pullParseFrom(bReader);
            parsedModel = Exam.pullParseFrom(bReader); //calls this static method to parse the data and create the exam questions
            emailString = emailObject.getEmail(); //gets the email text
            bReader.close();
        }
        catch (java.io.IOException e){
            e.printStackTrace();
        }
        if(parsedModel == null || parsedModel.isEmpty())
            Log.i(TAG, "ERROR: Questions Not Parsed");
        questions = parsedModel;
        Log.i(TAG, "Email is: " + emailString);

        //insert questions here
        MutableArray questionsForDB = new MutableArray(); //this is how to create an array according to online doc (and the contents look alright to me too?)

        //Log.i(TAG, "Debugging by iterating through questions");

        for (int i = 0; i < questions.size(); i++){
            //create a dictionary for each question, which will be inserted into an array
            MutableDictionary questionDict = new MutableDictionary(); //this is how to create dictionary according to online doc
            int question_num = i+1; //this is correct
            String questionDesc = questions.get(i).getQuestionString(); //this is correct
            //get options, which is also a dictionary
            //Log.i(TAG, "Iterating through the options now");
            MutableDictionary options = new MutableDictionary(); //this appears to be correct
            for (int o = 0; o < questions.get(i).getQuestionOptions().length; o++){
                String option_desc = questions.get(i).getQuestionOptions()[o]; //option_desc is correct
                options.setString(optionChars[o], option_desc);
            }

            //iterate through the options dict
            /*for (String key : options) {
                Log.i(TAG, "Key " + key +", = " + options.getValue(key));
            }*/

            questionDict.setInt("question_num", question_num);
            questionDict.setString("description", questionDesc); //this is how to set string in dict according to online document
            questionDict.setDictionary("options", options);

            questionsForDB.addDictionary(questionDict); //This should be how to add a dictionary to an array
        }

        //TODO: try iterating through the questionsForDB array?
        for (int i = 0; i < questionsForDB.count(); i++)
        {
            Log.i("tag", "Item " + i + " = " + questionsForDB.getDictionary(i));
        }

        questionsDoc.setString("document_type", "questions"); //This is how I set a string according to online doc
        questionsDoc.setString("submission_email", emailString); //This is correct too
        questionsDoc.setArray("questions", questionsForDB); //This is how I set an array according to online doc

        //The following 3 if statements are all correct

        if (questionsDoc.contains("document_type")) {
            Log.i("tag", "\nquestionsDoc contains document_type property");
            Log.i("tag", "The value is " + questionsDoc.getString("document_type"));
        } else {
            Log.i("tag", "\nERROR: no such document_type property in questionsDoc");
        }

        if (questionsDoc.contains("submission_email")) {
            Log.i("tag", "questionsDoc contains submission_email property");
            Log.i("tag", "The value is " + questionsDoc.getString("submission_email"));
        } else {
            Log.i("tag", "ERROR: no such submission_email property in questionsDoc");
        }

        if (questionsDoc.contains("questions")) {
            Log.i("tag", "questionsDoc contains questions property");
            Log.i("tag", "The array is "  +questionsDoc.getArray("questions"));
        } else {
            Log.i("tag", "ERROR: no such questionsDoc property in questionsDoc");
        }

        //This is correct too
        Log.i("tag", "questionsDoc contains " + questionsDoc.count() + " elements\n");

        //Saving a document should be done after document is populated
        try {
            database.save(questionsDoc); //this is how to save a document according to online doc
            Log.i(TAG, "questionsDoc is saved to database");
            ResultSet rs = QueryBuilder
                    //.select(SelectResult.all()) //is there something wrong with SelectResult.all()?
                    .select(SelectResult.all())
                    .from(DataSource.database(database))
                    .where(Expression.property("document_type").equalTo(Expression.string("questions")))
                    .execute(); //is there something wrong with query.execute()?
            //Log.i(TAG, "rs.allResults().get(0) is: " + rs.allResults().get(0));
            //Log.i(TAG, "submission_email for rs.allResults().get(0) is: " + rs.allResults().get(0).getString("submission_email"));
            StringBuffer buffer = new StringBuffer();
            for (Result result : rs.allResults()) {
                Log.i(TAG, "result is: " + result);
                buffer.append("Document type: " + result.getString("document_type") + "\n");
                Log.i(TAG, "Document type is: " + result.getString("document_type"));
                buffer.append("Submission Email: " + result.getString("submission_email") + "\n");
                Log.i(TAG, "Submission email is: " + result.getString("submission_email"));
                buffer.append("Questions: " + result.getArray("questions") + "\n\n");
                Log.i(TAG, "Questions is: " + result.getArray("questions"));
            }
            //database.close(); //Do I need to close database?
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        Log.i("tag", "Now about to insert answerKey");
        if(questions != null && questions.size() > 0)
            mQuestionTextView.setText("" + (mCurrentQuestionIndex + 1) + ") " +
                    questions.get(mCurrentQuestionIndex).toString());

        //insert answerkey data here

        ArrayList<Answer> parsedModel_Answers = null;
        try {
            InputStream iStream = getResources().openRawResource(R.raw.answerkey);
            BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
            parsedModel_Answers = AnswerKey.pullParseFrom(bReader);
            bReader.close();
        }catch (java.io.IOException e){
            e.printStackTrace();
        }
        if(parsedModel_Answers == null || parsedModel_Answers.isEmpty())
            Log.i(TAG, "ERROR: Answers Not Parsed");
        answers = parsedModel_Answers;

        MutableArray answersForDB = new MutableArray();

        Log.i("tag", "Printing out each question's answerString");
        for (int i = 0; i < answers.size(); i++) {
            MutableDictionary answerForDB = new MutableDictionary();
            int question_num = i+1;
            String answerString = answers.get(i).getAnswerString();
            Log.i("tag", "answerString is: " + answerString);

            answerForDB.setInt("question_num", question_num);
            answerForDB.setString("answer", answerString);

            answersForDB.addDictionary(answerForDB);
        }

        answerKeyDoc.setString("document_type", "answerKey");
        answerKeyDoc.setArray("answers", answersForDB);

        try {
            database.save(answerKeyDoc);
            //database.close(); //Do I need to close database?
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        if(savedInstanceState != null){ //check for the possiblity that there is no saved state information
            mCurrentQuestionIndex = savedInstanceState.getInt(QUESTION_INDEX_KEY, 0);
            buttons = (int[]) savedInstanceState.getSerializable("arr");
            //mCurrentSelectedButton = savedInstanceState.getInt(BUTTON_KEY, 0);
            mQuestionTextView.setText("" + (mCurrentQuestionIndex+1) + ") " + questions.get(mCurrentQuestionIndex).toString());
            Log.i(TAG, "Current question index: " + mCurrentQuestionIndex);
            //Log.i(TAG, "Current button selected: " + questions.get(mCurrentQuestionIndex).getButton());
            Log.i(TAG, "Current button selected: " + buttons[mCurrentQuestionIndex]);
            buttonColour();
        }

        /*if(gameHasStarted==true){
            mQuestionTextView.setText("" + (mCurrentQuestionIndex+1) + ") " + questions.get(mCurrentQuestionIndex).toString());
            Log.i(TAG, "Current question index: " + mCurrentQuestionIndex);
            Log.i(TAG, "Current button selected: " + questions.get(mCurrentQuestionIndex).getButton());
            if(questions.get(mCurrentQuestionIndex).getButton() > 0){
                buttonColour();
            }
        }*/
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){ //the state is restored in onCreate()
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(QUESTION_INDEX_KEY, mCurrentQuestionIndex); //saves the current question user is on
        savedInstanceState.putSerializable("arr", buttons); //saves all of the user's selected answers
        //savedInstanceState.putInt(BUTTON_KEY, mCurrentSelectedButton);
        Log.i(TAG, "onSaveInstanceState(Bundle)");
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG, "onResume()");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.i(TAG, "onStop()");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.i(TAG, "onRestart()");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }

    private void buttonColour(){
        buttonClear();

        /*if(questions.get(mCurrentQuestionIndex).getButton()==1){
            mButtonA.setBackgroundColor(Color.BLUE);
        }
        if(questions.get(mCurrentQuestionIndex).getButton()==2){
            mButtonB.setBackgroundColor(Color.BLUE);
        }
        if(questions.get(mCurrentQuestionIndex).getButton()==3){
            mButtonC.setBackgroundColor(Color.BLUE);
        }
        if(questions.get(mCurrentQuestionIndex).getButton()==4){
            mButtonD.setBackgroundColor(Color.BLUE);
        }
        if(questions.get(mCurrentQuestionIndex).getButton()==5){
            mButtonE.setBackgroundColor(Color.BLUE);
        }*/

        if(buttons[mCurrentQuestionIndex]==1){
            mButtonA.setBackgroundColor(Color.BLUE);
        }
        if(buttons[mCurrentQuestionIndex]==2){
            mButtonB.setBackgroundColor(Color.BLUE);
        }
        if(buttons[mCurrentQuestionIndex]==3){
            mButtonC.setBackgroundColor(Color.BLUE);
        }
        if(buttons[mCurrentQuestionIndex]==4){
            mButtonD.setBackgroundColor(Color.BLUE);
        }
        if(buttons[mCurrentQuestionIndex]==5){
            mButtonE.setBackgroundColor(Color.BLUE);
        }
    }

    private void buttonClear(){
        mButtonA.setBackgroundColor(Color.GRAY);
        mButtonB.setBackgroundColor(Color.GRAY);
        mButtonC.setBackgroundColor(Color.GRAY);
        mButtonD.setBackgroundColor(Color.GRAY);
        mButtonE.setBackgroundColor(Color.GRAY);
    }

    private void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

}
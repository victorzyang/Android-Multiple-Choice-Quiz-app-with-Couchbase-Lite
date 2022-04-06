package com.example.androidcouchbaselitesetup;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

public class Submission extends AppCompatActivity {

    private EditText mNameText;
    private EditText mStudentNumberText;
    private Button submit;
    //private String name;
    //private String number;
    private  final String TAG = this.getClass().getSimpleName() + " @" + System.identityHashCode(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submission);

        mNameText = (EditText) findViewById(R.id.nameEditText);
        mStudentNumberText = (EditText) findViewById(R.id.studentNumberEditText);
        submit = (Button) findViewById(R.id.submitButton);

        //final String email = getIntent().getStringExtra(MainActivity.EMAIL_KEY); //is this the correct code???

        //int[] array = getIntent().getIntArrayExtra(MainActivity.EXAM_KEY); //is this the correct code???

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mNameText.getText().toString();
                String number = mStudentNumberText.getText().toString();

                //int[] array = new int[10];
                int[] array = getIntent().getIntArrayExtra(MainActivity.EXAM_KEY); //is this the correct code???

                Log.i(TAG, "printing out contents of array");
                for (int i = 0; i < array.length; i++){
                    Log.i(TAG, "Result at Question " + (i+1) + " is: " + array[i]);
                }

                if(!TextUtils.isEmpty(name)==true/*name.length()!=0*/ && /*number.length()!=0*/!TextUtils.isEmpty(number)==true /*&& TextUtils.isDigitsOnly(number)*/){ //make sure that a name and number has been entered

                    Log.i(TAG, "Length of name: " + name.length()); //debugging
                    String email = getIntent().getStringExtra(MainActivity.EMAIL_KEY); //gets the email string from the MainActivity

                    Log.i(TAG, "The email is " + email);

                    //email the test results
                    String emailURI = "mailto:" + email;
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    //emailIntent.setData(Uri.parse(emailURI));
                    emailIntent.setData(Uri.parse("mailto:"));
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
                    String emailSubject = "";

                    for(int i = 0; i < 10; i++){ //goes through all of the student's answers in the identifier that was passed through explicit intents
                        Log.i(TAG, "" + array[i]); //debugging
                        //determine what to write in the email body
                        if(array[i]==1){
                            emailSubject += "" + (i+1) + ") A\n";
                        }else if(array[i]==2){
                            emailSubject += "" + (i+1) + ") B\n";
                        }else if(array[i]==3){
                            emailSubject += "" + (i+1) + ") C\n";
                        }else if(array[i]==4){
                            emailSubject += "" + (i+1) + ") D\n";
                        }else if(array[i]==5){
                            emailSubject += "" + (i+1) + ") E\n";
                        }else{
                            emailSubject += "" + (i+1) + ") N/A\n";
                        }
                    }

                    Log.i(TAG, "emailSubject is: " + emailSubject);

                    emailIntent.putExtra(Intent.EXTRA_TEXT, emailSubject);
                    //emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);

                    //emailIntent.setType("message/rfc822"); //this doesn't seem to help anything...

                    /*if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        //startActivity(emailIntent);
                        startActivity(Intent.createChooser(emailIntent, "Email Client..."));
                    }*/

                    startActivity(Intent.createChooser(emailIntent, "Email Client..."));
                }
            }
        });
    }
}

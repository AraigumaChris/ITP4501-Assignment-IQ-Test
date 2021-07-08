package com.example.itp4501_assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 3434;
    private Button btStart , btQLog , btTestLog , btDeleteRecord ;
    private ImageView animImage ;
    private AnimationDrawable originalAnim;

    FetchPageTask task = null;
    private String questions[] ;    //for storing the questions in the remote server
    private int answers[] ;         //for storing the answers of the questions in the remote server

    private String playTime , averageTime ,correctedAnswerNum ;
    private TextView tvLoading;
    private Boolean initialDB ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        /* connect with layout item */
        animImage = (ImageView) findViewById(R.id.pink);
        btStart = (Button) findViewById(R.id.btStart);
        btQLog = (Button) findViewById(R.id.btQLog);
        btTestLog = (Button) findViewById(R.id.btTLog);
        btDeleteRecord = (Button)findViewById(R.id.btDeleteRecord);
        tvLoading = (TextView) findViewById(R.id.tvLoading);

        /* make animation for imageView */

        playAnim();  // play the animation

        //for keeping the data in database so that will not clear after run the app
        SharedPreferences settings = getSharedPreferences("isInitialDB?", Context.MODE_PRIVATE);
        initialDB = settings.getBoolean("initialDB", false);
        if (!initialDB){
            initialDB();

        }

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().
                permitNetwork().build());



    }
    public void playAnim(){ // play the animation for imageView
        originalAnim = (AnimationDrawable) getResources().getDrawable(R.drawable.pink);
        originalAnim.setOneShot(false);
        originalAnim.setVisible(true, true);
        animImage.setImageDrawable(originalAnim);
        originalAnim.start();
        originalAnim.setVisible(true, true);
    }

    // initial the Database when first download or pressing delete record button
    public void initialDB() {
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.itp4501_assignment/Assigment",
                    null, SQLiteDatabase.CREATE_IF_NECESSARY);

            db.execSQL("DROP TABLE IF EXISTS QuestionsLog;");
            db.execSQL("CREATE TABLE QuestionsLog (questionNo INTEGER PRIMARY KEY  AUTOINCREMENT, question text, yourAnswer text, isCorrect text);");

            db.execSQL("DROP TABLE IF EXISTS TestsLog;");
            db.execSQL("CREATE TABLE TestsLog (testNo INTEGER PRIMARY KEY AUTOINCREMENT, testDateAndTime text, duration text , correctCount int );");

            db.close();
            initialDB = true;  //mark as initaled
        } catch (SQLiteException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /* when Start button was pressed */
    public void btStartOnClick(View v) {
        //disable all the button in activity main
        btStart.setEnabled(false);
        btQLog.setEnabled(false);
        btTestLog.setEnabled(false);
        btDeleteRecord.setEnabled(false);

        //show the Loading text during downloading question from the remote server
        tvLoading.setText(getString(R.string.loading));

        //download data from remote server and store into questions and answers array
        if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {
            task = new FetchPageTask();
            task.execute(getString(R.string.jsonURL));

        }

    }
    //change the screen to gameActivity
    // when game button was pressed and remoteserver successful downloaded   or Try again button was pressed
    public void changeGameIntent(){
        Intent i = new Intent(this, GameActivity.class);    //set next intent to gameActivity
        i.putExtra("questions" , questions);                        //send questions and answer array to game Activity
        i.putExtra("answer" , answers);
        startActivityForResult(i, REQUEST_CODE);                        //change the screen to gameActivity

    }

    //change the screen to gameResultActivity
    // when user finish answering all 5 question and press result button
    public void changeResultIntent(){
        Intent i = new Intent(this, GameResultActivity.class);              //set next intent to gameActivity
        i.putExtra("playTime" , playTime+"");                                  //send playTime averagePlayTIme and correctedAnswer Num to game Activity
        i.putExtra("averagePlayTime" ,averageTime+"");
        i.putExtra("correctedAnswerNum" , correctedAnswerNum+"");
        startActivityForResult(i, REQUEST_CODE);                                                 //change the screen
    }

    //change the screen to QuestionLog
    // when QuestionLog button is pressed
    public void btQuestionLogOnClick(View v){
        Intent i = new Intent(this, QuestionLogActivity.class);         //set next intent to questionLogActivity
        startActivityForResult(i, REQUEST_CODE);                                         //change the screen

    }
    //change the screen to TestsLog
    // when TestsLog button is pressed
    public void btTestLogOnClick(View v){
        Intent i = new Intent(this, TestLogActivity.class);                      //set next intent to TestsLogActivity
        startActivityForResult(i, REQUEST_CODE);                                                 //change the screen

    }
    // delete all the data in QuestionsLog and TestsLog table
    public void btDeleteRecordOnClick(View v){
        initialDB();
        Toast.makeText(this, getString(R.string.deleteMessage), Toast.LENGTH_LONG).show();      //show delete message

    }

    //download question and answer from remote server
    private class FetchPageTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... values) { // parameters not
            String result="";
            URL url = null;
            InputStream inputStream = null;

            try {
                url = new URL(getString(R.string.jsonURL));
                HttpURLConnection con = (HttpURLConnection)url.openConnection();

                con.setRequestMethod("GET");
                con.connect();

                inputStream = con.getInputStream();
                BufferedReader bufferedReader = new
                        BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                while((line = bufferedReader.readLine()) != null)
                    result += line;

                Log.d("doInBackground", "get data complete");
                inputStream.close();

            } catch (Exception e) {
                result=e.getMessage();
            }

            return result ;
        }

        // store the data into questions and answers array
        @Override
        protected void onPostExecute(String result) {

            try {

                JSONObject jOBj  = new JSONObject(result);
                JSONArray questionsArray = jOBj.getJSONArray("questions");
                questions = new String[questionsArray.length()];
                answers = new int[questionsArray.length()];

                for (int i = 0 ; i < questionsArray.length() ; i++){
                    JSONObject questionCode = questionsArray.getJSONObject(i);
                    questions[i] = questionCode.getString("question");
                    answers[i] = questionCode.getInt("answer");


                }


            } catch (Exception e) {
                String error = e.getMessage();
            }
            changeGameIntent();

        }

    }
    // when other Activity onFinish and send back data to MainActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data.hasExtra("playTime")){                             //when result in gameactivity was pressed
                playTime = data.getExtras().getString("playTime");                      //store extras into local variable
                averageTime =  data.getExtras().getString("averageTime");
                correctedAnswerNum= data.getExtras().getString("correctedAnswerNum");
                changeResultIntent();                                                           //run resultActivity
            }
            if (data.hasExtra("nextActivity")){                                     // when  button Titel screen or Try Again or Play  or barchart was pressed
                String nextActivity = data.getExtras().getString("nextActivity");
                if (nextActivity.equals("game")){                                           //if button try again or play  is pressed
                    btStart.setEnabled(false);                                              //disable all the button in activity main
                    btQLog.setEnabled(false);
                    btTestLog.setEnabled(false);
                    btDeleteRecord.setEnabled(false);
                    tvLoading.setText(getString(R.string.loading));
                    //show the Loading text
                    if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)) {           //if the play button is pressed first time and Start button didn't pressed before
                        task = new FetchPageTask();
                        task.execute(getString(R.string.jsonURL));
                    }
                    else { changeGameIntent();}                                 //change to game Intent

                }else {                                                         //when title screen button was pressed
                    btStart.setEnabled(true);                                //enable all the button in activity main
                    btQLog.setEnabled(true);
                    btTestLog.setEnabled(true);
                    btDeleteRecord.setEnabled(true);
                    tvLoading.setText("");                                   //clear loading text
                    playAnim();                                             // play animation
                }
                if (nextActivity.equals("barchart")){                           //when barchart button was press
                    Intent i = new Intent(this, TestLogBarCharActivity.class);
                    startActivityForResult(i, REQUEST_CODE);                                        //start barchart activity
                }else if(nextActivity.equals("piechart")){
                    Intent i = new Intent(this, TestLogPieChartActivity.class);
                    startActivityForResult(i, REQUEST_CODE);
                }
            }

        }
    }

    //the app was stopped
    @Override
    protected void onStop(){
        super.onStop();
        SharedPreferences settings = getSharedPreferences("isInitialDB?", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("initialDB", initialDB);                                                  //set initalDB as true  so that will not delete all the data in database after run the app next time
        editor.commit();
    }

}


package com.example.itp4501_assignment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {
    private long startTime = 0;             //for get the start time of test
    private String questions[] ;            //for storing the questions
    private int answers[] ;                 //for storing the answers
    private boolean usedQuestion[] ;        //for checking the question used or not
    private RadioButton rbChoiceFirst ,rbChoiceSecond ,rbChoiceThird ,rbChoiceFourth ;
    private TextView tvQustionWay , tvQuestion , tvResult  , tvCountDown , tvQuestionNumber , tvScore ;
    private int questionNumber = 0 ;        //for counting the total number of question answerd
    private int correctAnswerPlace ;        //for store the correct place of answer in the radiot button
    private int questionChoosing;           //choosing a question from question array
    private RadioGroup rgAnswer;
    private ContentValues contentValues = new ContentValues();
    private int correctedAnswerNum = 0 ;        //count the correct answer number
    private MediaPlayer correctMP ;
    private static final long COUNTDOWN_IN_MILLIS = 10000 ;
    private CountDownTimer countDownTimer ;
    private long timeLeftInMillis ;
    private ImageView answerCorrect ;
    private Drawable correct , wrong , timeout ;
private Timer timer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        startTime = System.currentTimeMillis();         //store the start time of this activity
        Intent intent = getIntent();

        rbChoiceFirst = (RadioButton) findViewById(R.id.rbChoiceFirst);
        rbChoiceSecond = (RadioButton) findViewById(R.id.rbChoiceSecond);
        rbChoiceThird = (RadioButton) findViewById(R.id.rbChoiceThird);
        rbChoiceFourth = (RadioButton) findViewById(R.id.rbChoiceFourth);

        tvQustionWay = (TextView) findViewById(R.id.tvQustionWay);
        tvQuestion = (TextView) findViewById(R.id.tvQustion);
        tvResult = (TextView) findViewById(R.id.tvResult);
        tvCountDown = (TextView) findViewById(R.id.tvCountDown);
        tvQuestionNumber = (TextView) findViewById(R.id.tvQustionNumber);
        tvScore = (TextView)findViewById(R.id.tvScore);


        rgAnswer = (RadioGroup) findViewById(R.id.rgAnswer);

        correctMP = MediaPlayer.create(this, R.raw.wow);  //sound effect for correct answer
        questions = intent.getStringArrayExtra("questions");      //get questionarray from Mainactivity and store in question array
        answers = intent.getIntArrayExtra("answer");            //get answerarray from Mainactivity and store in answer array
        usedQuestion = new boolean[questions.length];           //set the size of the usedquestion to same as questionarray

        answerCorrect = (ImageView) findViewById(R.id.answerPic);
        for (int i = 0 ; i < usedQuestion.length ; i++){
            usedQuestion[i] = false ;                       //set all the usedQuestion value as false to declare the question isn't used
        }
        questionMaking ();      //show question on screen

        correct = (Drawable) getResources().getDrawable(R.drawable.correct);
        wrong = (Drawable) getResources().getDrawable(R.drawable.wrong);
        timeout = (Drawable) getResources().getDrawable(R.drawable.timeout);



    }
    public void questionInitalization(){

    }
    public void questionMaking (){
        //disable next question button
        rbChoiceFirst.setChecked(false);
        rbChoiceSecond.setChecked(false);
        rbChoiceThird.setChecked(false);
        rbChoiceFourth.setChecked(false);


        while (true){
            questionChoosing = (int) (Math.random() * questions.length);
            if (usedQuestion[questionChoosing] == false)        //find an used question
                break ;
        }
        questionNumber++;
        tvQuestionNumber.setText(getString(R.string.gameQuestion) + ": " + questionNumber + "/5");
        tvScore.setText(getString(R.string.score) + ": " + correctedAnswerNum + "/5");
        tvQuestion.setText(getString(R.string.gameQuestion) +" " + questionNumber + " : " + questions[questionChoosing]); //show the question in the question textview
        usedQuestion[questionChoosing] = true ; //mark the question as used


        contentValues.clear();
        contentValues.put("question",questions[questionChoosing]);  //store question in to contenvalue

        for(int i = 0; i < rgAnswer.getChildCount(); i++){
            ((RadioButton)rgAnswer.getChildAt(i)).setEnabled(true);         //set all the radiobutton enable to click
        }
        correctAnswerPlace = (int) ((Math.random() * 4 )+ 1) ;          //random pick a radio button place to store the correct answer
        //generate 3 random number for 3 wrong answer radiobutton
        int randomAnswerOne = (int)( answers[questionChoosing] + Math.random() * 3 + 1);
        int randomAnswerTwo = (int)( answers[questionChoosing] - Math.random() * 3 - 1);
        int randomAnswerThree = (int)( answers[questionChoosing] + Math.random() * 4 -1);
        while (randomAnswerOne == randomAnswerThree){
            randomAnswerThree = (int)( answers[questionChoosing] + Math.random() * 4 - 1);
        }
        while (randomAnswerTwo == randomAnswerThree){
            randomAnswerThree = (int)( answers[questionChoosing] + Math.random() * 4 - 1);
        }
        while (randomAnswerOne == randomAnswerTwo){
            randomAnswerTwo = (int)( answers[questionChoosing] + Math.random() * 3 - 1);
        }
        while ( answers[questionChoosing]  == randomAnswerThree){
            randomAnswerThree = (int)( answers[questionChoosing] + Math.random() * 4 - 1);
        }
        switch (correctAnswerPlace){     //store correct answer and 3 wrong answer into radiobutton
            case 1 :
                rbChoiceFirst.setText(String.valueOf(answers[questionChoosing]));
                rbChoiceSecond.setText(String.valueOf(randomAnswerOne)  );
                rbChoiceThird.setText(String.valueOf(randomAnswerTwo) );
                rbChoiceFourth.setText(String.valueOf(randomAnswerThree)  );
                break;

            case 2 :
                rbChoiceFirst.setText(String.valueOf(randomAnswerOne));
                rbChoiceSecond.setText(String.valueOf(answers[questionChoosing] )  );
                rbChoiceThird.setText(String.valueOf(randomAnswerTwo) );
                rbChoiceFourth.setText(String.valueOf(randomAnswerThree)  );
                break;

            case 3 :
                rbChoiceFirst.setText(String.valueOf(randomAnswerOne));
                rbChoiceSecond.setText(String.valueOf(randomAnswerTwo)  );
                rbChoiceThird.setText(String.valueOf(answers[questionChoosing] ) );
                rbChoiceFourth.setText(String.valueOf(randomAnswerThree)  );
                break;
            case 4 :

                rbChoiceFirst.setText(String.valueOf(randomAnswerOne));
                rbChoiceSecond.setText(String.valueOf(randomAnswerTwo)  );
                rbChoiceThird.setText(String.valueOf(randomAnswerThree) );
                rbChoiceFourth.setText(String.valueOf(answers[questionChoosing] )  );
                break;

            default:
                break;

        }
        timeLeftInMillis = COUNTDOWN_IN_MILLIS;
        startCountDown();

    }
    private void startCountDown() {
        timer = new Timer(true);
            timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeLeftInMillis -=500;
                GameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateCountDownText();

                        if(timeLeftInMillis<=-500) {
                            timer.cancel();
                            updateCountDownText();
                            tvResult.setText(getString(R.string.timeOut));
                            tvResult.setTextColor(Color.RED);
                            contentValues.put("yourAnswer", "null");
                            contentValues.put("isCorrect", "no");
                            Vibrator vibrat = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrat.vibrate(500);
                            //countDownTimer.cancel();
                            answerCorrect.setImageDrawable(timeout);
                            btNextQuestionOnClick();
                        }
                    }
                });


            }
        }, 100, 500);
       
    }

    private void updateCountDownText(){
        int minutes = (int) (timeLeftInMillis /1000 ) /60 ;
        int seconds = (int) (timeLeftInMillis /1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
        tvCountDown.setText(timeFormatted);
        if (timeLeftInMillis < 5000){
            tvCountDown.setTextColor(Color.RED);
        }else {
            tvCountDown.setTextColor(Color.BLACK);
        }
    }

    public void choiceOneOnClick(View v){
        timer.cancel();
        //countDownTimer.cancel();
        if ( correctAnswerPlace == 1 ){
            tvResult.setText(getString(R.string.correct));
            tvResult.setTextColor(Color.GREEN);
            correctMP.start();  //when correct play sound effect
            correctedAnswerNum++;
            contentValues.put("yourAnswer",rbChoiceFirst.getText().toString());
            contentValues.put("isCorrect","yes");
            answerCorrect.setImageDrawable(correct);
            try {
                SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.itp4501_assignment/Assigment",
                        null, SQLiteDatabase.CREATE_IF_NECESSARY);
                int rowPosition =  (int)db.insert( "QuestionsLog",null,contentValues);
                db.close();
            } catch (SQLiteException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            btNextQuestionOnClick();
        }else {
            wrongChoice();
        }
    }
    public void choiceSecondOnClick(View v){
        timer.cancel();
//        countDownTimer.cancel();
        if ( correctAnswerPlace == 2 ){
            tvResult.setText(getString(R.string.correct));
            tvResult.setTextColor(Color.GREEN);
            correctMP.start();  //when correct play sound effect
            correctedAnswerNum++;
            contentValues.put("yourAnswer",rbChoiceFirst.getText().toString());
            contentValues.put("isCorrect","yes");
            answerCorrect.setImageDrawable(correct);
            try {
                SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.itp4501_assignment/Assigment",
                        null, SQLiteDatabase.CREATE_IF_NECESSARY);
                int rowPosition =  (int)db.insert( "QuestionsLog",null,contentValues);
                db.close();
            } catch (SQLiteException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            btNextQuestionOnClick();
        }else {
            wrongChoice();
        }
    }
    public void choiceThirdOnClick(View v){
        timer.cancel();
        //countDownTimer.cancel();
        if ( correctAnswerPlace == 3 ){
            tvResult.setText(getString(R.string.correct));
            tvResult.setTextColor(Color.GREEN);
            correctMP.start();  //when correct play sound effect
            correctedAnswerNum++;
            contentValues.put("yourAnswer",rbChoiceFirst.getText().toString());
            contentValues.put("isCorrect","yes");
            answerCorrect.setImageDrawable(correct);
            try {
                SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.itp4501_assignment/Assigment",
                        null, SQLiteDatabase.CREATE_IF_NECESSARY);
                int rowPosition =  (int)db.insert( "QuestionsLog",null,contentValues);
                db.close();
            } catch (SQLiteException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            btNextQuestionOnClick();
        }else {
            wrongChoice();
        }
    }
    public void choiceFourthOnClick(View v){
        timer.cancel();
//        countDownTimer.cancel();
        if ( correctAnswerPlace == 4 ){
            tvResult.setText(getString(R.string.correct));
            tvResult.setTextColor(Color.GREEN);
            correctMP.start();  //when correct play sound effect
            correctedAnswerNum++;
            contentValues.put("yourAnswer",rbChoiceFirst.getText().toString());
            contentValues.put("isCorrect","yes");
            answerCorrect.setImageDrawable(correct);
            try {
                SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.itp4501_assignment/Assigment",
                        null, SQLiteDatabase.CREATE_IF_NECESSARY);
                int rowPosition =  (int)db.insert( "QuestionsLog",null,contentValues);
                db.close();
            } catch (SQLiteException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            btNextQuestionOnClick();
        }else {
            wrongChoice();
        }
    }
    // when Submit button clicked
    public void wrongChoice(){
        tvResult.setText(getString(R.string.wrong));
        tvResult.setTextColor(Color.RED);
        int selectedId = rgAnswer.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(selectedId);
        contentValues.put("yourAnswer" , radioButton.getText().toString());
        contentValues.put("isCorrect","no");
        Vibrator vibrat = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrat.vibrate(500);  //when wrong vibrat the phone
        answerCorrect.setImageDrawable(wrong);
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.itp4501_assignment/Assigment",
                    null, SQLiteDatabase.CREATE_IF_NECESSARY);
            int rowPosition =  (int)db.insert( "QuestionsLog",null,contentValues);
            db.close();
        } catch (SQLiteException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        btNextQuestionOnClick();
    }

    // when Result / NextQuestion button clicked
    public void btNextQuestionOnClick() {

        if (questionNumber < 5){        //if user answered first four question

            questionMaking();           //move to next question
        }
        else {              //if user answered firth question
            //insert testdate and time , duration and corrected answer into testsLog table

            try {
                SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.itp4501_assignment/Assigment",
                        null, SQLiteDatabase.CREATE_IF_NECESSARY);

                Date date = new Date();
                date = new Timestamp(date.getTime());

                contentValues.clear();

                contentValues.put("testDateAndTime", String.valueOf(date)); //get finish time


                long finishTime = System.currentTimeMillis();
                int elapsedTime = (int) (finishTime-startTime)/1000;        //get duration
                contentValues.put("duration",elapsedTime + " " + getString(R.string.seconds));
                contentValues.put("correctCount" , correctedAnswerNum);     //get correctAnswer num
                int rowPosition =  (int)db.insert( "TestsLog",null,contentValues);
                contentValues.clear();
            } catch (SQLiteException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finish();   //send user to GameResultActivity
        }
    }


    @Override
    public void finish() {
        timer.cancel();
        long finishTime = System.currentTimeMillis();
        int elapsedTime = (int) (finishTime-startTime)/1000;
        double averageTime = (double) elapsedTime/5 ;
        Intent result = new Intent();
        //store play time , average time and corrected ANswer num into Intent and send user to GameResult Activity
        result.putExtra("playTime", elapsedTime+"");
        result.putExtra("averageTime", averageTime+"");
        result.putExtra("correctedAnswerNum",correctedAnswerNum+"");
        setResult(RESULT_OK, result);
        super.finish();



    }

}

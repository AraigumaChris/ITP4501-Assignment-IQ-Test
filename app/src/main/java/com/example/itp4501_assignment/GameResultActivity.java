package com.example.itp4501_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameResultActivity extends AppCompatActivity  {
    String playTime ;
    String correctedAnswerNum ;
    String averagePlayTime ;
    TextView tvTitle , tvTotalTimeTitle , tvTotalTime , tvAverageTimeTitle ,
            tvAverageTime , tvNumOfCorrectedTitle ,tvNumOfCorrected;
    Button btTryAgain , btTitleScreen;
    String nextActivity ="" ;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        Intent intent = getIntent();

        tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
        tvAverageTime = (TextView) findViewById(R.id.tvAverageTime);
        tvNumOfCorrected = (TextView) findViewById(R.id.tvNumOfCorrected);

        playTime = intent.getStringExtra("playTime");       //get data from Mainactivity
        correctedAnswerNum = intent.getStringExtra("correctedAnswerNum");
        averagePlayTime = intent.getStringExtra("averagePlayTime");

        //show the result of the test
        tvTotalTime.setText(playTime + " " + getString(R.string.seconds));
        tvNumOfCorrected.setText(correctedAnswerNum + " " + getString(R.string.correctQuestion));
        tvAverageTime.setText(averagePlayTime  + " "+getString(R.string.seconds));



    }
    // when user pressed try again
    public void btTryAgainOnClick(View v) {
        nextActivity = "game";      //send user to start game again
        finish();
    }
    //if user pressed title screen
    public void btTitleScreenOnClick(View v) {
        finish();
    }

    @Override
    public void finish() {
        Intent result = new Intent();
        //putExtra so that the app will send user to title screen or start the game again
        result.putExtra("nextActivity", nextActivity+"");
        setResult(RESULT_OK, result);
        super.finish();
    }

}

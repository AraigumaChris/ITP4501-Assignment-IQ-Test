package com.example.itp4501_assignment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class TestLogBarCharActivity extends AppCompatActivity {
    private LinearLayout layout;
    private Button btTryAgain, btTitleScreen;
    private String nextActivity = "";
    private static final float FRAME = 100;
    int testsLogNum;
    private int record[];

    class PanelView extends View {
        public PanelView(Context context) {
            super(context);
        }

        String title = getString(R.string.barChartTitle);  //title of the barchart
        String testNo = getString(R.string.testNoTitle);            //the description of the number
        final float scale = getResources().getDisplayMetrics().density;     //get the scale of the screen

        int correctCount;
        private int currentFrame;

        @Override
        public void onDraw(Canvas c) {
            super.onDraw(c);
            Paint paint = new Paint();  //decalre a Paint variable
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(R.drawable.gradient_background);
            c.drawPaint(paint);         //make the background whitecolor and FILL

            /* Title */
            paint.setColor(Color.BLACK);            //set text black color
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(20 * scale);      //set textsize
            paint.setTypeface(Typeface.SERIF);      //set the font
            float vertSpace = (float) (getHeight() * 0.05);  //the place in the height of the screen
            float horiSpace = (float) (getWidth() * 0.025);
            c.drawText(title, horiSpace, vertSpace, paint);       //draw title

            /* description */
            vertSpace = (float) (getHeight() * 0.95);
            horiSpace = (float) (getWidth() * 0.30);
            c.drawText(testNo, horiSpace, vertSpace, paint);

            vertSpace = (float) (getHeight() * 0.85);
            //get the spacebetween each of the number in the barchart
            float spaceBetweenNumber = (float) (getHeight() * 0.65 / 5);

            horiSpace = (float) (getWidth() * 0.01);
            paint.setTextSize(12* scale);
            for (int i = 1; i <= 5; i++) {
                /* bar chart value number */
                c.drawText(Integer.toString(i), horiSpace, vertSpace - (i * spaceBetweenNumber), paint);
                /* bar chart number line */
                paint.setStrokeWidth(3);
                paint.setAntiAlias(true);
                c.drawLine((float) (getWidth() * 0.05), vertSpace - (i * spaceBetweenNumber), (float) (getWidth() * 0.04), vertSpace - (i * spaceBetweenNumber), paint);
            }


            vertSpace = (float) (getHeight() * 0.85);
            horiSpace = (float) (getWidth() * 0.07);

            paint.setStrokeWidth(3);
            // make bar in the barcahrt
            paint.setColor(Color.RED);
            float range = (vertSpace-(vertSpace-spaceBetweenNumber*5))/FRAME;
            for (int i = 0; i < record.length * 2; i++) {

                if ((i & 1) == 1) {
                    if (record[i / 2] > currentFrame / (  FRAME / 5)) {
                        //     if (record[i / 2] > i / (10 * FRAME / 50))
                        //       c.drawRect(horiSpace, vertSpace - (record[i/2] * spaceBetweenNumber), (horiSpace + 10 * scale), vertSpace, paint);  /* barchart stick*/
                        //    else {
                        c.drawRect(horiSpace, vertSpace - (range*currentFrame), (horiSpace + 10 * scale), vertSpace, paint);  /* barchart stick*/

                    }else{
                        c.drawRect(horiSpace, vertSpace - (record[i / 2] * spaceBetweenNumber), (horiSpace + 10 * scale), vertSpace, paint);  /* barchart stick*/

                    }
                    horiSpace += getWidth() * 0.05;
                }
                // }
            }
            // draw bar which the height depend of the correctQuestionNumber


            horiSpace = (float) (getWidth() * 0.06)+5;
            for (int i = 0; i < record.length * 2; i++) {
                //draw the test number under the bar in the barchart
                if ((i & 1) == 1) {
                    float horiSpaceForNumber = (horiSpace + horiSpace + 1 * scale) / 2;
                    paint.setColor(Color.BLACK);
                    /*nuzmber under barchart stick */
                    c.drawText(Integer.toString(i / 2), horiSpaceForNumber, (float) (getHeight() * 0.89), paint);
                    horiSpace += getWidth() * 0.05; //move the next bar further so the bar will not in same place
                }
            }
            /* barchart border line */

            paint.setAntiAlias(true);


            vertSpace = (float) (getHeight() * 0.85);
            horiSpace = (float) (getWidth() * 0.05);
            //make the straight border line of the barchart
            c.drawLine(horiSpace, (float) (getHeight() * 0.1), horiSpace, vertSpace, paint);
            //make the horizontal border line of the barchart
            c.drawLine(horiSpace, vertSpace, getWidth(), vertSpace, paint);


            currentFrame++;
            if (currentFrame <= FRAME) {
                invalidate();
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.testlog_barchart);
        Intent intent = getIntent();
        btTitleScreen = (Button) findViewById(R.id.btTitleScreen);
        layout = (LinearLayout) findViewById(R.id.TestLogBarLayout); //declare the layout
        ArrayList<Integer> corrects = new ArrayList<>();

        try {
            Cursor cursor;
            //connect datanase
            SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.itp4501_assignment/Assigment",
                    null, SQLiteDatabase.OPEN_READONLY);
            //get the correctCount of the  in the testsLog Table where testNo same as the currect testno
            cursor = db.rawQuery("SELECT correctCount FROM TestsLog", null);
            while (cursor.moveToNext()) {
                corrects.add(cursor.getInt(cursor.getColumnIndex("correctCount")));
            }
            db.close();
        } catch (SQLiteException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        record = new int[corrects.size()];
        for (int i = 0; i < corrects.size(); i++) {
            record[i] = corrects.get(i);
        }
        PanelView panelView = new PanelView(this);  //delcare a  view of the barchart
        layout.addView(panelView);              //add the barchart into the layout

    }

    //if user pressed title screen
    public void btTitleScreenOnClick(View v) {
        finish();
    }

    // when user pressed Play
    public void btPlayOnClick(View v) {
        nextActivity = "game";  //send user to start game again
        finish();
    }

    @Override
    public void finish() {
        Intent result = new Intent();
        //putExtra so that the app will send user to title screen or start the game again
        result.putExtra("nextActivity", nextActivity + "");
        setResult(RESULT_OK, result);
        super.finish();
    }


}

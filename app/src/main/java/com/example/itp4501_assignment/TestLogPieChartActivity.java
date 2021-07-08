package com.example.itp4501_assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class TestLogPieChartActivity extends AppCompatActivity {

    private int[] degrees;
    private int count = 0;
    private float[] percentage;
    private int[] records;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_log_pie_chart);
        LinearLayout layout = findViewById(R.id.TestLogPieLayout);
        PieChartView view = new PieChartView();
        layout.addView(view);

        ArrayList<Integer> corrects = new ArrayList<>();
        degrees = new int[6];
        percentage= new float[6];
        records = new int[6];
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
        count = 0;
        //calculate ratio
        for (int i = 0; i < corrects.size(); i++) {
            int correct = corrects.get(i);
            if (correct <= 5) {
                records[correct]++;
                count++;
            }
        }
        //calculate degree
        for (int i = 0; i < 6; i++) {
            percentage[i] = records[i] *100f/ count;
            degrees[i] = Math.round( percentage[i]* 3.6f);
        }
    }

    class PieChartView extends View {
        private int rColor[] = {0xffff0000, 0xffffff00, 0xff32cd32, 0xff880055, 0xff0000ff, 0xff00ffff};


        private static final int FRAME = 90;
        private static final int MARGIN_START = 20;
        private static final int MARGIN_PIE = 30;
        private static final int BOX_SIZE = 30;
        private int currentFrame = 0;

        public PieChartView() {
            super(TestLogPieChartActivity.this);
        }

        public void onDraw(Canvas c) {
            super.onDraw(c);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);

            int currentDegree;
            int tmpI = currentFrame * (360 / FRAME);
            float cDegree = 0;
            boolean isPortrait = c.getWidth() < c.getHeight();
            int ref = isPortrait ? c.getWidth() : c.getHeight();
            for (int x = 0; x < 6; x++) {

                if (degrees[x] > 0) {
                    if (tmpI >= degrees[x]) {
                        tmpI -= degrees[x];
                        currentDegree = degrees[x];
                    } else {
                        currentDegree = tmpI;
                        tmpI = 0;
                    }
                } else {
                    continue;
                }
                float dawDegree = currentDegree;
                // define the diameter of the arc
                //
                paint.setColor(rColor[x]);

                RectF rec = new RectF(MARGIN_PIE, MARGIN_PIE, ref - MARGIN_PIE, ref - MARGIN_PIE);

                c.drawArc(rec, cDegree, dawDegree, true, paint);
                cDegree += dawDegree;

                if (tmpI <= 0) {
                    break;
                }
            }
            int vStart =isPortrait ?ref:0;
            int hStart =isPortrait ?0:ref;
            Point[] cellPosition = new Point[6];
            int cellHeight = isPortrait?(getHeight()-vStart)/3:getHeight()/3;
            int cellWidth = isPortrait?getWidth()/2:(getWidth()-hStart)/2;
            int centerStart = isPortrait?(vStart+cellHeight/2):cellHeight/2;

            for (int x = 0; x < 6; ) {
                for(int y=0;y<2;x++,y++){
                    cellPosition[x] = new Point(isPortrait?(y*cellWidth):hStart+(y*cellWidth),centerStart+x/2*cellHeight);
                    cellPosition[x].x +=MARGIN_START;
                    cellPosition[x].y -=BOX_SIZE/2;
                }
            }
            paint.setTextSize(BOX_SIZE/2);
            for (int x = 0; x < 6; x++) {
                paint.setColor(rColor[x]);
                paint.setStyle(Paint.Style.FILL);
                Point p = cellPosition[x];
                c.drawRect(p.x,p.y,p.x+BOX_SIZE,p.y+BOX_SIZE,paint);
                paint.setColor(Color.BLACK);
                c.drawText(String.format("%d correct(s) %.1f%% (%d)",x,percentage[x],records[x]),p.x+BOX_SIZE+MARGIN_START,p.y+BOX_SIZE/2+BOX_SIZE/4,paint);
            }
            if (currentFrame++ <= FRAME) {
                invalidate();
            }
            
        }

    }
}
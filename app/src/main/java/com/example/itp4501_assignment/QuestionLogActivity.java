package com.example.itp4501_assignment;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.Toast;
import android.database.Cursor;

import androidx.appcompat.app.AppCompatActivity;

public class QuestionLogActivity extends AppCompatActivity {
    private TableLayout tbData;
    private Result result;
    private String  nextActivity="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questionlog);
        Intent intent = getIntent();

        //decalre result tablelayout
        result = new Result(this, (TableLayout) findViewById(R.id.tbData));

        //connect database and store all the data from questionsLog table to tablelayout
        initialDB();
    }
    public void initialDB() {
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.itp4501_assignment/Assigment",
                    null, SQLiteDatabase.CREATE_IF_NECESSARY);



            //get all the data from questionsLog table to tablelayout
            result.fillTable(db.rawQuery("SELECT * FROM QuestionsLog", null));

            db.close();
        } catch (SQLiteException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    //if user pressed title screen
    public void btTitleScreenOnClick(View v) {
        finish();
    }

    // when user pressed Play
    public void btPlayOnClick(View v) {
        nextActivity = "game";   //send user to start game again
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

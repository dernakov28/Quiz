package com.example.vlad.quiz;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    public static final String GUESSES = "guesses";
    ResultsDbHelper dbHelper;

    ListView resultsListView;
    ArrayAdapter<String> adapter;
    ArrayList<String> results;
    TextView resultsTextView;
    Button saveResultsButton;

    SQLiteDatabase db;

    int correctAnswersCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        correctAnswersCount = intent.getIntExtra(GUESSES, 0);

        dbHelper = new ResultsDbHelper(this);
        db = dbHelper.getWritableDatabase();

        resultsTextView = (TextView) findViewById(R.id.resultsTextView);
        resultsTextView.setText(String.valueOf(correctAnswersCount));

        saveResultsButton = (Button) findViewById(R.id.saveResultsButton);
        saveResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);

                builder.setTitle(R.string.input_name);
                final EditText nameEditText = new EditText(ResultActivity.this);
                builder.setView(nameEditText);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ContentValues contentValues = new ContentValues();
                        String name = nameEditText.getText().toString();
                        if (name.equals("")) {
                            name = "Anonymous";
                        }
                        contentValues.put(ResultsDbHelper.KEY_NAME, name);
                        contentValues.put(ResultsDbHelper.KEY_VALUE, correctAnswersCount);
                        db.insert(dbHelper.TABLE_NAME, null, contentValues);
                        readResults();
                        adapter.notifyDataSetChanged();
                    }
                });

                builder.create().show();

                ((Button) view).setEnabled(false);
            }
        });

        results = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, R.layout.results_item, results);
        resultsListView = (ListView) findViewById(R.id.resultsListView);
        resultsListView.setAdapter(adapter);

        readResults();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void readResults() {
        adapter.clear();
        Cursor cursor = db.query(dbHelper.TABLE_NAME, null, null, null, null, null, ResultsDbHelper.KEY_VALUE + " DESC", "10");
        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(ResultsDbHelper.KEY_NAME);
            int valueIndex = cursor.getColumnIndex(ResultsDbHelper.KEY_VALUE);
            do {
                String value = String.valueOf(cursor.getInt(valueIndex));
                if (value.length() == 1) value = " " + value;
                adapter.add(value + "   " + cursor.getString(nameIndex));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

}

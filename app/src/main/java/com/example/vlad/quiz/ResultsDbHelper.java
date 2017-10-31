package com.example.vlad.quiz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by vlad on 23/10/17.
 */

public class ResultsDbHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "results";
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (name TEXT, " +
            "value INTEGER)";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "USA_Presidents";

    public static final String KEY_NAME = "name";
    public static final String KEY_VALUE = "value";

    public ResultsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

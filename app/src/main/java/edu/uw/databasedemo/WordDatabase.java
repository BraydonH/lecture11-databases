package edu.uw.databasedemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * A class for managing a database of words
 */
public class WordDatabase {

    private static final String TAG = "WordDB";

    //database details
    private static final String DATABASE_NAME = "words.db";
    private static final int DATABASE_VERSION = 1;

    //class cannot be instantiated
    private WordDatabase(){}

    /**
     * The schema and contract for the underlying database.
     */
    public static class WordEntry implements BaseColumns {
        //class cannot be instantiated
        private WordEntry(){}

        public static final String TABLE_NAME = "words";
        public static final String COL_WORD = "word";
        public static final String COL_COUNT = "count";
    }

    /**
     * A class to help open, create, and update the database
     */
    public static class DatabaseHelper extends SQLiteOpenHelper {

        //SQL for creating a table
        private static final String CREATE_TASKS_TABLE =
                "CREATE TABLE " + WordEntry.TABLE_NAME + "(" +
                        WordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "+
                        WordEntry.COL_WORD + " TEXT" + ","+
                        WordEntry.COL_COUNT + " INTEGER" +
                        ")";

        //SQL for dropping a table
        private static final String DROP_TASKS_TABLE = "DROP TABLE IF EXISTS "+ WordEntry.TABLE_NAME;


        public DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(TAG, "Creating tasks table");
            db.execSQL(CREATE_TASKS_TABLE); //create table if needed

            //add sample words
            ContentValues sample1 = new ContentValues();
            sample1.put(WordEntry.COL_WORD, "Embiggen");
            sample1.put(WordEntry.COL_COUNT, 0);
            ContentValues sample2 = new ContentValues();
            sample2.put(WordEntry.COL_WORD, "Cromulent");
            sample2.put(WordEntry.COL_COUNT, 0);

            db.insert(WordEntry.TABLE_NAME, null, sample1);
            db.insert(WordEntry.TABLE_NAME, null, sample2);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TASKS_TABLE); //just drop and recreate table
            onCreate(db);
        }
    }


}
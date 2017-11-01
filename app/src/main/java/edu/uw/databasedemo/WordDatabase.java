package edu.uw.databasedemo;

import android.provider.BaseColumns;

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

    //SQL for creating a table
    private static final String CREATE_TASKS_TABLE =
            "CREATE TABLE " + WordEntry.TABLE_NAME + "(" +
                    WordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "+
                    WordEntry.COL_WORD + " TEXT" + ","+
                    WordEntry.COL_COUNT + " INTEGER" +
                    ")";

    //SQL for dropping a table
    private static final String DROP_TASKS_TABLE = "DROP TABLE IF EXISTS "+ WordEntry.TABLE_NAME;

}
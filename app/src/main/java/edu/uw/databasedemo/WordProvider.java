package edu.uw.databasedemo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class WordProvider extends ContentProvider {

    private static final String TAG = "WordProvider";

    //Content Provider details
    private static final String AUTHORITY = "edu.uw.databasedemo.provider";
    private static final String WORD_RESOURCE = "words";

    //URI details
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"+WORD_RESOURCE);

    //integer values representing each supported resource Uri
    private static final int WORD_LIST_URI = 1; // /words
    private static final int WORD_SINGLE_URI = 2;// /words/:id

    private static final UriMatcher sUriMatcher; //for handling Uri requests

    static {
        //setup mapping between URIs and IDs
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, WORD_RESOURCE, WORD_LIST_URI);
        sUriMatcher.addURI(AUTHORITY, WORD_RESOURCE + "/#", WORD_SINGLE_URI);
    }

    private WordDatabase.DatabaseHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new WordDatabase.DatabaseHelper(getContext()); //initialize the helper
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(WordDatabase.WordEntry.TABLE_NAME);

        switch(sUriMatcher.match(uri)){
            case WORD_LIST_URI: //all words
                break; //no change
            case WORD_SINGLE_URI: //single word
                builder.appendWhere(WordDatabase.WordEntry._ID + "=" + uri.getLastPathSegment()); //restrict to that item
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }

        Cursor c = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        if(sUriMatcher.match(uri) != WORD_LIST_URI) {
            throw new IllegalArgumentException("Unknown insert URI "+uri);
        }

        if(!values.containsKey(WordDatabase.WordEntry.COL_WORD)){
            values.put(WordDatabase.WordEntry.COL_WORD, "");
        }

        if(!values.containsKey(WordDatabase.WordEntry.COL_COUNT)){
            values.put(WordDatabase.WordEntry.COL_COUNT, 0);
        }

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        long rowId = db.insert(WordDatabase.WordEntry.TABLE_NAME, null, values);
        if (rowId > 0) { //if successful
            Uri wordUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(wordUri, null);
            return wordUri; //return the URI for the entry
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        int count;
        switch (sUriMatcher.match(uri)) {
            case WORD_LIST_URI:
                count = db.update(WordDatabase.WordEntry.TABLE_NAME, values, selection, selectionArgs); //just pass in params
                break;
            case WORD_SINGLE_URI:
                String wordId = uri.getLastPathSegment();
                count = db.update(WordDatabase.WordEntry.TABLE_NAME, values, WordDatabase.WordEntry._ID + "=" + wordId //select by id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs); //apply params
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
        throw new SQLException("Failed to update row " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        int count;
        switch (sUriMatcher.match(uri)) {
            case WORD_LIST_URI:
                count = db.delete(WordDatabase.WordEntry.TABLE_NAME, selection, selectionArgs); //just pass in params
                break;
            case WORD_SINGLE_URI:
                String wordId = uri.getLastPathSegment();
                count = db.delete(WordDatabase.WordEntry.TABLE_NAME, WordDatabase.WordEntry._ID + "=" + wordId //select by id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs); //apply params
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
        throw new SQLException("Failed to update row " + uri);

    }


    @Override
    public String getType(Uri uri) {
        //return cursor types, per http://developer.android.com/guide/topics/providers/content-provider-creating.html#TableMIMETypes
        switch(sUriMatcher.match(uri)){
            case WORD_LIST_URI:
                return "vnd.android.cursor.dir/"+AUTHORITY+"."+WORD_RESOURCE;
            case WORD_SINGLE_URI:
                return "vnd.android.cursor.item/"+AUTHORITY+"."+WORD_RESOURCE;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }
    }
}
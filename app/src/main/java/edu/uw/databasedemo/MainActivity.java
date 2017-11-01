package edu.uw.databasedemo;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //list-model
        String[] data = {"Dog","Cat","Android","Inconceivable"};

        //list-view
        AdapterView listView = (AdapterView)findViewById(R.id.word_list_view);

        //list-controller
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_layout, //item to inflate
                null, //cursor to show
                new String[] {WordDatabase.WordEntry.COL_WORD, WordDatabase.WordEntry.COL_COUNT}, //fields to display
                new int[] {R.id.txt_item_word, R.id.txt_item_freq},                       //where to display them
                0); //flags
        listView.setAdapter(adapter);

        //load the data
        getSupportLoaderManager().initLoader(0, null, this);


        //handle button input
        final TextView inputText = (TextView)findViewById(R.id.txt_add_word);
        Button addButton = (Button)findViewById(R.id.btn_add_word);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputWord = inputText.getText().toString();
                Log.v(TAG, "To add: "+inputWord);

                addWord(inputWord); //call helper methods
            }
        });

        //handle item clicking
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor item = (Cursor)parent.getItemAtPosition(position); //item we clicked on
                String word = item.getString(item.getColumnIndexOrThrow( WordDatabase.WordEntry.COL_WORD ));
                int freq = item.getInt(item.getColumnIndexOrThrow( WordDatabase.WordEntry.COL_COUNT ));
                Log.v(TAG, "Clicked on '"+word+"' ("+freq+")");

                setFrequency(id, freq+1);
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = new String[] { WordDatabase.WordEntry.COL_WORD, WordDatabase.WordEntry.COL_COUNT, WordDatabase.WordEntry._ID };

        //create the CursorLoader
        CursorLoader loader = new CursorLoader(
                this,
                WordProvider.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //replace the data
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //empty the data
        adapter.swapCursor(null);
    }

    //adds (creates) a word to the list
    public void addWord(String word){
        ContentValues newValues = new ContentValues();
        newValues.put(WordDatabase.WordEntry.COL_WORD, word);
        newValues.put(WordDatabase.WordEntry.COL_COUNT, 100);

        Uri newUri = this.getContentResolver().insert(
                WordProvider.CONTENT_URI,   // the user dictionary content URI!
                newValues                   // the values to insert
        );
        Log.v(TAG, "New word at: "+newUri);
    }

    //sets (updates) the frequency of the word with the given id
    public void setFrequency(long id, int newFrequency){
        ContentValues newValues = new ContentValues();
        newValues.put( WordDatabase.WordEntry.COL_COUNT, newFrequency);

        this.getContentResolver().update(
                ContentUris.withAppendedId( WordProvider.CONTENT_URI, id),
                newValues,
                null, null); //no selection
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_test:

                WordDatabase.DatabaseHelper helper = new WordDatabase.DatabaseHelper(this);
                SQLiteDatabase db = helper.getReadableDatabase();

                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(WordDatabase.WordEntry.TABLE_NAME);

                Cursor results = builder.query(
                        db,
                        new String[] {WordDatabase.WordEntry.COL_WORD, WordDatabase.WordEntry.COL_COUNT},
                        null, null, null, null, null);

                while(results.moveToNext()){
                    String word = results.getString(results.getColumnIndexOrThrow( WordDatabase.WordEntry.COL_WORD )); //get the "word" field as a String
                    int freq = results.getInt(results.getColumnIndexOrThrow( WordDatabase.WordEntry.COL_COUNT ));
                    Log.v(TAG, "'"+word+"' ("+freq+")");
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

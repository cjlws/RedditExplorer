package com.supsim.redditexplorer;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.supsim.redditexplorer.Views.StatsTable;
import com.supsim.redditexplorer.data.Stat;
import com.supsim.redditexplorer.data.StatsRecordContract;

import java.util.ArrayList;

public class StatsActivity extends AppCompatActivity {

    StatsTable statsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        statsTable = (StatsTable) findViewById(R.id.stats_table_row_holder);
        fillTable(8);
        getAllStats();
    }

    private void fillTable(int rows){

        for(int i = 0; i < rows; i++){
//            Stat stat = new Stat(i+1, "Testing", i+22);
            statsTable.addStatRow(new Stat(i+1, "Testing", i+22));
        }
    }

    private void getAllStats(){
        //        newUri = context.getContentResolver().insert(StatsRecordContract.Stats.CONTENT_URI, contentValues);

//            Cursor cursor;
            Cursor cursor = getContentResolver().query(StatsRecordContract.Stats.CONTENT_URI,
                    null,
                    null,
                    null,
                    null,
                    null);  // This one is sort order so obviously needs to change...

            ArrayList<Stat> allStats = new ArrayList<Stat>();
            cursor.moveToFirst();
            for(int i = 0; i < cursor.getCount(); i++){
                allStats.add(new Stat(i+1, cursor.getString(1), cursor.getInt(2)));
            }
            Log.d("STATS PROVIDER", "There are a total of " + allStats.size() + " stats in the array");

        if(cursor != null && !cursor.isClosed()) cursor.close();
    }

}

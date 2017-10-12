package com.supsim.redditexplorer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.supsim.redditexplorer.Views.StatsTable;
import com.supsim.redditexplorer.data.Stat;
import com.supsim.redditexplorer.data.StatsRecordContract;

import java.util.ArrayList;

public class StatsActivity extends AppCompatActivity {

    StatsTable statsTable;
    private static final String TAG = "Stats Activity";
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Latest Stats");

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "PlaceHolder Action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        statsTable = (StatsTable) findViewById(R.id.stats_table_row_holder);
        ArrayList<Stat> allStats = getAllStats();

        if (allStats.isEmpty()) {
            Log.d(TAG, "There are no stats to display");
            displayNoStatsMessage();
        } else {
            Log.d(TAG, "There are " + allStats.size() + " stats to display");
//            fillTable(allStats.size());
            fillTable(allStats);
        }

        Stat topSub = allStats.get(0);
        configureFabButton(topSub.getSubreddit());

    }

    private void configureFabButton(String topSubreddit){
        final String textToDisplay = "I'm using Reddit Explorer and I've discovered that my most read sub is " + topSubreddit;
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, textToDisplay, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stats_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_all_stats) {
            deleteAllStats();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Function for filling the table with temp test data
    private void fillTable(int rows) {

        for (int i = 0; i < rows; i++) {
            statsTable.addStatRow(new Stat(i + 1, "Testing", i + 22));
        }
    }

    private void deleteAllStats() {
        //TODO Code to delete all stats and refresh display
        Toast.makeText(getApplicationContext(), "Deleting All Stats", Toast.LENGTH_LONG).show();
    }

    private void fillTable(ArrayList<Stat> stats) {
        for (Stat stat : stats) {
            Log.d(TAG, stat.toString());

            statsTable.addStatRow(stat);
        }
    }

    private void displayNoStatsMessage() {
        Toast.makeText(getApplicationContext(), "There are no stats to display, soz", Toast.LENGTH_LONG).show();
    }

    private ArrayList<Stat> getAllStats() {

        Cursor cursor = getContentResolver().query(StatsRecordContract.Stats.CONTENT_URI,
                null,
                null,
                null,
                StatsRecordContract.Stats.COL_STAT_COUNT + " DESC");  // This one is sort order so obviously needs to change...

        ArrayList<Stat> allStats = new ArrayList<Stat>();

        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                allStats.add(new Stat(i + 1, cursor.getString(1), cursor.getInt(2)));
                cursor.moveToNext();
            }
            Log.d("STATS PROVIDER", "There are a total of " + allStats.size() + " stats in the array");
        }

        if (cursor != null && !cursor.isClosed()) cursor.close();
        return allStats;
    }

}

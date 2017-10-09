package com.supsim.redditexplorer;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.supsim.redditexplorer.account.AccountGeneral;
import com.supsim.redditexplorer.data.RedditArticleContract;
import com.supsim.redditexplorer.data.StatsRecordContract;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, RedditItemTouchHelper.RedditItemTouchHelperListener {

    private static final String TAG = "ItemsListActivity";
    private RecyclerView mRecyclerView;
    private RedditAdapter mRedditAdapter;

    static final int COL_ID = 0;
    static final int COL_DOMAIN = 1;
    static final int COL_SUBREDDIT = 2;
    static final int COL_TITLE = 3;
    static final int COL_SCORE = 4;
    static final int COL_NSFW = 5;
    static final int COL_COMMENTS = 6;
    static final int COL_CREATED = 7;
    static final int COL_AUTHOR = 8;
    static final int COL_THUMBNAIL = 9;
    static final int COL_PERMALINK = 10;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate Ran");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action - will do", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        mRecyclerView = (RecyclerView)findViewById(R.id.item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        View emptyView = new View(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

//        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RedditItemTouchHelper(0, ItemTouchHelper.LEFT, this) {
//            @Override
//            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//
//                Log.d(TAG, "View was swiped " + direction);
//            }
//        };

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RedditItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        Cursor cursor = getContentResolver().query(RedditArticleContract.Articles.CONTENT_URI, null, null, null, null, null);

        mRedditAdapter = new RedditAdapter(getSupportFragmentManager(), cursor, mTwoPane);
        mRecyclerView.setAdapter(mRedditAdapter);


        Cursor statsCursor = getContentResolver().query(StatsRecordContract.Stats.CONTENT_URI, null, null, null, null, null);
        if (statsCursor != null){
            Log.d("STATS", "There are " + statsCursor.getCount() + " stats records");
        }


        //TODO Put in some control here so this doesn't fire every time the page is reloaded
        AccountGeneral.createSyncAccount(this);

        //TODO Come up with a better number or logic here

        int totalRecords = 0;
        if(cursor != null) {
            totalRecords = cursor.getCount();
            Log.d(TAG, "Total Records: " + totalRecords);
        }

        if(totalRecords < 10){
            SyncAdapter.performSync();
        }

//        if (cursor != null && !cursor.isClosed()) {
//            cursor.close();
//        }

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position){
        if(viewHolder instanceof RedditAdapter.RedditAdapterViewHolder){
//            mRedditAdapter.removeItem(viewHolder.getAdapterPosition());
//            mRedditAdapter.notifyDataSetChanged();
            //TODO Lots more to do here!

            Log.d(TAG, "POSITION: " + position);
            Log.d(TAG, "ADAP POSITION: " + viewHolder.getAdapterPosition() );

            String idToDelete = mRedditAdapter.getItemID(viewHolder.getAdapterPosition());

            Log.d(TAG, "Attempting to delete article with id " + idToDelete);
            int deleted = getContentResolver().delete(RedditArticleContract.Articles.CONTENT_URI,
                    RedditArticleContract.Articles.COL_ID + " = ?", new String[]{idToDelete});

            Log.d(TAG, deleted + " rows deleted");
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.action_view_stats){
            Intent openStats = new Intent(this, StatsActivity.class);
            startActivity(openStats);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        Log.d(TAG, "On Create Loader Ran");
        return new CursorLoader(
                getApplicationContext(),
                RedditArticleContract.Articles.CONTENT_URI,
                null,  //TODO Only return the columns needed using new String[]{"col1", "Col2", etc}
                null,
                null,
                null
        );
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        Log.d(TAG, "On Load Finished Ran");
        if(data != null){
            Log.d(TAG, "Data Size: " + data.getCount());
            //TODO Put in control to manually swap cursor in case user is in the middle of reading
            mRedditAdapter.swapCursor(data);
        } else {
            Log.d(TAG, "Incoming cursor was null");
        }
    }

    public void onLoaderReset(Loader<Cursor> loader){
        Log.d(TAG, "On Loader Reset Ran");
        mRedditAdapter.swapCursor(null);
    }
}

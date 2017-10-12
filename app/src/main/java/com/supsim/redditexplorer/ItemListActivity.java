package com.supsim.redditexplorer;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import com.google.firebase.analytics.FirebaseAnalytics;
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

    //TODO Enhance Stats Database.  Need to make it so that a. articles are only recorded once and b. if they have been dismissed then don't show them again



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

    boolean showPhoneTutorial;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static final String PREFS_NAME = "REPrefs";
    public static final String PHONE_TUTORIAL_PREF_LABEL = "phoneTutorial";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate Ran");
        super.onCreate(savedInstanceState);


        //TODO Move to better place
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        showPhoneTutorial = sharedPreferences.getBoolean(PHONE_TUTORIAL_PREF_LABEL, true);


        setContentView(R.layout.activity_item_list);

        // Obtain the Firebase Analytics instance
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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

        if(mTwoPane) showPhoneTutorial = false;

        mRecyclerView = (RecyclerView)findViewById(R.id.item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        View emptyView = new View(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RedditItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        Cursor cursor = getContentResolver().query(RedditArticleContract.Articles.CONTENT_URI, null, null, null, null, null);

            mRedditAdapter = new RedditAdapter(getSupportFragmentManager(), cursor, mTwoPane);
            mRecyclerView.setAdapter(mRedditAdapter);


        // Code to check if the tablet is displaying an article or if it is blank
        // If blank then display a handy tutorial page in the blank space
        if(mRedditAdapter != null && mRedditAdapter.hasFragmentManager() && mTwoPane){
//            Log.d("ILA", "Reddit Adpater has Fragment Manager");
//            if(mTwoPane) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.item_detail_container);
                if(!(fragment instanceof ItemDetailFragment)){
                    Log.d("TESTING", "\r\n -- IT WAS NOT AN INSTANCE --\r\n");
                    TabletTutorialFragment tabletTutorialFragment = new TabletTutorialFragment();
                    try{
                        getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, tabletTutorialFragment).commit();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
//        }


        Cursor statsCursor = getContentResolver().query(StatsRecordContract.Stats.CONTENT_URI, null, null, null, null, null);
        if (statsCursor != null){
            Log.d("STATS", "There are " + statsCursor.getCount() + " stats records");
        }


        //TODO Put in some control here so this doesn't fire every time the page is reloaded
        AccountGeneral.createSyncAccount(this);

        //TODO Come up with a better number or logic here

//        int totalRecords = 0;
//        if(cursor != null) {
//            totalRecords = cursor.getCount();
//            Log.d(TAG, "Total Records: " + totalRecords);
//        }
//
//        if(totalRecords < 10){
//            SyncAdapter.performSync();
//        }

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

            if(idToDelete.equals("tutorial")){
                Log.d("TESTING", "Attempting to delete the tutorial");
                //TODO Add a firebase Log to say tutorial completed
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
//                showPhoneTutorial = sharedPreferences.getBoolean(PHONE_TUTORIAL_PREF_LABEL, true);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PHONE_TUTORIAL_PREF_LABEL, false);
                editor.apply();
                showPhoneTutorial = true;  //TODO change this when testing complete
            } else {

            Log.d(TAG, "Attempting to delete article with id " + idToDelete);
            int deleted = getContentResolver().delete(RedditArticleContract.Articles.CONTENT_URI,
                    RedditArticleContract.Articles.COL_ID + " = ?", new String[]{idToDelete});

            Log.d(TAG, deleted + " rows deleted");
        }
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
            if(!showPhoneTutorial) {
                mRedditAdapter.swapCursor(data);
            } else {
                String[] columns = new String[]{
                        RedditArticleContract.Articles.COL_ID,
                        RedditArticleContract.Articles.COL_DOMAIN,
                        RedditArticleContract.Articles.COL_SUBREDDIT,
                        RedditArticleContract.Articles.COL_TITLE,
                        RedditArticleContract.Articles.COL_SCORE,
                        RedditArticleContract.Articles.COL_NSFW,
                        RedditArticleContract.Articles.COL_COMMENTS,
                        RedditArticleContract.Articles.COL_CREATED,
                        RedditArticleContract.Articles.COL_AUTHOR,
                        RedditArticleContract.Articles.COL_PERMALINK,
                        RedditArticleContract.Articles.COL_THUMBNAIL};

                MatrixCursor matrixCursor = new MatrixCursor(columns);

                matrixCursor.addRow(new Object[]{
                        "tutorial",
                        "Domain",
                        "WelcomeToRedditExplorer",
                        "Hi, I see you're new here.  Welcome!\r\n\r\n" +
                                "Reddit articles are displayed here in a scrolling list.\r\n" +
                                "If something looks interesting just tap it to read it.\r\n" +
                                "If something doesn't look interesting then swipe it to the left to get rid of it.\r\n" +
                                "As you read and swipe, stats are built up on your reading habits.\r\n" +
                                "To view your stats just tap the link at the top of the screen\r\n\r\n" +
                                "To get rid of this guide just swipe it off to the left.  Go on, give it a go :)",
                        123,
                        "False",
                        567,
                        34567890,
                        "Author",
                        "Permalink",
                        "https://b.thumbs.redditmedia.com/y5nDZyxlBjrPFP1RfsDjRH-5SOfg5QBhOUYERMNfnWQ.jpg"
                });  //TODO Add Tutorial Articles

                MergeCursor mergeCursor = new MergeCursor(new Cursor[]{matrixCursor, data});

                Log.d("TESTING", "SHOW PHONE TUTORIAL");

//                showPhoneTutorial = false;
                mRedditAdapter.swapCursor(mergeCursor);
            }
        } else {
            Log.d(TAG, "Incoming cursor was null");
        }
    }

    public void onLoaderReset(Loader<Cursor> loader){
        Log.d(TAG, "On Loader Reset Ran");
        mRedditAdapter.swapCursor(null);
    }
}

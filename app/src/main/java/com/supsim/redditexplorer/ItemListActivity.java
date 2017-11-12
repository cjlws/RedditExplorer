package com.supsim.redditexplorer;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.supsim.redditexplorer.account.AccountGeneral;
import com.supsim.redditexplorer.data.ActionedArticleContract;
import com.supsim.redditexplorer.data.RedditArticleContract;
import com.supsim.redditexplorer.data.StatsRecordContract;


public class ItemListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, RedditItemTouchHelper.RedditItemTouchHelperListener {

    private static final String TAG = "ItemsListActivity";
    private RedditAdapter mRedditAdapter;
    private RecyclerView.AdapterDataObserver adapterDataObserver;

    static final int COL_ID = 0;
    static final int COL_SUBREDDIT = 2;
    static final int COL_TITLE = 3;
    static final int COL_AUTHOR = 8;

    static final int STAT_COL_VIEWS = 2;

    //Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
    private boolean mTwoPane;

    //Whether or not to display the phone tutorial
    boolean showPhoneTutorial;

    private FirebaseAnalytics mFirebaseAnalytics;

    //Shared preferences settings
    public static final String PREFS_NAME = "REPrefs";
    public static final String PHONE_TUTORIAL_PREF_LABEL = "phoneTutorial";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        showPhoneTutorial = sharedPreferences.getBoolean(PHONE_TUTORIAL_PREF_LABEL, true);


        setContentView(R.layout.activity_item_list);

        // Obtain the Firebase Analytics instance
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Set the title for the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
        }

        if (mTwoPane) showPhoneTutorial = false;

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.item_list);
        final TextView emptyMessageTextView = (TextView)findViewById(R.id.emptyReadingListTextMessage);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        // Adds the main left and right swipes to the items in the list
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RedditItemTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        Cursor cursor = getContentResolver().query(RedditArticleContract.Articles.CONTENT_URI, null, null, null, null, null);

        mRedditAdapter = new RedditAdapter(getSupportFragmentManager(), cursor, mTwoPane);
        adapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(mRedditAdapter != null && isAdapterEmpty()){

                    if(emptyMessageTextView.getVisibility() == View.GONE) emptyMessageTextView.setVisibility(View.VISIBLE);
                } else {
                    if(emptyMessageTextView.getVisibility() == View.VISIBLE) emptyMessageTextView.setVisibility(View.GONE);
                }
            }
        };

        mRecyclerView.setAdapter(mRedditAdapter);



        // Code to check if the tablet is displaying an article or if it is blank
        // If blank then display a handy tutorial page in the blank space
        if (mRedditAdapter != null && mRedditAdapter.hasFragmentManager() && mTwoPane) {

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.item_detail_container);
            if (!(fragment instanceof ItemDetailFragment)) {
                TabletTutorialFragment tabletTutorialFragment = new TabletTutorialFragment();
                try {
                    getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, tabletTutorialFragment).commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        AccountGeneral.createSyncAccount(this);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof RedditAdapter.RedditAdapterViewHolder) {

            String id = mRedditAdapter.getItemID(viewHolder.getAdapterPosition());

            if (id.equals("tutorial")) {

                recordTutorialCompleted();

            } else {

                recordSwipe(direction, id, position);
            }
        }
    }

    private void recordTutorialCompleted(){
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, null);
        storeBoolean(PHONE_TUTORIAL_PREF_LABEL, false);
        showPhoneTutorial = false;
        mRedditAdapter.notifyDataSetChanged();
        getLoaderManager().restartLoader(0, null, this);
    }

    private void storeBoolean(String label, boolean value){
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(label, value);
        editor.apply();
    }

    private void recordSwipe(int direction, String id, int position){

            ContentValues contentValues = new ContentValues();
            contentValues.put(ActionedArticleContract.Actioned_Articles.COL_ACTIONED_REDDIT_ID, id);


            if(direction == ItemTouchHelper.LEFT) {

                Log.d(TAG, "Logging downvote for article with id " + id);
                contentValues.put(ActionedArticleContract.Actioned_Articles.COL_ACTIONED_TYPE, ActionedArticleContract.Actioned_Articles.ACTIONED_TYPE_DELETED);

            } else if (direction == ItemTouchHelper.RIGHT){

                Cursor cursor = mRedditAdapter.getCursor();
                if(cursor != null){
                cursor.moveToPosition(position);

                String subreddit = cursor.getString(COL_SUBREDDIT);

                Log.d(TAG, "Logging upvote for article with id " + id + " and sub of " + subreddit);
                recordVisit(subreddit);
                contentValues.put(ActionedArticleContract.Actioned_Articles.COL_ACTIONED_TYPE, ActionedArticleContract.Actioned_Articles.ACTIONED_TYPE_READ);
            }
            }

            getContentResolver().delete(RedditArticleContract.Articles.CONTENT_URI,
                RedditArticleContract.Articles.COL_ID + " = ?", new String[]{id});
            getContentResolver().insert(ActionedArticleContract.Actioned_Articles.CONTENT_URI, contentValues);
    }


    private void recordVisit(String subreddit) {


        boolean visitRecorded = false;
        final String tempTag = "RECORD_VISIT";

        String selection = StatsRecordContract.Stats.COL_STAT_SUBREDDIT + " = ?";
        String[] selectionArgs = new String[]{subreddit};
        Cursor cursor = getContentResolver().query(StatsRecordContract.Stats.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null);

        if (cursor != null) {
            int test = cursor.getCount();

            if (test > 0) {

                cursor.moveToFirst();
                int currentCount = cursor.getInt(STAT_COL_VIEWS);
                Log.d(tempTag, subreddit + " was already in the database with a score of " + currentCount);

                int rows = Tools.increaseSubsCount(getApplicationContext(), cursor);
                Log.d(tempTag, "A total of " + rows + " were updated");
                if (rows > 0) visitRecorded = true;
            } else {
                Log.d(tempTag, "No record found for " + subreddit);
                Uri newUri = Tools.addNewSubToDatabase(getApplicationContext(), subreddit);
                long newID = ContentUris.parseId(newUri);
                Log.d(tempTag, "Added " + subreddit + " and got back ID of " + newID);
                if (newUri != null) visitRecorded = true;
            }
        }
        if (cursor != null && !cursor.isClosed()) cursor.close();
        if (visitRecorded) Log.d(tempTag, "Visit to " + subreddit + " was recorded");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(mRedditAdapter != null && adapterDataObserver != null) mRedditAdapter.registerAdapterDataObserver(adapterDataObserver);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mRedditAdapter != null && adapterDataObserver != null) mRedditAdapter.unregisterAdapterDataObserver(adapterDataObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_view_stats) {
            Intent openStats = new Intent(this, StatsActivity.class);
            startActivity(openStats);
            return true;
        } else if (id == R.id.action_refresh_articles){
            SyncAdapter.performSync();
        }
        return super.onOptionsItemSelected(item);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getApplicationContext(),
                RedditArticleContract.Articles.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    private boolean isAdapterEmpty(){
        return mRedditAdapter.getItemCount() == 0;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (!showPhoneTutorial) {
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
                        RedditArticleContract.Articles.COL_THUMBNAIL,
                        RedditArticleContract.Articles.COL_PREVIEWS};

                MatrixCursor matrixCursor = new MatrixCursor(columns);

                matrixCursor.addRow(new Object[]{
                        "tutorial",
                        "Domain",
                        "WelcomeToRedditExplorer",
                        getString(R.string.tutorial_phone_body),
                        123,
                        0,
                        567,
                        34567890,
                        "Author",
                        "Permalink",
                        "",
                        ""
                });

                MergeCursor mergeCursor = new MergeCursor(new Cursor[]{matrixCursor, data});
                mRedditAdapter.swapCursor(mergeCursor);
            }
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mRedditAdapter.swapCursor(null);
    }
}

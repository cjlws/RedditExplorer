package com.supsim.redditexplorer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.supsim.redditexplorer.account.AccountGeneral;
import com.supsim.redditexplorer.data.RedditArticle;
import com.supsim.redditexplorer.data.RedditArticleContract;
import com.supsim.redditexplorer.dummy.DummyContent;

import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ItemsListActivity";
    private RecyclerView mRecyclerView;
    private RedditAdapter mRedditAdapter;

    static final int COL_ID = 0;
    static final int COL_SUBREDDIT = 1;
    static final int COL_TITLE = 2;
    static final int COL_AUTHOR = 3;


//    private RedditArticleObserver redditArticleObserver;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

//    SimpleCursorAdapter mAdapter;
//    SuperSimpleViewAdapter adapter;




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

        mRecyclerView = (RecyclerView)findViewById(R.id.item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        View emptyView = new View(this);
        mRecyclerView.setHasFixedSize(true);

        mRedditAdapter = new RedditAdapter(getApplicationContext(),
                new RedditAdapter.RedditAdapterOnClickHandler() {
                    @Override
                    public void onClick(Long date, RedditAdapter.RedditAdapterViewHolder viewHolder) {

                    }
                }, emptyView, 0);  //TODO Get rid of choice mode
        mRecyclerView.setAdapter(mRedditAdapter);

//        View recyclerView = findViewById(R.id.item_list);
//        assert recyclerView != null;

//        mAdapter = new SimpleCursorAdapter(getApplicationContext(),
//                android.R.layout.simple_list_item_2, null,
//                new String[] {RedditArticleContract.PATH_ARTICLES})

//        mAdapter = new SuperSimpleViewAdapter(this, android.R.layout.simple_list_item_2, null, )

//        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

//        redditArticleObserver = new RedditArticleObserver();


        //TODO Put in some control here so this doesn't fire every time the page is reloaded
        AccountGeneral.createSyncAccount(this);

        Cursor cursor = getContentResolver().query(RedditArticleContract.Articles.CONTENT_URI, null, null, null, null, null);
        int totalRecords = cursor.getCount();
        Log.d(TAG, "Total Records: " + totalRecords);

        //TODO Come up with a better number or logic here
        if(totalRecords < 3){
            SyncAdapter.performSync();
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onStart(){
        super.onStart();
//        getContentResolver().registerContentObserver(RedditArticleContract.Articles.CONTENT_URI, true, redditArticleObserver);
    }

    @Override
    protected void onStop(){
        super.onStop();
//        if(redditArticleObserver != null){
//            getContentResolver().unregisterContentObserver(redditArticleObserver);
//        }
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
            //Toast.makeText(ItemListActivity.this, "Display Stats Selected", Toast.LENGTH_LONG).show();
            Intent openStats = new Intent(this, StatsActivity.class);
            startActivity(openStats);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


//    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
////        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS));
//        recyclerView.setAdapter(new SuperSimpleViewAdapter(this, android.R.layout.simple_list_item_2, null, null, null, 0));
//    }

//    public class SuperSimpleViewAdapter extends RecyclerView.Adapter<SuperSimpleViewAdapter.ViewHolder>{
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//            public RedditArticle redditArticle;
//            public final TextView mIdView;
//            public final TextView mContentView;
//
//            public ViewHolder(View view){
//                super(view);
//
//                mIdView = (TextView) view.findViewById(R.id.id);
//                mContentView = (TextView) view.findViewById(R.id.content);
//            }
//
//            @Override
//            public String toString() {
//                return super.toString() + " '" + mContentView.getText() + "'";
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return articles.size();
//        }
//
//        private List<RedditArticle> articles;
//
//        public SuperSimpleViewAdapter(List<RedditArticle> articles){
//            this.articles = articles;
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_content, parent, false);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(final ViewHolder holder, int position){
//            holder.redditArticle = articles.get(position);
//            holder.mIdView.setText(articles.get(position).getTitle());
//            holder.mContentView.setText(articles.get(position).getLink());
//
//            //TODO onclick goes in here
//        }
//    }


//    public class SimpleItemRecyclerViewAdapter
//            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {
//
//        private final List<DummyContent.DummyItem> mValues;
//
//        public SimpleItemRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
//            mValues = items;
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_list_content, parent, false);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(final ViewHolder holder, int position) {
//            holder.mItem = mValues.get(position);
//            holder.mIdView.setText(mValues.get(position).id);
//            holder.mContentView.setText(mValues.get(position).content);
//
//            holder.mView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mTwoPane) {
//                        Bundle arguments = new Bundle();
//                        arguments.putString(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
//                        ItemDetailFragment fragment = new ItemDetailFragment();
//                        fragment.setArguments(arguments);
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.item_detail_container, fragment)
//                                .commit();
//                    } else {
//                        Context context = v.getContext();
//                        Intent intent = new Intent(context, ItemDetailActivity.class);
//                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
//
//                        context.startActivity(intent);
//                    }
//                }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return mValues.size();
//        }
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//            public final View mView;
//            public final TextView mIdView;
//            public final TextView mContentView;
//            public DummyContent.DummyItem mItem;
//
//            public ViewHolder(View view) {
//                super(view);
//                mView = view;
//                mIdView = (TextView) view.findViewById(R.id.id);
//                mContentView = (TextView) view.findViewById(R.id.content);
//            }
//
//            @Override
//            public String toString() {
//                return super.toString() + " '" + mContentView.getText() + "'";
//            }
//        }
//    }

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
            mRedditAdapter.swapCursor(data);
        } else {
            Log.d(TAG, "Incoming cursor was null");
        }
//        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader){
        Log.d(TAG, "On Loader Reset Ran");
        mRedditAdapter.swapCursor(null);
    }

//    private void refreshArticles(){
//        //TODO this is firing once for each entry into the database.  Not cool.
//        Log.d(TAG, "Call to Refresh Articles");
//    }

//    private final class RedditArticleObserver extends ContentObserver {
//        private RedditArticleObserver() {
//            super(new Handler(Looper.getMainLooper()));
//        }
//
//        @Override
//        public void onChange(boolean selfChange, Uri uri){
//            refreshArticles();
//        }
//    }
}

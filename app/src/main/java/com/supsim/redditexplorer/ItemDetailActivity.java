package com.supsim.redditexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.supsim.redditexplorer.data.RedditArticle;


public class ItemDetailActivity extends AppCompatActivity {

    private static final String TAG = "ItemDetailActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        RedditArticle redditArticle = new RedditArticle(getIntent().getExtras());

        final String shareString = getString(
                R.string.share_article_title,
                redditArticle.getShortenedTitle(20),
                Tools.getAbsoluteLink(getString(R.string.domain_stub), redditArticle.getPermalink())
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(Tools.getShareOnClickListener(ItemDetailActivity.this,
                getString(R.string.share_article_subject),
                shareString, getString(R.string.share_article_chooser_title)));


        // Show the Navigate Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(true);

        }

        if (savedInstanceState == null) {

            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Log.d(TAG, "IDA created the following reddit article: " + redditArticle.toString());

            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(redditArticle.getRedditArticleBundle());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, ItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

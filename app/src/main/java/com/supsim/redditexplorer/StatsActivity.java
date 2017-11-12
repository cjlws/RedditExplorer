package com.supsim.redditexplorer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.supsim.redditexplorer.data.Stat;
import com.supsim.redditexplorer.data.StatsRecordContract;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    FloatingActionButton floatingActionButton;
    public static final String FROM_WIDGET_KEY = "from_widget";

    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new StatsChartFragment(), "Chart");
        adapter.addFragment(new StatsTableFragment(), "Table");
        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager){
            super(manager);
        }

        @Override
        public Fragment getItem(int position){
            return mFragmentList.get(position);
        }

        @Override
        public int getCount(){
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle passedIn = getIntent().getExtras();
        if(passedIn != null && passedIn.containsKey(FROM_WIDGET_KEY) && passedIn.getBoolean(FROM_WIDGET_KEY)){

            // Log when a user clicks on the widget to lauch stats
            Bundle firebaseBundle = new Bundle();
            firebaseBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Stats Screen");
            firebaseBundle.putString(FirebaseAnalytics.Param.ITEM_ID, "widget");

            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, firebaseBundle);

        } else {
            // Log when a user comes from the reading list page
            Bundle firebaseBundle = new Bundle();
            firebaseBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Stats Screen");
            firebaseBundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itemslist");

            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, firebaseBundle);
        }

        setContentView(R.layout.activity_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setTitle(getString(R.string.stats_activity_page_title));
            actionBar.setDisplayHomeAsUpEnabled(true);

        }

        updateFragments();

    }

    private void updateFragments(){

        ArrayList<Stat> allStats = getAllStats();

        ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);

        if(viewPager == null){

            //For tablets
            StatsTableFragment tableFragment = new StatsTableFragment();
            StatsChartFragment chartFragment = new StatsChartFragment();

            getSupportFragmentManager().beginTransaction().
                    replace(R.id.stats_table_layout_holder, tableFragment).commit();
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.stats_chart_layout_holder, chartFragment).commit();

        } else {

            //For phones
            setupViewPager(viewPager);

            TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
        }



        if(allStats.isEmpty()){
            configureFabButton(null);
        } else {
            configureFabButton(getTopStat(allStats).getSubreddit());
        }



    }

    private Stat getTopStat(ArrayList<Stat> allStats){
        return allStats.get(0);
    }

    private void configureFabButton(String topSubreddit){
        final String textToDisplay;
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        if(topSubreddit != null){
            textToDisplay = getString(R.string.share_stats_top_subreddit,
                    Tools.addRToSubreddit(topSubreddit));
        } else {
            textToDisplay = getString(R.string.share_stats_top_subreddit_no_data);
        }



        floatingActionButton.setOnClickListener(Tools.getShareOnClickListener(
                        StatsActivity.this,
                        getString(R.string.share_stats_top_subreddit_subject),
                        textToDisplay,
                        getString(R.string.share_stats_chooser_title)
                ));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stats_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_clear_all_stats:
                deleteAllStats();
                return true;
            case android.R.id.home:
                navigateUpTo(new Intent(this, ItemListActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void deleteAllStats() {

        int rows = getApplicationContext().getContentResolver().delete(StatsRecordContract.Stats.CONTENT_URI, null, null);

        if(rows > 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.stats_action_confirmation_delete_all, rows), Toast.LENGTH_LONG).show();
            updateFragments();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.stats_action_error_delete_all), Toast.LENGTH_LONG).show();
        }
    }


    private ArrayList<Stat> getAllStats() {

        Cursor cursor = getContentResolver().query(StatsRecordContract.Stats.CONTENT_URI,
                null,
                null,
                null,
                StatsRecordContract.Stats.COL_STAT_COUNT + " DESC");

        ArrayList<Stat> allStats = new ArrayList<>();

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

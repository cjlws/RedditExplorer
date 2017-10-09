package com.supsim.redditexplorer.data;

import android.net.Uri;

public class StatsRecordContract {
    public static final String CONTENT_AUTHORITY = "com.supsim.redditexplorer.statsync";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_STATS = "stats";

    static final String STAT_DB_NAME = "stats_db";
    static final int STAT_DB_VERSION = 3;

    public static abstract class Stats {
        public static final String NAME = "stats";
        public static final String COL_STAT_ID = "statsId";
        public static final String COL_STAT_SUBREDDIT = "statsSubreddit";
        public static final String COL_STAT_COUNT = "statsCount";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATS).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_URI + "/" + PATH_STATS;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_URI + "/" + PATH_STATS;
    }
}

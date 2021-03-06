package com.supsim.redditexplorer.data;


import android.net.Uri;

public class ActionedArticleContract {
    static final String CONTENT_AUTHORITY = "com.supsim.redditexplorer.articlesync";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_ACTIONED_ARTICLES = "actionedArticles";

    static final String ACTIONED_ARTICLES_DB_NAME = "actioned_db";
    static final int ACTIONED_ARTICLES_DB_VERSION = 3;

    public static abstract class Actioned_Articles {
        static final String NAME = "actionedArticles";
        static final String COL_ACTIONED_ID = "actionedId";
        public static final String COL_ACTIONED_REDDIT_ID = "actionedRedditId";
        public static final String COL_ACTIONED_TYPE = "actionedType";

        public static final String ACTIONED_TYPE_DELETED = "deleted";
        public static final String ACTIONED_TYPE_READ = "read";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ACTIONED_ARTICLES).build();
        static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_URI + "/" + PATH_ACTIONED_ARTICLES;
        static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item" + CONTENT_URI + "/" + PATH_ACTIONED_ARTICLES;

    }
}

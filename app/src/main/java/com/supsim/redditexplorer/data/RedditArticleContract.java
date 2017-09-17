package com.supsim.redditexplorer.data;

import android.net.Uri;


public class RedditArticleContract {
    public static final String CONTENT_AUTHORITY = "com.supsim.redditexplorer.sync";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ARTICLES = "articles";

    static final String DB_NAME = "articles_db";
    static final int DB_VERSION = 1;

    public static abstract class Articles {
        public static final String NAME = "articles";
        public static final String COL_ID = "articleId";
        public static final String COL_DOMAIN = "articleDomain";
        public static final String COL_SUBREDDIT = "articleSubreddit";
        public static final String COL_TITLE = "articleTitle";
        public static final String COL_SCORE = "articleScore";
        public static final String COL_NSFW = "articleNSFW";
        public static final String COL_COMMENTS = "articleComments";
        public static final String COL_CREATED = "articleCreated";
        public static final String COL_AUTHOR = "articleAuthor";
        public static final String COL_THUMBNAIL = "articleThumbnail";
        public static final String COL_PERMALINK = "articlePermalink";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTICLES).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_URI + "/" + PATH_ARTICLES;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_URI + "/" + PATH_ARTICLES;
    }
}

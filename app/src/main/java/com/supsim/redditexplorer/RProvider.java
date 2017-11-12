package com.supsim.redditexplorer;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.supsim.redditexplorer.data.DatabaseClient;
import com.supsim.redditexplorer.data.RedditArticleContract;

public class RProvider extends ContentProvider {

    private static final int ARTICLE = 1;
    private static final int ARTICLE_ID = 2;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(RedditArticleContract.CONTENT_AUTHORITY, RedditArticleContract.PATH_ARTICLES, ARTICLE);
        uriMatcher.addURI(RedditArticleContract.CONTENT_AUTHORITY, RedditArticleContract.PATH_ARTICLES + "/#", ARTICLE_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate(){
        this.database = DatabaseClient.getInstance(getContext()).getDatabase();
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri){
        switch (uriMatcher.match(uri)){
            case ARTICLE:
                return RedditArticleContract.Articles.CONTENT_TYPE;
            case ARTICLE_ID:
                return RedditArticleContract.Articles.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("URI was invalid");
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (uriMatcher.match(uri)){
            case ARTICLE:
                cursor = database.query(RedditArticleContract.Articles.NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ARTICLE_ID:
                long _id = ContentUris.parseId(uri);
                cursor = database.query(RedditArticleContract.Articles.NAME,
                        projection,
                        RedditArticleContract.Articles.COL_ID + "=?",
                        new String[] {String.valueOf(_id)},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("URI was invalid");
        }

        assert getContext() != null;
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values){
        Uri returnUri;
        long _id;

        switch (uriMatcher.match(uri)){
            case ARTICLE:
                _id = database.insert(RedditArticleContract.Articles.NAME, null, values);
                returnUri = ContentUris.withAppendedId(RedditArticleContract.Articles.CONTENT_URI, _id);
                break;
            default:
                throw new IllegalArgumentException("URI was invalid");
        }

        assert getContext() != null;
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs){
        int rows;
        switch (uriMatcher.match(uri)){
            case ARTICLE:
                rows = database.delete(RedditArticleContract.Articles.NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI was invalid");
        }
        if (rows != 0){
            assert getContext() != null;
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs){
        int rows;
        switch (uriMatcher.match(uri)){
            case ARTICLE:
                rows = database.update(RedditArticleContract.Articles.NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI was invalid");

        }
        if (rows != 0){
            assert getContext() != null;
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }
}

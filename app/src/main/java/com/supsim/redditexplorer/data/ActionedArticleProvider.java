package com.supsim.redditexplorer.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class ActionedArticleProvider extends ContentProvider {

    private static final int ARTICLE = 1;
    private static final int ARTICLE_ID = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ActionedArticleContract.CONTENT_AUTHORITY, ActionedArticleContract.PATH_ACTIONED_ARTICLES, ARTICLE);
        uriMatcher.addURI(ActionedArticleContract.CONTENT_AUTHORITY, ActionedArticleContract.PATH_ACTIONED_ARTICLES + "/#", ARTICLE_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        this.database = ActionedArticleDatabaseClient.getInstance(getContext()).getDatabase();
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ARTICLE:
                return ActionedArticleContract.Actioned_Articles.CONTENT_TYPE;
            case ARTICLE_ID:
                return ActionedArticleContract.Actioned_Articles.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Uri was invalid");
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case ARTICLE:
                cursor = database.query(ActionedArticleContract.Actioned_Articles.NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ARTICLE_ID:
                long _id = ContentUris.parseId(uri);
                cursor = database.query(ActionedArticleContract.Actioned_Articles.NAME,
                        projection,
                        ActionedArticleContract.Actioned_Articles.COL_ACTIONED_ID + "=?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Uri was invalid");
        }
        assert getContext() != null;
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        Uri returnUri;
        long _id;

        switch (uriMatcher.match(uri)) {
            case ARTICLE:
                _id = database.insert(ActionedArticleContract.Actioned_Articles.NAME, null, values);
                returnUri = ContentUris.withAppendedId(ActionedArticleContract.Actioned_Articles.CONTENT_URI, _id);
                break;
            default:
                throw new IllegalArgumentException("Uri was invalid");

        }

        assert getContext() != null;
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int rows;
        switch (uriMatcher.match(uri)) {
            case ARTICLE:
                rows = database.delete(ActionedArticleContract.Actioned_Articles.NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Uri was invalid");
        }
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return 0;  //Update should never be needed
    }

}

package com.supsim.redditexplorer.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.supsim.redditexplorer.TopThreeWidgetProvider;

public class StatsProvider extends ContentProvider {

    private static final int STAT = 1;
    private static final int STAT_ID = 2;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(StatsRecordContract.CONTENT_AUTHORITY, StatsRecordContract.PATH_STATS, STAT);
        uriMatcher.addURI(StatsRecordContract.CONTENT_AUTHORITY, StatsRecordContract.PATH_STATS + "/#", STAT_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate(){
        this.database = StatsDatabaseClient.getInstance(getContext()).getDatabase();
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri){
        switch (uriMatcher.match(uri)){
            case STAT:
                return StatsRecordContract.Stats.CONTENT_TYPE;
            case STAT_ID:
                return StatsRecordContract.Stats.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Uri was invalid");
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){

        Cursor cursor;
        switch (uriMatcher.match(uri)){
            case STAT:
                cursor = database.query(StatsRecordContract.Stats.NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case STAT_ID:
                long _id = ContentUris.parseId(uri);
                cursor = database.query(StatsRecordContract.Stats.NAME,
                        projection,
                        StatsRecordContract.Stats.COL_STAT_ID + "=?",
                        new String[] {String.valueOf(_id)},
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
    public Uri insert(@NonNull Uri uri, ContentValues values){

        Uri returnUri;
        long _id;

        switch (uriMatcher.match(uri)){
            case STAT:
                _id = database.insert(StatsRecordContract.Stats.NAME, null, values);
                returnUri = ContentUris.withAppendedId(StatsRecordContract.Stats.CONTENT_URI, _id);
                break;
            default:
                throw new IllegalArgumentException("URI was invalid");
        }

        assert getContext() != null;

        TopThreeWidgetProvider.sendRefreshBroadcast(getContext());

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs){

        int rows;
        switch (uriMatcher.match(uri)){
            case STAT:
                rows = database.delete(StatsRecordContract.Stats.NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI was invalid");
        }
        if (rows !=0){
            assert getContext() != null;
            TopThreeWidgetProvider.sendRefreshBroadcast(getContext());
        }
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs){

        int rows;
        switch (uriMatcher.match(uri)){
            case STAT:
                rows = database.update(StatsRecordContract.Stats.NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI was invalid");
        }
        if (rows !=0){
            assert getContext() != null;
            TopThreeWidgetProvider.sendRefreshBroadcast(getContext());
        }
        return rows;
    }



}

package com.supsim.redditexplorer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.supsim.redditexplorer.data.StatsRecordContract;


public class Tools {

    protected static Uri addNewSubToDatabase(Context context, String subreddit) {
        Log.d("STATS New", "Adding " + subreddit + " to database");
        Uri newUri;
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsRecordContract.Stats.COL_STAT_SUBREDDIT, subreddit);
        contentValues.put(StatsRecordContract.Stats.COL_STAT_COUNT, 1);

        newUri = context.getContentResolver().insert(StatsRecordContract.Stats.CONTENT_URI, contentValues);

        return newUri;
    }

    protected static int increaseSubsCount(Context context, Cursor cursor) {
        Log.d("STATS ADD", "Upping the score...");
//        int currentCountIndex = cursor.getColumnIndex(StatsRecordContract.Stats.COL_STAT_COUNT);
        int currentCount = cursor.getInt(ItemDetailFragment.COL_SCORE);
//        int idIndex = cursor.getColumnIndex(StatsRecordContract.Stats.COL_STAT_ID);
        int id = cursor.getInt(ItemDetailFragment.COL_ID);

        String selection = StatsRecordContract.Stats.COL_STAT_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        ContentValues newValues = new ContentValues();
        newValues.put(StatsRecordContract.Stats.COL_STAT_COUNT, currentCount + 1);

        int rowsUpdated = 0;

        rowsUpdated = context.getContentResolver().update(
                StatsRecordContract.Stats.CONTENT_URI,
                newValues,
                selection,
                selectionArgs
        );

        return rowsUpdated;
    }


}

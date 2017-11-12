package com.supsim.redditexplorer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;

import com.supsim.redditexplorer.data.StatsRecordContract;

import java.util.Date;
import java.util.Locale;


public class Tools {


    static String getAbsoluteLink(String domainStub, String link){
        return domainStub + removeXMLStringEncoding(link);
    }

    static Uri addNewSubToDatabase(Context context, String subreddit) {
        Uri newUri;
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatsRecordContract.Stats.COL_STAT_SUBREDDIT, subreddit);
        contentValues.put(StatsRecordContract.Stats.COL_STAT_COUNT, 1);

        newUri = context.getContentResolver().insert(StatsRecordContract.Stats.CONTENT_URI, contentValues);

        return newUri;
    }

    static Intent createBrowserIntent(String link){
        return new Intent(Intent.ACTION_VIEW).setData(Uri.parse(removeXMLStringEncoding(link)));
    }

    private static Intent createShareIntent(String shareTitle, String shareBody){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareTitle);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        return sharingIntent;
    }

    static View.OnClickListener getShareOnClickListener(final Context context,
                                                               final String shareTitle,
                                                               final String shareBody,
                                                               final String chooserTitle){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(Intent.createChooser(createShareIntent(shareTitle, shareBody), chooserTitle));
            }
        };
    }

    static int increaseSubsCount(Context context, Cursor cursor) {
        int currentCount = cursor.getInt(ItemDetailFragment.COL_SCORE);
        int id = cursor.getInt(ItemDetailFragment.COL_ID);

        String selection = StatsRecordContract.Stats.COL_STAT_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        ContentValues newValues = new ContentValues();
        newValues.put(StatsRecordContract.Stats.COL_STAT_COUNT, currentCount + 1);


        return context.getContentResolver().update(
                StatsRecordContract.Stats.CONTENT_URI,
                newValues,
                selection,
                selectionArgs
        );
    }


    public static String removeXMLStringEncoding(String string){

        return string.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
    }

    public static String addRToSubreddit(String subreddit){

        return "r\u2215" + removeXMLStringEncoding(subreddit);
    }

    static String formatAuthorAndTime(Context context, String author, int time) {
        String formatedTime = formatTime(context, time);
        return String.format(Locale.getDefault(), context.getString(R.string.post_author_and_time_format), formatedTime, author);
    }

    private static String formatTime(Context context, int created) {

        long currentTimestamp = new Date().getTime();
        long elapsed = (currentTimestamp / 1000) - created;

        if (elapsed >= 60 * 60 * 24) {

            // Elapsed time is in the realm of days

            int numberOfDays = (int) (elapsed / (60 * 60 * 24));
            if (numberOfDays == 1) {
                return context.getString(R.string.tools_format_time_day, numberOfDays);
            } else {
                return context.getString(R.string.tools_format_time_days, numberOfDays);
            }
        } else if (elapsed >= 60 * 60) {

            int numberOfHours = (int) (elapsed / (60 * 60));
            int numberOfMinutes = (int) ((elapsed - (numberOfHours * 60 * 60)) / 60);

            String hourString;
            String minuteString;

            if (numberOfHours == 1) {
                hourString = context.getString(R.string.tools_format_time_hour, numberOfHours);
            } else {
                hourString = context.getString(R.string.tools_format_time_hours, numberOfHours);
            }

            if (numberOfMinutes == 1) {
                minuteString = context.getString(R.string.tools_format_time_minute, numberOfMinutes);
            } else {
                minuteString = context.getString(R.string.tools_format_time_minutes, numberOfMinutes);
            }

            return context.getString(R.string.tools_format_time_hours_and_minutes, hourString, minuteString);

        } else if (elapsed >= 60) {

            int numberOfMinutes = (int) (elapsed / 60);
            int numberOfSeconds = (int) (elapsed - (numberOfMinutes * 60));

            String minuteString;
            String secondString;

            if (numberOfMinutes == 1) {
                minuteString = context.getString(R.string.tools_format_time_minute, numberOfMinutes);
            } else {
                minuteString = context.getString(R.string.tools_format_time_minutes, numberOfMinutes);
            }

            if (numberOfSeconds == 1) {
                secondString = context.getString(R.string.tools_format_time_second, numberOfSeconds);
            } else {
                secondString = context.getString(R.string.tools_format_time_seconds, numberOfSeconds);
            }

            return context.getString(R.string.tools_format_time_minutes_and_seconds, minuteString, secondString);

        } else if (elapsed > 0 && elapsed < 60) {

            if (elapsed == 1) {
                return context.getString(R.string.tools_format_time_second, elapsed);
            } else {
                return context.getString(R.string.tools_format_time_seconds, elapsed);
            }

        }
        return String.valueOf(created);
    }

    static String formatScore(int score) {
        if (score < 1000) {
            return String.valueOf(score);
        } else {
            return String.format(Locale.getDefault(), "%.2fK", ((double) score / 1000));
        }
    }

}


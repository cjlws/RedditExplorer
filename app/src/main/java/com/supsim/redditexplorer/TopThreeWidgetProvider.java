package com.supsim.redditexplorer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import com.supsim.redditexplorer.data.StatsRecordContract;

public class TopThreeWidgetProvider extends AppWidgetProvider {

    private static final String REFRESH_ACTION = "com.supsim.redditexplorer.appwidget.action.REFRESH";
    private static final int SUBREDDIT_COLUMN = 0;
    private static final int VIEWS_COLUMN = 1;

    public static void sendRefreshBroadcast(Context context){
        Intent intent = new Intent(REFRESH_ACTION);
        intent.setComponent(new ComponentName(context, TopThreeWidgetProvider.class));
        context.sendBroadcast(intent);

    }

    @Override
    public void onReceive(final Context context, Intent intent){
        final String action = intent.getAction();

        if(REFRESH_ACTION.equals(action)){

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, TopThreeWidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            onUpdate(context, appWidgetManager, allWidgetIds);

        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){

        super.onUpdate(context, appWidgetManager, appWidgetIds);

        ComponentName thisWidget = new ComponentName(context, TopThreeWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for(int widgetId : allWidgetIds){

            updateWidget(context, widgetId, appWidgetManager);

        }
    }

    private void updateWidget(Context context, int widgetId, AppWidgetManager appWidgetManager){

        String[] projection = new String[]{StatsRecordContract.Stats.COL_STAT_SUBREDDIT, StatsRecordContract.Stats.COL_STAT_COUNT};
        String limit = StatsRecordContract.Stats.COL_STAT_COUNT + " DESC LIMIT 3";  // Only return the top three stats

        Cursor cursor = context.getContentResolver().query(StatsRecordContract.Stats.CONTENT_URI, projection, null, null, limit);

        Intent intent;
        RemoteViews remoteViews;

        if(cursor != null && cursor.getCount() == 3){
            Log.d("WIDGET", "There are enough results to draw the widget proper");

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_top_three_layout);

            if(cursor.moveToFirst()){
                remoteViews.setTextViewText(R.id.widgetFirstPlace, cursor.getString(SUBREDDIT_COLUMN));
                remoteViews.setTextViewText(R.id.widgetFirstPlaceViews, cursor.getString(VIEWS_COLUMN));
            }

            if(cursor.moveToNext()){
                remoteViews.setTextViewText(R.id.widgetSecondPlace, cursor.getString(SUBREDDIT_COLUMN));
                remoteViews.setTextViewText(R.id.widgetSecondPlaceViews, cursor.getString(VIEWS_COLUMN));
            }

            if(cursor.moveToNext()){
                remoteViews.setTextViewText(R.id.widgetThirdPlace, cursor.getString(SUBREDDIT_COLUMN));
                remoteViews.setTextViewText(R.id.widgetThirdPlaceViews, cursor.getString(VIEWS_COLUMN));
            }

            intent = new Intent(context, StatsActivity.class);
            intent.putExtra(StatsActivity.FROM_WIDGET_KEY, true);

        } else {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_top_three_layout_empty);
            intent = new Intent(context, ItemListActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);
        appWidgetManager.updateAppWidget(widgetId, remoteViews);

        if(cursor != null && !cursor.isClosed()) cursor.close();
    }

}

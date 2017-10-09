package com.supsim.redditexplorer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.supsim.redditexplorer.data.StatsRecordContract.STAT_DB_NAME;
import static com.supsim.redditexplorer.data.StatsRecordContract.STAT_DB_VERSION;

public class StatsDatabaseClient extends SQLiteOpenHelper {

    private static volatile StatsDatabaseClient instance;
    private final SQLiteDatabase database;

    private StatsDatabaseClient(Context context){
        super(context, STAT_DB_NAME, null, STAT_DB_VERSION);
        this.database = getWritableDatabase();
    }

    public static StatsDatabaseClient getInstance(Context context){
        if(instance == null){
            synchronized (StatsDatabaseClient.class){
                if(instance == null){
                    instance = new StatsDatabaseClient(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database){
        createStatsTable(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        database.execSQL("DROP TABLE IF EXISTS [" + StatsRecordContract.Stats.NAME + "];");
        onCreate(database);
    }

    public SQLiteDatabase getDatabase(){ return database;}

    private void createStatsTable(SQLiteDatabase database){
        database.execSQL("CREATE TABLE [" + StatsRecordContract.Stats.NAME + "] ([" +
                StatsRecordContract.Stats.COL_STAT_ID + "] INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, [" +
                StatsRecordContract.Stats.COL_STAT_SUBREDDIT + "] TEXT, [" +
                StatsRecordContract.Stats.COL_STAT_COUNT + "] NUMBER);"
        );
    }
}
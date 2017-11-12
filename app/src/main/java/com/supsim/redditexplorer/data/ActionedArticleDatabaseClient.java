package com.supsim.redditexplorer.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.supsim.redditexplorer.data.ActionedArticleContract.ACTIONED_ARTICLES_DB_NAME;
import static com.supsim.redditexplorer.data.ActionedArticleContract.ACTIONED_ARTICLES_DB_VERSION;


class ActionedArticleDatabaseClient extends SQLiteOpenHelper {

    private static volatile ActionedArticleDatabaseClient instance;
    private final SQLiteDatabase database;

    private ActionedArticleDatabaseClient(Context context){
        super(context, ACTIONED_ARTICLES_DB_NAME, null, ACTIONED_ARTICLES_DB_VERSION);
        this.database = getWritableDatabase();
    }

    public static ActionedArticleDatabaseClient getInstance(Context context){
        if(instance == null){
            synchronized (ActionedArticleDatabaseClient.class){
                if(instance == null){
                    instance = new ActionedArticleDatabaseClient(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database){
        createActionedArticlesTable(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        database.execSQL("DROP TABLE IF EXISTS [" + ActionedArticleContract.Actioned_Articles.NAME + "];");
        onCreate(database);
    }

    public SQLiteDatabase getDatabase(){return database;}

    private void createActionedArticlesTable(SQLiteDatabase database){
        database.execSQL("CREATE TABLE [" + ActionedArticleContract.Actioned_Articles.NAME + "] ([" +
        ActionedArticleContract.Actioned_Articles.COL_ACTIONED_ID + "] INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, [" +
        ActionedArticleContract.Actioned_Articles.COL_ACTIONED_REDDIT_ID + "] TEXT, [" +
        ActionedArticleContract.Actioned_Articles.COL_ACTIONED_TYPE + "] TEXT);");
    }
}

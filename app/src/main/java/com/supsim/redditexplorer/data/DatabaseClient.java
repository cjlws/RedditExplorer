package com.supsim.redditexplorer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.supsim.redditexplorer.data.RedditArticleContract.DB_NAME;
import static com.supsim.redditexplorer.data.RedditArticleContract.DB_VERSION;

/**
 * Created by johnrobinson on 10/09/2017.
 */

public class DatabaseClient extends SQLiteOpenHelper {

    private static volatile DatabaseClient instance;
    private final SQLiteDatabase database;

    private DatabaseClient(Context context){
        super(context, DB_NAME, null, DB_VERSION);
        this.database = getWritableDatabase();
    }

    public static DatabaseClient getInstance(Context context){
        if(instance == null){
            synchronized (DatabaseClient.class){
                if (instance == null){
                    instance = new DatabaseClient(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database){
        createArticlesTable(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        database.execSQL("DROP TABLE IF EXISTS [" + RedditArticleContract.Articles.NAME + "];");
        onCreate(database);
    }

    public SQLiteDatabase getDatabase(){
        return database;
    }

    private void createArticlesTable(SQLiteDatabase database){
        database.execSQL("CREATE TABLE [" + RedditArticleContract.Articles.NAME + "] ([" +
                RedditArticleContract.Articles.COL_ID + "] TEXT UNIQUE PRIMARY KEY, [" +  //TODO Add autoincrement
                RedditArticleContract.Articles.COL_DOMAIN + "] TEXT, [" +
                RedditArticleContract.Articles.COL_SUBREDDIT + "] TEXT, [" +
                RedditArticleContract.Articles.COL_TITLE + "] TEXT, [" +
                RedditArticleContract.Articles.COL_SCORE + "] NUMBER, [" +
                RedditArticleContract.Articles.COL_NSFW + "] NUMBER, [" +
                RedditArticleContract.Articles.COL_COMMENTS + "] NUMBER, [" +
                RedditArticleContract.Articles.COL_CREATED + "] NUMBER, [" +
                RedditArticleContract.Articles.COL_AUTHOR + "] TEXT, [" +
                RedditArticleContract.Articles.COL_THUMBNAIL + "] TEXT, [" +
                RedditArticleContract.Articles.COL_PERMALINK + "] TEXT);"



        );
    }
}

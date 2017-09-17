package com.supsim.redditexplorer;


import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.supsim.redditexplorer.account.AccountGeneral;
import com.supsim.redditexplorer.data.RedditArticle;
import com.supsim.redditexplorer.data.RedditArticleContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";
    private ContentResolver mContentResolver;
    private static final String url = "https://www.reddit.com/.json";

    public SyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
        this.mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient providerClient, SyncResult syncResult){
        Log.d(TAG, "On Perform Sync Ran");

        //TODO Move parsing out to own class
        try {
            String jsonFeed = download(url);

            JSONObject jsonObject = new JSONObject(jsonFeed);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONArray children = dataObject.getJSONArray("children");

            Vector<ContentValues> allVector = new Vector<ContentValues>(children.length());

            for(int i = 0; i < children.length(); i++){
                JSONObject child = children.getJSONObject(i);
                JSONObject article = child.getJSONObject("data");

                String domain = article.getString("domain");            // The domain the link points to
                String subreddit = article.getString("subreddit");      // The name of the subreddit it has been posted under
                String id = article.getString("id");                    // The unique reddit ID
                String title = article.getString("title");              // The title of the post
                String score = article.getString("score");              // Total score for the post
                int nsfw = convertNSFW(article.getBoolean("over_18"));  // NSFW or not - converted to int to make storage easier
                int num_comments = article.getInt("num_comments");      // Number of comments available
                long created = article.getLong("created_utc");          // Timestamp of the creation of the post
                String author = article.getString("author");            // reddit username of the author
                String thumbnail = article.getString("thumbnail");      // scaled thumbnail to accompany the link.  There are also _height and _width fields available
                String permalink = article.getString("permalink");      // link to the reddit article page - relative link

                RedditArticle redditArticle = new RedditArticle(
                        domain,
                        subreddit,
                        id,
                        title,
                        score,
                        nsfw,
                        num_comments,
                        created,
                        author,
                        thumbnail,
                        permalink);
                Log.d(TAG, i + " " + redditArticle.toString());

                ContentValues redditValues = new ContentValues();
                redditValues.put(RedditArticleContract.Articles.COL_SUBREDDIT, subreddit);
                redditValues.put(RedditArticleContract.Articles.COL_TITLE, title);
                redditValues.put(RedditArticleContract.Articles.COL_AUTHOR, author);
                redditValues.put(RedditArticleContract.Articles.COL_PERMALINK, permalink);

                allVector.add(redditValues);
            }

            Log.d(TAG, "Vector Size: " + allVector.size());

            if(allVector.size() > 0){
                ContentValues[] contentValuesArray = new ContentValues[allVector.size()];
                allVector.toArray(contentValuesArray);
                mContentResolver.delete(RedditArticleContract.Articles.CONTENT_URI, null, null);
                mContentResolver.bulkInsert(RedditArticleContract.Articles.CONTENT_URI, contentValuesArray);
                mContentResolver.notifyChange(RedditArticleContract.Articles.CONTENT_URI, null, false);
            }
            
        } catch (IOException e){
            //TODO Do something useful here!
            Log.e(TAG, "IO Exception" + e);
        } catch (JSONException e){
            //TODO Do something useful
            Log.e(TAG, "JSON Error" + e);
        }
    }

    private int convertNSFW(boolean nsfw){
        if(nsfw){
            return 1;
        } else {
            return 0;
        }
    }

    private String download(String address) throws IOException {
        HttpsURLConnection connection = null;
        InputStream inputStream = null;

        try {
            URL server = new URL(address);
            connection = (HttpsURLConnection)server.openConnection();
            connection.connect();

            int status = connection.getResponseCode();
            inputStream = (status == HttpsURLConnection.HTTP_OK) ? connection.getInputStream() : connection.getErrorStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            for (String temp; ((temp = bufferedReader.readLine()) != null);){
                stringBuilder.append(temp);
            }
            return stringBuilder.toString();

        } finally {
            if (inputStream != null) {inputStream.close();}
            if (connection != null) {connection.disconnect();}
        }
    }

    public static void performSync(){
        //TODO
        Log.d(TAG, "Perform Sync was called");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AccountGeneral.getAccount(), RedditArticleContract.CONTENT_AUTHORITY, bundle);
    }
}

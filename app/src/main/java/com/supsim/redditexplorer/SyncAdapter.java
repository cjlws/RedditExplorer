package com.supsim.redditexplorer;


import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.supsim.redditexplorer.Network.MySingleton;
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
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";
    ContentResolver mContentResolver;
    private static final String url = "https://www.reddit.com/.json";

    public SyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient providerClient, SyncResult syncResult){
        Log.d(TAG, "On Perform Sync Ran");

        try {
            String jsonFeed = download(url);

            JSONObject jsonObject = new JSONObject(jsonFeed);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONArray children = dataObject.getJSONArray("children");

            for(int i = 0; i < children.length(); i++){
                JSONObject child = children.getJSONObject(i);
                JSONObject article = child.getJSONObject("data");

                String domain = article.getString("domain");
                String subreddit = article.getString("subreddit");
                String id = article.getString("id");
                String title = article.getString("title");
                String score = article.getString("score");
                boolean nsfw = article.getBoolean("over_18");
                int num_comments = article.getInt("num_comments");
                long created = article.getLong("created_utc");
                String author = article.getString("author");
                //TODO More to add such as thumbnail links plus the actual link!

                RedditArticle redditArticle = new RedditArticle(domain, subreddit, id, title, "", "", score, nsfw, num_comments, created, author);
                Log.d(TAG, i + redditArticle.toString());
            }
            
        } catch (IOException e){
            //TODO Do something useful here!
            Log.e(TAG, "IO Exception" + e);
        } catch (JSONException e){
            //TODO Do something useful
            Log.e(TAG, "JSON Error" + e);
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

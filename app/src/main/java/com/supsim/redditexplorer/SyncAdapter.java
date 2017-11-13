package com.supsim.redditexplorer;


import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.supsim.redditexplorer.account.AccountGeneral;
import com.supsim.redditexplorer.data.ActionedArticleContract;
import com.supsim.redditexplorer.data.RedditArticleContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

public class SyncAdapter extends AbstractThreadedSyncAdapter {


    private static final String TAG = "SyncAdapter";
    private ContentResolver mContentResolver;
    private static final String url = "https://www.reddit.com/.json";

    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContentResolver = context.getContentResolver();
    }

//    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
//        super(context, autoInitialize, allowParallelSyncs);
//        this.mContentResolver = context.getContentResolver();
//    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient providerClient, SyncResult syncResult) {
        Log.d(TAG, "On Perform Sync Ran");

        ArrayList<String> alreadyActioned = new ArrayList<>();

        Cursor actionedArticles = mContentResolver.query(
                ActionedArticleContract.Actioned_Articles.CONTENT_URI,
                null,
//                ActionedArticleContract.Actioned_Articles.COL_ACTIONED_TYPE + " = ?",
//                new String[]{ActionedArticleContract.Actioned_Articles.ACTIONED_TYPE_DELETED},  // Re-enable for easier testing
                null,
                null,
                null);


        if (actionedArticles != null) {
            for (actionedArticles.moveToFirst(); !actionedArticles.isAfterLast(); actionedArticles.moveToNext()) {
                alreadyActioned.add(actionedArticles.getString(1));
            }
        }

        if (actionedArticles != null && !actionedArticles.isClosed()) actionedArticles.close();

        Log.d("TESTING", " ----  Produced an Array of " + alreadyActioned.size() + " ---- ");


        try {
            String jsonFeed = download(url);

            JSONObject jsonObject = new JSONObject(jsonFeed);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONArray children = dataObject.getJSONArray("children");

            Vector<ContentValues> allVector = new Vector<>(children.length());

            for (int i = 0; i < children.length(); i++) {
                JSONObject child = children.getJSONObject(i);
                JSONObject article = child.getJSONObject("data");


                String id = article.getString("id");                    // The unique reddit ID

                if (alreadyActioned.contains(id)) {
                    Log.d("TESTING", "Article ID " + id + " has already been actioned so ignoring");
                } else {
                    Log.d("TESTING", "Article ID " + id + " is a new article and therefore will be saved");

                    String domain = article.getString("domain");            // The domain the link points to
                    String subreddit = article.getString("subreddit");      // The name of the subreddit it has been posted under

                    String title = article.getString("title");              // The title of the post
                    String score = article.getString("score");              // Total score for the post
                    int nsfw = convertNSFW(article.getBoolean("over_18"));  // NSFW or not - converted to int to make storage easier
                    int num_comments = article.getInt("num_comments");      // Number of comments available
                    int created = article.getInt("created_utc");          // Timestamp of the creation of the post
                    String author = article.getString("author");            // reddit username of the author
                    String thumbnail = article.optString("thumbnail", "");      // scaled thumbnail to accompany the link.  There are also _height and _width fields available
                    String permalink = article.getString("permalink");      // link to the reddit article page - relative link


                    String previews = "";
                    JSONObject preview = article.optJSONObject("preview");
                    if (preview != null) {
                        JSONArray images = preview.optJSONArray("images");
                        if (images != null) {

                            JSONObject previewdata = images.getJSONObject(0);
                            JSONArray resolutions = previewdata.optJSONArray("resolutions");
                            previews = resolutions.toString();
                        }
                    }

//                    RedditArticle redditArticle = new RedditArticle(
//                            domain,
//                            subreddit,
//                            id,
//                            title,
//                            score,
//                            nsfw,
//                            num_comments,
//                            created,
//                            author,
//                            thumbnail,
//                            permalink,
//                            previews);

                    ContentValues redditValues = new ContentValues();
                    redditValues.put(RedditArticleContract.Articles.COL_ID, id);
                    redditValues.put(RedditArticleContract.Articles.COL_SUBREDDIT, subreddit);
                    redditValues.put(RedditArticleContract.Articles.COL_TITLE, title);
                    redditValues.put(RedditArticleContract.Articles.COL_AUTHOR, author);
                    redditValues.put(RedditArticleContract.Articles.COL_PERMALINK, permalink);
                    redditValues.put(RedditArticleContract.Articles.COL_THUMBNAIL, thumbnail);
                    redditValues.put(RedditArticleContract.Articles.COL_DOMAIN, domain);
                    redditValues.put(RedditArticleContract.Articles.COL_COMMENTS, num_comments);
                    redditValues.put(RedditArticleContract.Articles.COL_SCORE, score);
                    redditValues.put(RedditArticleContract.Articles.COL_CREATED, created);
                    redditValues.put(RedditArticleContract.Articles.COL_PREVIEWS, previews);

                    if (nsfw == 0) allVector.add(redditValues);
                }
            }

            if (allVector.size() > 0) {
                ContentValues[] contentValuesArray = new ContentValues[allVector.size()];
                allVector.toArray(contentValuesArray);
                mContentResolver.delete(RedditArticleContract.Articles.CONTENT_URI, null, null);
                mContentResolver.bulkInsert(RedditArticleContract.Articles.CONTENT_URI, contentValuesArray);
                mContentResolver.notifyChange(RedditArticleContract.Articles.CONTENT_URI, null, false);
            }

        } catch (IOException e) {

            Log.e(TAG, "IO Exception" + e);
        } catch (JSONException e) {

            Log.e(TAG, "JSON Error" + e);
        }
    }

    private int convertNSFW(boolean nsfw) {
        if (nsfw) {
            return 1;
        } else {
            return 0;
        }
    }

    @NonNull
    private String download(@NonNull String address) throws IOException {
        HttpsURLConnection connection = null;
        InputStream inputStream = null;

        try {
            URL server = new URL(address);
            connection = (HttpsURLConnection) server.openConnection();
            connection.connect();

            int status = connection.getResponseCode();
            inputStream = (status == HttpsURLConnection.HTTP_OK) ? connection.getInputStream() : connection.getErrorStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            for (String temp; ((temp = bufferedReader.readLine()) != null); ) {
                stringBuilder.append(temp);
            }
            return stringBuilder.toString();

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void performSync() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AccountGeneral.getAccount(), RedditArticleContract.CONTENT_AUTHORITY, bundle);
    }
}

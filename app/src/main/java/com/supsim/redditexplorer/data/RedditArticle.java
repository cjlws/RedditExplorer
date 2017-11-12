package com.supsim.redditexplorer.data;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.supsim.redditexplorer.Tools.removeXMLStringEncoding;

public class RedditArticle {

    private static final String interprocessArticleID = "interprocessArticleID";
    private static final String interprocessArticleDomain = "interprocessArticleDomain";
    private static final String interprocessArticleSubReddit = "interprocessArticleSubReddit";
    private static final String interprocessArticleTitle = "interprocessArticleTitle";
    private static final String interprocessArticleScore = "interprocessArticleScore";
    private static final String interprocessArticleNumberComments = "interprocessArticleComments";
    private static final String interprocessArticleCreated = "interprocessArticleCreated";
    private static final String interprocessArticleAuthor = "interprocessArticleAuthor";
    private static final String interprocessArticleThumbnail = "interprocessArticleThumbnail";
    private static final String interprocessArticlePermalink = "interprocessArticleLink";
    private static final String interprocessArticlePreviews = "interprocessArticlePreviews";

    private String domain;
    private String subreddit;
    private String id;
    private String title;
    private String score;
    private int nsfw;  // Use int so it plays nicer with database than boolean
    private int num_comments;
    private int created;
    private String author;
    private String post_thumbnail;
    private String permalink;
    private String previewOptions;


    public RedditArticle(String domain,
                         String subreddit,
                         String id,
                         String title,
                         String score,
                         int nsfw,
                         int num_comments,
                         int created,
                         String author,
                         String post_thumbnail,
                         String permalink,
                         String previewOptions){
        this.domain = domain;
        this.subreddit = subreddit;
        this.id = id;
        this.title = title;
        this.score = score;
        this.nsfw = nsfw;
        this.num_comments = num_comments;
        this.created = created;
        this.author = author;
        this.post_thumbnail = post_thumbnail;
        this.permalink = permalink;
        this.previewOptions = previewOptions;

    }

    public RedditArticle(Cursor cursor){
        this.id = cursor.getString(0);
        this.domain = cursor.getString(1);
        this.subreddit = cursor.getString(2);
        this.title = cursor.getString(3);
        this.score = cursor.getString(4);
        this.nsfw = cursor.getInt(5);
        this.num_comments = cursor.getInt(6);
        this.created = cursor.getInt(7);
        this.author = cursor.getString(8);
        this.post_thumbnail = cursor.getString(9);
        this.permalink = cursor.getString(10);
        this.previewOptions = cursor.getString(11);
    }

public Bundle getRedditArticleBundle(){
    Bundle bundle = new Bundle();
    bundle.putString(interprocessArticleID, this.id);
    bundle.putString(interprocessArticleDomain, this.domain);
    bundle.putString(interprocessArticleSubReddit, this.subreddit);
    bundle.putString(interprocessArticleTitle, this.title);
    bundle.putString(interprocessArticleScore, this.score);
    bundle.putInt(interprocessArticleNumberComments, this.num_comments);
    bundle.putInt(interprocessArticleCreated, this.created);
    bundle.putString(interprocessArticleAuthor, this.author);
    bundle.putString(interprocessArticleThumbnail, this.post_thumbnail);
    bundle.putString(interprocessArticlePermalink, this.permalink);
    bundle.putString(interprocessArticlePreviews, this.previewOptions);

    return bundle;

}

public RedditArticle(Bundle bundle){
    this.id = bundle.getString(interprocessArticleID);
    this.domain = bundle.getString(interprocessArticleDomain);
    this.subreddit = bundle.getString(interprocessArticleSubReddit);
    this.title = bundle.getString(interprocessArticleTitle);
    this.score = bundle.getString(interprocessArticleScore);
    this.nsfw = 0;
    this.num_comments = bundle.getInt(interprocessArticleNumberComments);
    this.created = bundle.getInt(interprocessArticleCreated);
    this.author = bundle.getString(interprocessArticleAuthor);
    this.post_thumbnail = bundle.getString(interprocessArticleThumbnail);
    this.permalink = bundle.getString(interprocessArticlePermalink);
    this.previewOptions = bundle.getString(interprocessArticlePreviews);
}

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Domain: ").append(this.domain).append("\n");
        stringBuilder.append("Subreddit: ").append(this.subreddit).append("\n");
        stringBuilder.append("ID: ").append(this.id).append("\n");
        stringBuilder.append("Title: ").append(this.title).append("\n");
        stringBuilder.append("Score: ").append(this.score).append("\n");
        stringBuilder.append("NSFW: ");
        if(postIsSafeForWork()) {
            stringBuilder.append("Safe").append("\n");
        } else {
            stringBuilder.append("Not Safe").append("\n");
        }
        stringBuilder.append("Comments: ").append(this.num_comments).append("\n");
        stringBuilder.append("Created: ").append(this.created).append("\n");
        stringBuilder.append("Author: ").append(this.author).append("\n");
        stringBuilder.append("Thumbnail: ").append(this.post_thumbnail).append("\n");
        stringBuilder.append("Link: ").append(this.permalink).append("\n");
        stringBuilder.append("Previews: ").append(this.previewOptions).append("\n");

        return stringBuilder.toString();
    }

    private boolean postIsSafeForWork(){
        return this.nsfw == 1;
    }

    public String getDomain() {
        return domain;
    }

    public String getBestPreviewImage(int viewWidth){

        String bestMatch = "";

        if(!this.previewOptions.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(this.previewOptions);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject preview = jsonArray.getJSONObject(i);
                    if (preview.getInt("width") >= viewWidth) {
                        bestMatch = removeXMLStringEncoding(preview.getString("url"));
                        Log.d("IMAGE", "Best Match Found of " + preview.getInt("width") + " (needs to be at least " + viewWidth + ") - url: " + bestMatch);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

            return bestMatch;
        }

        public String getPreviewOptions(){
            return this.previewOptions;
        }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public int getScore() {
        return Integer.valueOf(score);
    }

    public int getCreated() {
        return created;
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPost_thumbnail() {
        return post_thumbnail;
    }

    public String getSafeThumbnail(){
        if(this.post_thumbnail.equals("default") || this.post_thumbnail.equals("spoiler") || this.post_thumbnail.equals("self") || this.post_thumbnail.equals("nsfw") || this.post_thumbnail.equals("image")){
            return "";
        }
        return removeXMLStringEncoding(this.post_thumbnail);
    }

    public String getPermalink() {
        return permalink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getShortenedTitle(int maxCharInTitle){

        if(this.getTitle().length() <= maxCharInTitle){

            return this.getTitle();

        } else {

            return this.getTitle().substring(0, maxCharInTitle);

        }
    }

    public void setTitle(String title) {
        this.title = title;
    }



}

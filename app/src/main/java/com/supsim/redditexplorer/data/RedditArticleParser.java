package com.supsim.redditexplorer.data;

import org.json.JSONObject;

/**
 * Created by johnrobinson on 03/09/2017.
 */

public class RedditArticleParser {
    public static RedditArticle parse(JSONObject jsonArticle){
        RedditArticle redditArticle = new RedditArticle();
        redditArticle.setId(jsonArticle.optString("id"));
        redditArticle.setTitle(jsonArticle.optString("title"));
        redditArticle.setContent(jsonArticle.optString("content"));
        redditArticle.setLink(jsonArticle.optString("link"));
        return redditArticle;
    }
}

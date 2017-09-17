package com.supsim.redditexplorer.data;

import org.json.JSONObject;

public class RedditArticleParser {

    //TODO Move parsing in to here to make reading easier
    public static RedditArticle parse(JSONObject jsonArticle){
        RedditArticle redditArticle = new RedditArticle();
//        redditArticle.setId(jsonArticle.optString("id"));
//        redditArticle.setTitle(jsonArticle.optString("title"));
//        redditArticle.setContent(jsonArticle.optString("content"));
//        redditArticle.setLink(jsonArticle.optString("link"));
        return redditArticle;
    }
}

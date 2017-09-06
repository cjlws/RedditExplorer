package com.supsim.redditexplorer.data;

/**
 * Created by johnrobinson on 03/09/2017.
 */

public class RedditArticle {

    private String domain;
    private String subreddit;
    private String id;
    private String title;
    private String content;
    private String link;
    private String score;
    private boolean nsfw;
    private int num_comments;
    private long created;
    private String author;

    public RedditArticle(){

    }

    public RedditArticle(String domain, String subreddit, String id, String title, String content,
                         String link, String score, boolean nsfw, int num_comments, long created, String author){
        this.domain = domain;
        this.subreddit = subreddit;
        this.id = id;
        this.title = title;
        this.content = content;
        this.link = link;
        this.score = score;
        this.nsfw = nsfw;
        this.num_comments = num_comments;
        this.created = created;
        this.author = author;
    }

    @Override
    public String toString(){
        return "Sub: " + this.subreddit + ", Title: " + this.title + ", Author: " + author;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}

package com.supsim.redditexplorer.data;

/**
 * Created by johnrobinson on 03/09/2017.
 */

public class RedditArticle {

    private String id;
    private String title;
    private String content;
    private String link;

    public RedditArticle(){

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

package com.supsim.redditexplorer.data;

public class ActionedArticle {

    private String redditId;

    public ActionedArticle(String redditId){
        this.redditId = redditId;
    }

    public String getRedditId(){
        return this.redditId;
    }
}

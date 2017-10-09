package com.supsim.redditexplorer.data;

public class SecondLevelComment {

    private String author;
    private String comment;

    public SecondLevelComment(){
       //Empty Constructor
    }

    public SecondLevelComment(String author, String comment){
        this.author = author;
        this.comment = comment;
    }

    public String getAuthor(){
        return this.author;
    }

    public String getComment(){
        return this.comment;
    }

    @Override
    public String toString(){
        return this.author + ":  " + this.comment;
    }
}

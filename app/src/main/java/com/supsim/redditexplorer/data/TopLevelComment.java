package com.supsim.redditexplorer.data;

import java.util.ArrayList;

public class TopLevelComment {

    private String author;
    private int score;
    private String comment;
    private ArrayList<SecondLevelComment> replies;
    private int type;  // 1 = single comment, 2 = comment with subcomment

    public TopLevelComment(String author, int score, String comment, ArrayList<SecondLevelComment> replies, int type){
        this.author = author;
        this.score = score;
        this.comment = comment;
        this.replies = replies;
        this.type = type;
    }

    public String getComment(){
        return this.comment;
    }

    public String getAuthor(){
        return this.author;
    }

    public int getScore(){
            return this.score;
    }

    public ArrayList<SecondLevelComment> getReplies(){
        return this.replies;
    }

    public String getNumberOfSecondLevelComments(){
        return "There are " + this.replies.size() + " replies to " + this.comment;
    }

    public int getType(){
        return this.type;
    }
}

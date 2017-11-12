package com.supsim.redditexplorer.data;

import com.supsim.redditexplorer.Tools;

import java.util.ArrayList;

public class TopLevelComment {

    private static final int maxCommentLength = 50;

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

    private int numberOfSecondLevelComments(){
        return this.replies.size();
    }

    private String shortComment(){
        if (this.comment.length() <= maxCommentLength){
            return Tools.removeXMLStringEncoding(this.comment);
        } else {
            return Tools.removeXMLStringEncoding(this.comment.substring(0, maxCommentLength));
        }
    }

    @Override
    public String toString(){
        if (this.type == 1){
            return "TLC - Au: " + this.author + ", Sc: " + this.score + ", Co: " + shortComment().replace("\n", ". ");
        } else {
            return "TLC - Au: " + this.author + ", Sc: " + this.score + ", Co: " + shortComment().replace("\n", ". ") + ", SubCo: " + this.numberOfSecondLevelComments();
        }
    }

    public int getType(){
        return this.type;
    }
}

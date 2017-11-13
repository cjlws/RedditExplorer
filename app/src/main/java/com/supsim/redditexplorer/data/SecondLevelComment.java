package com.supsim.redditexplorer.data;

import com.supsim.redditexplorer.Tools;

public class SecondLevelComment {

    private static final int maxLengthComment = 50;  // Max number of characters to display

    private String author;
    private String comment;

    public SecondLevelComment(String author, String comment) {
        this.author = author;
        this.comment = comment;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getComment() {
        return this.comment;
    }

    private String shortAndCleanComment() {
        if (this.comment.length() <= maxLengthComment) {
            return Tools.removeXMLStringEncoding(this.comment.replace("\n", ". "));
        } else {
            return Tools.removeXMLStringEncoding(this.comment.substring(0, maxLengthComment).replace("\n", ". "));
        }
    }

    @Override
    public String toString() {
        return this.author + ":  " + shortAndCleanComment();
    }
}

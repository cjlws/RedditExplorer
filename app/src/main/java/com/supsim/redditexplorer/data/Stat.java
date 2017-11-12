package com.supsim.redditexplorer.data;


import com.github.mikephil.charting.data.PieEntry;

public class Stat {

    private int rank;
    private String subreddit;
    private int numberOfViews;


    public Stat(int rank, String subreddit, int numberOfViews){
        this.rank = rank;
        this.subreddit = subreddit;
        this.numberOfViews = numberOfViews;
    }

    public int getRank(){
        return this.rank;
    }

    public String getSubreddit(){
        return this.subreddit;
    }

    public int getNumberOfViews(){
        return this.numberOfViews;
    }

    @Override
    public String toString(){
        return "Sub: " + this.subreddit + ", Views: " + this.numberOfViews + ", Rank: " + this.rank;
    }

    public PieEntry getPieEntry(){
        return new PieEntry((float)this.numberOfViews, this.subreddit);
    }
}

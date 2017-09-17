package com.supsim.redditexplorer.data;

public class RedditArticle {

    private String domain;
    private String subreddit;
    private String id;
    private String title;
    private String score;
    private int nsfw;  // Use int so it plays nicer with databse than boolean
    private int num_comments;
    private long created;
    private String author;
    private String post_thumbnail;
    private String permalink;

    public RedditArticle(){
        // Empty Constructor
    }

    public RedditArticle(String domain,
                         String subreddit,
                         String id,
                         String title,
                         String score,
                         int nsfw,
                         int num_comments,
                         long created,
                         String author,
                         String post_thumbnail,
                         String permalink){
        this.domain = domain;
        this.subreddit = subreddit;
        this.id = id;
        this.title = title;
        this.score = score;
        this.nsfw = nsfw;
        this.num_comments = num_comments;
        this.created = created;
        this.author = author;
        this.post_thumbnail = post_thumbnail;
        this.permalink = permalink;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Domain: ").append(this.domain);
        stringBuilder.append(", Subreddit: ").append(subreddit);
        stringBuilder.append(", ID: ").append(id);
        stringBuilder.append(", Title: ").append(title);
        stringBuilder.append(", Score: ").append(score);
        stringBuilder.append(", NSFW: ");
        if(postIsSafeForWork()) {
            stringBuilder.append("Safe");
        } else {
            stringBuilder.append("Not Safe");
        }
        stringBuilder.append(", Comments: ").append(num_comments);
        stringBuilder.append(", Created: ").append(created);
        stringBuilder.append(", Author: ").append(author);
        stringBuilder.append(", Thumbnail: ").append(post_thumbnail);
        stringBuilder.append(", Link: ").append(permalink);

        return stringBuilder.toString();
    }

    public boolean postIsSafeForWork(){
        if(this.nsfw == 1) return false;
        return true;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public int isNsfw() {
        return nsfw;
    }

    public void setNsfw(int nsfw) {
        this.nsfw = nsfw;
    }

    public int getNum_comments() {
        return num_comments;
    }

    public void setNum_comments(int num_comments) {
        this.num_comments = num_comments;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPost_thumbnail() {
        return post_thumbnail;
    }

    public void setPost_thumbnail(String post_thumbnail) {
        this.post_thumbnail = post_thumbnail;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
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

}

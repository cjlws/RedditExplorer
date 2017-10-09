package com.supsim.redditexplorer;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.supsim.redditexplorer.Network.MySingleton;
import com.supsim.redditexplorer.data.SecondLevelComment;
import com.supsim.redditexplorer.data.StatsRecordContract;
import com.supsim.redditexplorer.data.TopLevelComment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private String pageTitle = "Unknown";
    private String pageLink = "Oops";
    private String pageSub = "Gah";

    public static final String interprocessTitle = "interprocessTitle";
    public static final String interprocessLink = "interprocessLink";
    public static final String interprocessSubreddit = "interprocessSubreddit";
    public static final String domainStub = "https://reddit.com";

    RequestQueue requestQueue;
    ImageLoader imageLoader;
    TextView bodyText;
    ListView commentHolderLayout;
    CommentArrayAdapter commentArrayAdapter;

    int testingCommentTicker = 0;
    boolean visitRecorded;


    // Columns to store the stats
    static final int COL_ID = 0;
    static final int COL_SUBREDDIT = 1;
    static final int COL_SCORE = 2;

    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(interprocessLink)) {
            pageLink = getArguments().getString(interprocessLink);
            Log.d("Frag", pageLink);
        }

        if (getArguments().containsKey(interprocessTitle)) {
            pageTitle = getArguments().getString(interprocessTitle);
            Log.d("Frag", pageTitle);
        }

        if (getArguments().containsKey(interprocessSubreddit)) {
            pageSub = getArguments().getString(interprocessSubreddit);
            if (!visitRecorded) {
                Log.d("STATS Boolean", "Visit Not yet recorded");
                recordVisit(pageSub);
            } else {
                Log.d("STATS Boolean", "Visit aleady recorded");
            }
        }

        Activity activity = this.getActivity();
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(pageSub);
        }

        requestQueue = MySingleton.getInstance(this.getContext()).getRequestQueue();
        imageLoader = MySingleton.getInstance(this.getContext()).getImageLoader();
        getComments(pageLink);
    }

    private void recordVisit(String subreddit) {

        Log.d("STATS Record", "Record Vist to " + subreddit);

        String selection = StatsRecordContract.Stats.COL_STAT_SUBREDDIT + " = ?";
        String[] selectionArgs = new String[]{subreddit};
        Cursor cursor = getActivity().getContentResolver().query(StatsRecordContract.Stats.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null);

        if (cursor != null) {
            int test = cursor.getCount();
            Log.d("STATS Record", "Total Cursor Size is " + test);

            if (test > 0) {

//            int index = cursor.getColumnIndex(StatsRecordContract.Stats.COL_STAT_COUNT);


                cursor.moveToFirst();
                int currentCount = cursor.getInt(COL_SCORE);
                Log.d("STATS Record", subreddit + " was already in the database with a score of " + currentCount);

                int rows = Tools.increaseSubsCount(getContext(), cursor);
                Log.d("STATS UPDATE", "A total of " + rows + " were updated");
                if(rows > 0) visitRecorded = true;
            } else {
                Log.d("STATS Record", "No record found for " + subreddit);
                Uri newUri = Tools.addNewSubToDatabase(getActivity(), subreddit);
                long newID = ContentUris.parseId(newUri);
                Log.d("STATS New", "Added " + subreddit + " and got back ID of " + newID);
                if(newUri != null) visitRecorded = true;
            }
        }
        if(cursor != null && !cursor.isClosed()) cursor.close();
    }

//    private Uri addNewSubToDatabase(String subreddit) {
//        //TODO Give a return to indicate success or failure
//        Log.d("STATS New", "Adding " + subreddit + " to database");
//        Uri newUri;
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(StatsRecordContract.Stats.COL_STAT_SUBREDDIT, subreddit);
//        contentValues.put(StatsRecordContract.Stats.COL_STAT_COUNT, 1);
//
//        newUri = getActivity().getContentResolver().insert(StatsRecordContract.Stats.CONTENT_URI, contentValues);
//
//        return newUri;
//
//    }
//
//    private int increaseSubsCount(Cursor cursor) {
//        Log.d("STATS ADD", "Upping the score...");
////        int currentCountIndex = cursor.getColumnIndex(StatsRecordContract.Stats.COL_STAT_COUNT);
//        int currentCount = cursor.getInt(COL_SCORE);
////        int idIndex = cursor.getColumnIndex(StatsRecordContract.Stats.COL_STAT_ID);
//        int id = cursor.getInt(COL_ID);
//
//        String selection = StatsRecordContract.Stats.COL_STAT_ID + " LIKE ?";
//        String[] selectionArgs = {String.valueOf(id)};
//
//        ContentValues newValues = new ContentValues();
//        newValues.put(StatsRecordContract.Stats.COL_STAT_COUNT, currentCount + 1);
//
//        int rowsUpdated = 0;
//
//        rowsUpdated = getActivity().getContentResolver().update(
//                StatsRecordContract.Stats.CONTENT_URI,
//                newValues,
//                selection,
//                selectionArgs
//        );
//
//        return rowsUpdated;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        commentHolderLayout = (ListView) rootView.findViewById(R.id.comment_holder_listView);
        ArrayList<TopLevelComment> topLevelComments = new ArrayList<TopLevelComment>();
        commentArrayAdapter = new CommentArrayAdapter(getContext(), topLevelComments);
        commentHolderLayout.setAdapter(commentArrayAdapter);

        bodyText = (TextView) rootView.findViewById(R.id.temp_textview);


        //TODO Make this a bit fancier...
        if (pageTitle != null) {
            bodyText.setText("Downloading content for \"" + pageTitle + "\".....");
        } else {
            bodyText.setText("Loading Comments");
        }

        return rootView;
    }

    private void getComments(String link) {

        //TODO put in URL validation and guards against null

        String destinationUrl = getFullLink(link);
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, destinationUrl, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                updatePage(response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//            }
//        });
//
//        MySingleton.getInstance(this.getContext()).addToRequestQueue(stringRequest);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(destinationUrl, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                updatePage(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        MySingleton.getInstance(this.getContext()).addToRequestQueue(jsonArrayRequest);
    }

    private void updatePage(JSONArray response) {

        //TODO put in checks and measures...
        parseJson(response);

    }

    private void parseJson(JSONArray jsonArray) {

        try {

            if (jsonArray.length() == 2) {
                // Correct Number :)
                JSONObject postDetailsJSON = jsonArray.getJSONObject(0);
                JSONObject commentsJSON = jsonArray.getJSONObject(1);

                parseDetailsJSON(postDetailsJSON);
                parseCommentsJSON(commentsJSON);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO Work out comments are being shown multiple times on the tablet version
    //TODO Work out why only one comment is being shown on the phone version
    //TODO Build the top of the tablet detail page - mostly done
    //TODO Read the spec sheet to make sure all required details are being displayed
    //TODO Build some example pages where they are image or video based posts
    //TODO Block NSFW being requested
    //TODO Fix app name and tablet title bar - done, I think


    private void parseDetailsJSON(JSONObject detailsJSON) {

        try {
            JSONObject data = detailsJSON.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");
            for (int i = 0; i < children.length(); i++) {



                JSONObject child = children.getJSONObject(i);
                JSONObject childData = child.getJSONObject("data");

                Log.d("TEMP", childData.toString(4));

                //TODO Add default returns
                String domain = childData.optString("domain");
                String subreddit = childData.optString("subreddit");
                int score = childData.optInt("score");
                JSONObject preview = childData.optJSONObject("preview");
                int numberOfComments = childData.optInt("num_comments");
                String thumbnail = childData.optString("thumbnail");
                String url = childData.optString("url");
                int created_utc = childData.optInt("created_utc");
                String author = childData.optString("author");
                String title = childData.optString("title");

                String temp = "Domain: " + domain + "\n"
                        + "Sub: " + subreddit + "\n"
                        + "Score: " + score + "\n";

                    if(preview != null) {
                        temp    += "Preview: " + preview.toString() + "\n";
                    }

                temp   += "Comments: " + numberOfComments + "\n"
                        + "Thumbnail: " + thumbnail + "\n"
                        + "Url: " + url + "\n"
                        + "Created: " + created_utc + "\n"
                        + "Author: " + author;

                Log.d("DETAILS", temp);
                drawDetailsSection(domain, subreddit, author, score, created_utc, url, title, thumbnail, numberOfComments);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawDetailsSection(String domain, String subreddit, String author, int score, int created, final String mainLink, String title,
                                    String thumbnail, int numberOfComments) {

        TextView subredditTextView = (TextView)getActivity().findViewById(R.id.postSubredditTextView);
        TextView destinationDomainTextView = (TextView)getActivity().findViewById(R.id.postDestinationDomainTextView);
        TextView authorAndTimeTextView = (TextView)getActivity().findViewById(R.id.postAuthorAndTimeStampTextView);
        TextView scoreTextView = (TextView)getActivity().findViewById(R.id.postScoreTextView);
        TextView mainLinkTextView = (TextView)getActivity().findViewById(R.id.postMainLinkTextView);
        NetworkImageView thumbnailView = (NetworkImageView)getActivity().findViewById(R.id.postThumbnailImageView);

        subredditTextView.setText(formatSubreddit(subreddit));
        authorAndTimeTextView.setText(formatAuthorAndTime(author, created));
        destinationDomainTextView.setText(domain);
        scoreTextView.setText(formatScore(score));

        // Update main page link with article title and functioning hyperlink
        mainLinkTextView.setText(title);
        mainLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Link to: " + mainLink, Toast.LENGTH_LONG).show();
            }
        });
        mainLinkTextView.setClickable(true);

        // Draw thumbnail
        thumbnailView.setImageUrl(thumbnail, imageLoader);
        thumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Link to: " + mainLink, Toast.LENGTH_LONG).show();
            }
        });
        thumbnailView.setClickable(true);

        bodyText.setText("There are " + numberOfComments + " comments");

    }

    private String formatScore(int score) {
        if (score < 1000) {
            return String.valueOf(score);
        } else {
            return String.format(Locale.getDefault(), "%.2fK", ((double) score / 1000));
        }
    }

    private String formatTime(int created){


//        Date postingDate = new Date((long)created);
        long currentTimestamp = new Date().getTime();
        long elapsed = (currentTimestamp / 1000) - created;

//        Date elapsedDate = new Date(elapsed);

        Log.d("TIME", "Elapsed Time Unit = " + elapsed);

        if(elapsed >= 60*60*24){

            // Elapsed time is in the realm of days

            int numberOfDays = (int)(elapsed / (60*60*24));
            if(numberOfDays == 1){
                return numberOfDays + " day";
            } else {
                return numberOfDays + " days";
            }
        } else if(elapsed >= 60*60){

            int numberOfHours = (int)(elapsed / (60*60));
            int numberOfMinutes = (int)((elapsed - (numberOfHours * 60 * 60)) / 60);

            String returnString = "";

            if(numberOfHours == 1){
                returnString += numberOfHours + " hour, ";
            } else {
                returnString += numberOfHours + " hours, ";
            }

            if (numberOfMinutes == 1){
                returnString += numberOfMinutes + " minute";
            } else {
                returnString += numberOfMinutes + " minutes";
            }

            return returnString;

        } else if(elapsed >= 60){

            int numberOfMinutes = (int)(elapsed / 60);
            int numberOfSeconds = (int)(elapsed - (numberOfMinutes * 60));

            String returnString = "";

            if(numberOfMinutes == 1){
                returnString += numberOfMinutes + " minute, ";
            } else {
                returnString += numberOfMinutes + " minutes, ";
            }

            if(numberOfSeconds == 1){
                returnString += numberOfSeconds + " second";
            } else {
                returnString += numberOfSeconds + " seconds";
            }

            return returnString;

        } else if(elapsed > 0 && elapsed < 60){

            if(elapsed == 1){
                return elapsed + " second";
            } else {
                return elapsed + " seconds";
            }

        }
        return String.valueOf(created);
    }

    private String formatAuthorAndTime(String author, int time){
        String formatedTime = formatTime(time);
        return String.format(Locale.getDefault(), getString(R.string.post_author_and_time_format), formatedTime, author);
    }

    private String formatSubreddit(String subreddit){
        return String.format(Locale.getDefault(), getString(R.string.post_subreddit_format), subreddit);
    }

    private void drawCommentsSection(ArrayList<TopLevelComment> topLevelComments) {
        Log.d("DCS", "There was a request to draw comments for " + topLevelComments.size() + " comments");
        commentArrayAdapter.clear();
        commentArrayAdapter.addAll(topLevelComments);
        commentArrayAdapter.notifyDataSetChanged();
    }

    private void parseCommentsJSON(JSONObject commentsJSON) {

        int testingOnlyChildren = 0;
        int testingOnesWithReplies = 0;

        ArrayList<TopLevelComment> allcomments = new ArrayList<TopLevelComment>();
        try {

            JSONObject data = commentsJSON.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");

            Log.d("TEST", "Found " + children.length() + " children");

            for (int i = 0; i < children.length(); i++) {

                JSONObject child = children.getJSONObject(i);
                JSONObject childData = child.getJSONObject("data");


                JSONObject replies;
                boolean onlyChild;
                try {
                    // This clause fires when there are replies
                    replies = childData.getJSONObject("replies");
                    onlyChild = false;
                } catch (Exception e) {
                    // This clause fires when a comment has no replies to it
                    replies = null;
                    onlyChild = true;
                }

                if (onlyChild) {
//                    Log.d("TEST", "Adding only child " + childData.getString("body") + " to the mix");
                    testingOnlyChildren++;
                    allcomments.add(new TopLevelComment(
                            childData.getString("author"),
                            childData.getInt("score"),
                            childData.getString("body"),
                            null,
                            1
                    ));

                } else {

                    if (replies != null) {

                        JSONObject replyData = replies.getJSONObject("data");
                        JSONArray replyArray = replyData.getJSONArray("children");

                        ArrayList<SecondLevelComment> secondLevelComments = new ArrayList<>();

                        for (int a = 0; a < replyArray.length(); a++) {
                            JSONObject jsonObject = replyArray.getJSONObject(a);

                            if (jsonObject.getString("kind").equals("t1")) {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                try {
                                    secondLevelComments.add(new SecondLevelComment(dataObject.getString("author"), dataObject.getString("body")));
                                } catch (Exception e) {
                                }
                            } else {
                                Log.d("COMMENT_" + i + "_" + a, "Not a T1 : " + jsonObject.getString("kind"));
                            }
                        }

                        try {
                            testingOnesWithReplies++;
                            allcomments.add(new TopLevelComment(
                                    childData.getString("author"),
                                    childData.getInt("score"),
                                    childData.getString("body"),
                                    secondLevelComments,
                                    2
                            ));
                        } catch (Exception e) {
                            Log.d("BODY", "Nope");
                        }

                    } else {
                        Log.d("ERROR", "Replies came in as null");  //TODO provide user feedback
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        Log.d("TOTALS", "There should be a total of " + allcomments.size() + " comments, made up of " + testingOnlyChildren + " singles and " + testingOnesWithReplies + " with replies");

        drawCommentsSection(allcomments);

    }

    private String getFullLink(String link) {
        return domainStub + link + ".json";
    }

    private class CommentArrayAdapter extends ArrayAdapter<TopLevelComment> {


        private CommentArrayAdapter(Context context, ArrayList<TopLevelComment> topLevelComments) {
            super(context, 0, topLevelComments);
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            Log.d("GETVIEW", "Ticker at " + testingCommentTicker + " and position at " + position);
            testingCommentTicker++;
            TopLevelComment topLevelComment = getItem(position);

            if (topLevelComment.getType() == 2) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_block_with_replies, parent, false);
                    TextView authorTextView = (TextView) convertView.findViewById(R.id.comment_author_textview);
                    TextView commentTextView = (TextView) convertView.findViewById(R.id.comment_body);
                    TextView scoreTextView = (TextView) convertView.findViewById(R.id.comment_number_textview);
                    ListView subcomments = (ListView) convertView.findViewById(R.id.subcomment_holder);

                    //Display the root comment of the chain
                    authorTextView.setText(topLevelComment.getAuthor());
                    commentTextView.setText(topLevelComment.getComment());
                    scoreTextView.setText(formatScore(topLevelComment.getScore()));

                    //Display the first set of replies to the root comment
//                    Log.d("CAA", topLevelComment.getNumberOfSecondLevelComments());

//                    ArrayList<SecondLevelComment> secondLevelComments = topLevelComment.getReplies();
//
//                    SubCommentArrayAdapter adapter = new SubCommentArrayAdapter(getContext(), secondLevelComments);
//                    subcomments.setAdapter(adapter);
                }
            } else {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_block, parent, false);
                    TextView authorTextView = (TextView) convertView.findViewById(R.id.comment_author_textview);
                    TextView commentTextView = (TextView) convertView.findViewById(R.id.comment_body);
                    TextView scoreTextView = (TextView) convertView.findViewById(R.id.comment_number_textview);

                    authorTextView.setText(topLevelComment.getAuthor());
                    commentTextView.setText(topLevelComment.getComment());
                    scoreTextView.setText(formatScore(topLevelComment.getScore()));
                }
            }
            return convertView;
        }
    }

    private class SubCommentArrayAdapter extends ArrayAdapter<SecondLevelComment> {

        private SubCommentArrayAdapter(Context context, ArrayList<SecondLevelComment> secondLevelComments) {
            super(context, 0, secondLevelComments);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            SecondLevelComment secondLevelComment = getItem(position);

//            Log.d("SCAA", "Position " + position + ", Comment: " + secondLevelComment.getComment());

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.subcommentrow, parent, false);
                TextView authorTextView = (TextView) convertView.findViewById(R.id.subcomment_author);
                TextView bodyTextView = (TextView) convertView.findViewById(R.id.subcomment_body);
                authorTextView.setText(secondLevelComment.getAuthor());
                bodyTextView.setText(secondLevelComment.getComment());
            }
            return convertView;
        }
    }


}

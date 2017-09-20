package com.supsim.redditexplorer;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.supsim.redditexplorer.Network.MySingleton;
import com.supsim.redditexplorer.data.SecondLevelComment;
import com.supsim.redditexplorer.data.TopLevelComment;
import com.supsim.redditexplorer.dummy.DummyContent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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
    TextView bodyText;
    ListView commentHolderLayout;
    CommentArrayAdapter commentArrayAdapter;

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
        }

        Activity activity = this.getActivity();
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(pageSub);
        }

        requestQueue = MySingleton.getInstance(this.getContext()).getRequestQueue();
        getComments(pageLink);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        commentHolderLayout = (ListView) rootView.findViewById(R.id.comment_holder_listView);
        ArrayList<TopLevelComment> topLevelComments = new ArrayList<TopLevelComment>();
        commentArrayAdapter = new CommentArrayAdapter(getContext(), topLevelComments);
        commentHolderLayout.setAdapter(commentArrayAdapter);

        bodyText = (TextView)rootView.findViewById(R.id.temp_textview);
        bodyText.setText("Loading Comments");  //TODO Make this a bit fancier...

        if (pageTitle != null) {
            bodyText.setText(pageTitle);
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
    //TODO Build the top of the tablet detail page
    //TODO Read the spec sheet to make sure all required details are being displayed
    //TODO Build some example pages where they are image or video based posts
    //TODO Block NSFW being requested
    //TODO Fix app name and tablet title bar



    private void parseDetailsJSON(JSONObject detailsJSON) {

        try {
            JSONObject data = detailsJSON.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");
            for (int i = 0; i < children.length(); i++) {
                JSONObject child = children.getJSONObject(i);
                JSONObject childData = child.getJSONObject("data");

                String domain = childData.getString("domain");
                String subreddit = childData.getString("subreddit");
                String score = childData.getString("score");
                JSONObject preview = childData.getJSONObject("preview");
                int numberOfComments = childData.getInt("num_comments");
                String thumbnail = childData.getString("thumbnail");
                String url = childData.getString("url");
                int created_utc = childData.getInt("created_utc");
                String author = childData.getString("author");

                String temp = "Domain: " + domain + "\n"
                        + "Sub: " + subreddit + "\n"
                        + "Score: " + score + "\n"
                        + "Preview: " + preview.toString() + "\n"
                        + "Comments: " + numberOfComments + "\n"
                        + "Thumbnail: " + thumbnail + "\n"
                        + "Url: " + url + "\n"
                        + "Created: " + created_utc + "\n"
                        + "Author: " + author;

                Log.d("DETAILS", temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawDetailsSection(){

    }

    private void drawCommentsSection(ArrayList<TopLevelComment> topLevelComments){
        commentArrayAdapter.addAll(topLevelComments);

    }

    private void parseCommentsJSON(JSONObject commentsJSON) {

        ArrayList<TopLevelComment> allcomments = new ArrayList<TopLevelComment>();
        try {

            JSONObject data = commentsJSON.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");

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

                        try{
                            allcomments.add(new TopLevelComment(
                                    childData.getString("author"),
                                    childData.getInt("score"),
                                    childData.getString("body"),
                                    secondLevelComments,
                                    2
                            ));
                        } catch (Exception e){
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

        drawCommentsSection(allcomments);

    }

    private String getFullLink(String link) {
        return domainStub + link + ".json";
    }

    private class CommentArrayAdapter extends ArrayAdapter<TopLevelComment> {


        private CommentArrayAdapter(Context context, ArrayList<TopLevelComment> topLevelComments){
            super(context, 0, topLevelComments);
        }

        private String formatScore(int score){
            if(score < 1000){
                return String.valueOf(score);
            } else {
                return (score / 1000) + "K";  //TODO get a better format to match site resolution
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            TopLevelComment topLevelComment = getItem(position);

            if(topLevelComment.getType() == 2){
                if(convertView == null){
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_block_with_replies, parent, false);
                    TextView authorTextView = (TextView)convertView.findViewById(R.id.comment_author_textview);
                    TextView commentTextView = (TextView)convertView.findViewById(R.id.comment_body);
                    TextView scoreTextView = (TextView)convertView.findViewById(R.id.comment_number_textview);
                    ListView subcomments = (ListView) convertView.findViewById(R.id.subcomment_holder);

                    authorTextView.setText(topLevelComment.getAuthor());
                    commentTextView.setText(topLevelComment.getComment());
                    scoreTextView.setText(formatScore(topLevelComment.getScore()));
                    ArrayList<SecondLevelComment> secondLevelComments = topLevelComment.getReplies();

                    SubCommentArrayAdapter adapter = new SubCommentArrayAdapter(getContext(), secondLevelComments);
                    subcomments.setAdapter(adapter);
                }
            } else {
                if(convertView == null){
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_block, parent, false);
                    TextView authorTextView = (TextView)convertView.findViewById(R.id.comment_author_textview);
                    TextView commentTextView = (TextView)convertView.findViewById(R.id.comment_body);
                    TextView scoreTextView = (TextView)convertView.findViewById(R.id.comment_number_textview);

                    authorTextView.setText(topLevelComment.getAuthor());
                    commentTextView.setText(topLevelComment.getComment());
                    scoreTextView.setText(formatScore(topLevelComment.getScore()));
                }
            }
            return convertView;
        }
    }

    private class SubCommentArrayAdapter extends ArrayAdapter<SecondLevelComment>{

        private SubCommentArrayAdapter(Context context, ArrayList<SecondLevelComment> secondLevelComments){
            super(context, 0, secondLevelComments);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            SecondLevelComment secondLevelComment = getItem(position);
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.subcommentrow, parent, false);
            TextView authorTextView = (TextView)convertView.findViewById(R.id.subcomment_author);
            TextView bodyTextView = (TextView)convertView.findViewById(R.id.subcomment_body);
            authorTextView.setText(secondLevelComment.getAuthor());
            bodyTextView.setText(secondLevelComment.getComment());
            return convertView;
        }
    }




}

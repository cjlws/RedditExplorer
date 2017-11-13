package com.supsim.redditexplorer;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.supsim.redditexplorer.Network.MySingleton;
import com.supsim.redditexplorer.Views.ListViewWithoutScroll;
import com.supsim.redditexplorer.data.RedditArticle;
import com.supsim.redditexplorer.data.SecondLevelComment;
import com.supsim.redditexplorer.data.TopLevelComment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {

    private RedditArticle redditArticle;

    public static final String interprocessArticleID = "interprocessArticleID";


    private String savedInstanceKeyIsSelf = "is_self";
    private int savedInstanceContentIsSelf = ARTICLE_IS_SELF_TBD;
    private String savedInstanceKeySelfTextHtml = "self_text_html";
    private String savedInstanceContentSelfTextHtml = "";
    private String savedInstanceKeyCommentsJSON = "raw_json_comments";
    private String savedInstanceContentCommentsJSON = "";
    private String savedInstanceKeyID = "saved_id";
    private String savedInstanceContentID;

    private static final int ARTICLE_IS_SELF_TBD = 0;
    private static final int ARTICLE_IS_SELF_TRUE = 1;
    private static final int ARTICLE_IS_SELF_FALSE = 2;

    RequestQueue requestQueue;
    ImageLoader imageLoader;
    TextView bodyText;
    TextView selfText;
    CommentArrayAdapter commentArrayAdapter;
    boolean phoneView = false;
    int testingCommentTicker = 0;

    // Columns to store the stats
    static final int COL_ID = 0;
    static final int COL_SCORE = 2;

    private static final String TAG = "_TEST_IDF_";


    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(interprocessArticleID)) {
            Log.d(TAG, "Incoming intent has an ID of " + getArguments().getString(interprocessArticleID));
        }

        redditArticle = new RedditArticle(getArguments());
        Log.d(TAG, "Recreated Reddit Article with an ID of " + redditArticle.getId());

        if (savedInstanceState != null) {
            Log.d(TAG, "On Create Saved ID was " + savedInstanceState.getString(savedInstanceKeyID));
        } else {
            Log.d(TAG, "On Create Saved Instance State was null");
        }

        requestQueue = MySingleton.getInstance(this.getContext()).getRequestQueue();
        imageLoader = MySingleton.getInstance(this.getContext()).getImageLoader();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(savedInstanceKeyIsSelf, savedInstanceContentIsSelf);
        savedInstanceState.putString(savedInstanceKeySelfTextHtml, savedInstanceContentSelfTextHtml);
        savedInstanceState.putString(savedInstanceKeyCommentsJSON, savedInstanceContentCommentsJSON);
        savedInstanceState.putString(savedInstanceKeyID, savedInstanceContentID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        Activity activity = this.getActivity();

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(Tools.addRToSubreddit(redditArticle.getSubreddit()));
            phoneView = true;
        }


        ArrayList<TopLevelComment> topLevelComments = new ArrayList<>();
        commentArrayAdapter = new CommentArrayAdapter(getContext(), topLevelComments);

        if (!phoneView) {

            ListView commentHolderLayout = (ListView) rootView.findViewById(R.id.comment_holder_listView);
            commentHolderLayout.setAdapter(commentArrayAdapter);

        } else {

            ListViewWithoutScroll commentHolderLayout =
                    (ListViewWithoutScroll) rootView.findViewById(R.id.comment_holder_listView);
            commentHolderLayout.setFocusable(false);
            commentHolderLayout.setClickable(false);
            commentHolderLayout.setAdapter(commentArrayAdapter);

        }

        bodyText = (TextView) rootView.findViewById(R.id.temp_textview);
        selfText = (TextView) rootView.findViewById(R.id.selfTextHolder);


        // Update the status text to let the user know comments are being downloaded
        // Truncate long titles so they better fit the screen
        if (redditArticle.getTitle() != null) {

            updatePlaceholderText(getString(R.string.placeholder_loading_comments_with_title,
                    redditArticle.getShortenedTitle(20)));

        } else {

            updatePlaceholderText(getString(R.string.placeholder_loading_comments));

        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean previousSelf = false;
        boolean previousComments = false;

        if (savedInstanceState != null) {

            // Check for previously gathered self status
            if (savedInstanceState.containsKey(savedInstanceKeyIsSelf)) {
                int isSelf = savedInstanceState.getInt(savedInstanceKeyIsSelf, ARTICLE_IS_SELF_TBD);
                switch (isSelf) {
                    case ARTICLE_IS_SELF_FALSE:
                        savedInstanceContentIsSelf = ARTICLE_IS_SELF_FALSE;
                        previousSelf = true;
                        break;
                    case ARTICLE_IS_SELF_TRUE:

                        if (savedInstanceState.containsKey(savedInstanceKeySelfTextHtml)) {
                            String previousSelfContent = savedInstanceState.getString(savedInstanceKeySelfTextHtml);
                            if (previousSelfContent != null && !previousSelfContent.isEmpty()) {
                                savedInstanceContentSelfTextHtml = previousSelfContent;
                                savedInstanceContentIsSelf = ARTICLE_IS_SELF_TRUE;
                                previousSelf = true;
                                break;
                            }
                        }

                        // In theory this is a self-type article but the actual content is missing
                        // Therefore run through the process of getting it back

                        savedInstanceContentIsSelf = ARTICLE_IS_SELF_TBD;
                        break;
                    default:
                        savedInstanceContentIsSelf = ARTICLE_IS_SELF_TBD;
                }
            }

            // Check for previously gathered comments JSON
            if (savedInstanceState.containsKey(savedInstanceKeyCommentsJSON)) {
                String commentsJSON = savedInstanceState.getString(savedInstanceKeyCommentsJSON, "");
                if (commentsJSON != null && !commentsJSON.isEmpty()) {
                    savedInstanceContentCommentsJSON = commentsJSON;
                    previousComments = true;
                }
            }
        }

        if (previousComments && previousSelf) {


            if (!savedInstanceContentSelfTextHtml.isEmpty()) {
                addHTMLToSelfText(savedInstanceContentSelfTextHtml);
            }
            try {
                JSONObject jsonObject = new JSONObject(savedInstanceContentCommentsJSON);
                populateComments(jsonObject);

            } catch (Exception e) {
                e.printStackTrace();
                getComments(redditArticle.getPermalink());
            }
        } else {
            getComments(redditArticle.getPermalink());
        }

        drawDetailsSection();
    }

    private void updatePlaceholderText(String text) {

        bodyText.setText(Tools.removeXMLStringEncoding(text));
        if (bodyText.hasOnClickListeners()) bodyText.setOnClickListener(null);
    }

    private void updatePhoneTitleText(String text, final String link) {
        bodyText.setText(Tools.removeXMLStringEncoding(text));
        bodyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Tools.createBrowserIntent(getAbsoluteLink(link)));
            }
        });
        bodyText.setTextAppearance(getActivity(), R.style.PhoneTitleLink);
    }

    private void updateErrorText(String text) {

        bodyText.setText(text);
        if (bodyText.hasOnClickListeners()) bodyText.setOnClickListener(null);
    }

    private void getComments(String link) {

        if (link != null && !link.isEmpty()) {
            String destinationUrl = getFullLink(link);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(destinationUrl, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    updatePage(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    updateErrorText(getString(R.string.error_problem_downloading_page));
                }
            });
            MySingleton.getInstance(this.getContext()).addToRequestQueue(jsonArrayRequest);
        } else {
            updateErrorText(getString(R.string.error_problem_downloading_page));
        }
    }

    private void updatePage(JSONArray response) {

        if (response != null) {
            parseJson(response);
        } else {
            updateErrorText(getString(R.string.error_no_detail_page_response));
        }
    }

    private void parseJson(JSONArray jsonArray) {

        // The returned JSON comes as two objects
        // The first (index 0) contains details of the post itself
        // The second (index 1) contains the user comments

        try {

            if (jsonArray.length() == 2) {

                checkForSelf(jsonArray.getJSONObject(0));
                populateComments(jsonArray.getJSONObject(1));

            }

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private void checkForSelf(JSONObject jsonObject) {

        // This method checks for the presence of a 'self' field in the JSON
        // This field is only present when the post contains a body of text in addition to the title
        // Usually for things like jokes and short stories
        // If it is present then a second textview on the details page need to be populated

        if (jsonObject != null) {

            try {

                JSONArray children = jsonObject.getJSONObject("data").getJSONArray("children");

                for (int i = 0; i < children.length(); i++) {

                    if (children.get(i) instanceof JSONObject) {

                        JSONObject data = ((JSONObject) children.get(i)).getJSONObject("data");
                        String selftext = data.optString("selftext_html", "");

                        if (!selftext.isEmpty() && !data.isNull("selftext_html")) {

                            savedInstanceContentIsSelf = ARTICLE_IS_SELF_TRUE;

                            addHTMLToSelfText(selftext);

                        } else {

                            savedInstanceContentIsSelf = ARTICLE_IS_SELF_FALSE;
                        }
                    }
                }

            } catch (JSONException e) {

                savedInstanceContentIsSelf = ARTICLE_IS_SELF_TBD;
                e.printStackTrace();

            }
        }
    }

    @SuppressWarnings("deprecation")
    private void addHTMLToSelfText(String selfHtml) {

        // This method takes the html version of the self text, removes extra encoding and
        // pushes it to the textview.  Any hyperlinks are rendered as clickable

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            selfText.setText(Html.fromHtml((Tools.removeXMLStringEncoding(selfHtml)), Html.FROM_HTML_MODE_COMPACT));

        } else {

            selfText.setText(Html.fromHtml(Tools.removeXMLStringEncoding(selfHtml)));

        }
        selfText.setMovementMethod(LinkMovementMethod.getInstance());
        selfText.setVisibility(View.VISIBLE);

        // Cache the finalised self text for the savedInstance
        savedInstanceContentSelfTextHtml = selfHtml;

    }

    private void drawDetailsSection() {

        // Complete the details relating to the post

        TextView subredditTextView = (TextView) getActivity().findViewById(R.id.postSubredditTextView);
        TextView authorAndTimeTextView = (TextView) getActivity().findViewById(R.id.postAuthorAndTimeStampTextView);
        TextView scoreTextView = (TextView) getActivity().findViewById(R.id.postScoreTextView);
        TextView mainLinkTextView = (TextView) getActivity().findViewById(R.id.postMainLinkTextView);
        NetworkImageView thumbnailView = (NetworkImageView) getActivity().findViewById(R.id.postThumbnailImageView);
        NetworkImageView previewImageView = (NetworkImageView) getActivity().findViewById(R.id.toolbar_network_image_holder);


        // If on a phone then the toolbar title is changed to reflect the subreddit
        // If on a tablet then the detail block is amended with both sub and domain
        if (phoneView) {

            if (subredditTextView != null) {
                subredditTextView.setText(redditArticle.getDomain());
            }
        } else {

            if (subredditTextView != null) {
                subredditTextView.setText(getString(R.string.tablet_view_sub_and_domain,
                        Tools.addRToSubreddit(redditArticle.getSubreddit()),
                        redditArticle.getDomain()));
            }
        }


        if (authorAndTimeTextView != null)
            authorAndTimeTextView.setText(Tools.formatAuthorAndTime(getActivity(),
                    redditArticle.getAuthor(), redditArticle.getCreated()));

        if (scoreTextView != null)
            scoreTextView.setText(Tools.formatScore(redditArticle.getScore()));

        // Update main page link with article title and functioning hyperlink
        if (mainLinkTextView != null) {
            mainLinkTextView.setText(redditArticle.getTitle());
            mainLinkTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(Tools.createBrowserIntent(getAbsoluteLink(redditArticle.getPermalink())));
                }
            });
            mainLinkTextView.setClickable(true);
        }

        // Only valid for the tablet view - shows a thumbnail in the detail bar
        if (thumbnailView != null && !redditArticle.getSafeThumbnail().isEmpty()) {
            thumbnailView.setImageUrl(redditArticle.getPost_thumbnail(), imageLoader);
            thumbnailView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(Tools.createBrowserIntent(getAbsoluteLink(redditArticle.getPermalink())));
                }
            });
            thumbnailView.setClickable(true);
        }


        if (phoneView) {
            updatePhoneTitleText(redditArticle.getTitle(), redditArticle.getPermalink());
        }

        // Only triggered for phone view.  Updates the toolbar if a decent enough image is available
        // That is one that has a width at least as large as the view it is going in to

        if (previewImageView != null) {

            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            String imageUrl = redditArticle.getBestPreviewImage(metrics.widthPixels);
            previewImageView.setImageUrl(imageUrl, imageLoader);
        }

    }

    private void populateComments(JSONObject commentsJSON) {


        drawCommentsSection(parseCommentsJSON(commentsJSON));

        try {
            savedInstanceContentCommentsJSON = commentsJSON.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void drawCommentsSection(ArrayList<TopLevelComment> topLevelComments) {

        commentArrayAdapter.clear();
        commentArrayAdapter.addAll(topLevelComments);

        if (!phoneView) {
            updatePlaceholderText(getString(R.string.tablet_view_showing_number_of_comments,
                    topLevelComments.size()));
        }

        commentArrayAdapter.notifyDataSetChanged();
    }


    private ArrayList<TopLevelComment> parseCommentsJSON(JSONObject commentsJSON) {

        // This method takes the raw JSON feed of comments, parses the first two levels of depth,
        // converts each entry into a TopLevelComment object and bundles them into an
        // ArrayList to be returned

        ArrayList<TopLevelComment> allcomments = new ArrayList<>();
        try {

            JSONObject data = commentsJSON.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");

            for (int i = 0; i < children.length(); i++) {

                //Sanity check - they should all be Objects rather than Arrays
                if (children.get(i) instanceof JSONObject) {

                    // Ignore 'more' listings as they are not being used
                    if (!children.getJSONObject(i).getString("kind").equals("more")) {

                        //We want the 'T1' listings as they contain the goodies
                        if (children.getJSONObject(i).getString("kind").equals("t1")) {

                            //Within the 'T1' we want the 'data'
                            JSONObject sdfdsf = children.getJSONObject(i).getJSONObject("data");

                            //Splitting now between those that have replies and those that do not
                            //Replies come back as valid JSON and no replies do not
                            try {
                                JSONObject replies = sdfdsf.getJSONObject("replies");

                                //Now attempt to grab the bodies of any second-level replies
                                ArrayList<SecondLevelComment> secondLevelComments = new ArrayList<>();

                                try {
                                    JSONObject subData = replies.getJSONObject("data");
                                    JSONArray subChildren = subData.getJSONArray("children");

                                    // The second level replies come back as an array.
                                    // Iterate over to grab each body
                                    for (int subcount = 0; subcount < subChildren.length(); subcount++) {

                                        JSONObject subChild = subChildren.getJSONObject(subcount);

                                        if (subChild.getString("kind").equals("t1")) {

                                            JSONObject subChildData = subChild.getJSONObject("data");

                                            try {
                                                SecondLevelComment secondLevelComment = new SecondLevelComment(
                                                        subChildData.optString("author", getString(R.string.comment_author_error_placeholder)),
                                                        subChildData.optString("body", getString(R.string.comment_body_error_placholder)));

                                                secondLevelComments.add(secondLevelComment);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }


                                        }
                                    }
                                } catch (JSONException e) {
                                    Log.d(TAG, "Error gettings subcomments for message " + i);
                                }


                                if (secondLevelComments.size() > 0) {

                                    allcomments.add(new TopLevelComment(
                                            sdfdsf.optString("author", getString(R.string.comment_author_error_placeholder)),
                                            sdfdsf.optInt("score"),
                                            sdfdsf.optString("body", getString(R.string.comment_body_error_placholder)),
                                            secondLevelComments,
                                            2
                                    ));
                                } else {
                                    allcomments.add(new TopLevelComment(
                                            sdfdsf.optString("author", getString(R.string.comment_author_error_placeholder)),
                                            sdfdsf.optInt("score"),
                                            sdfdsf.optString("body", getString(R.string.comment_body_error_placholder)),
                                            null,
                                            1
                                    ));
                                }


                            } catch (JSONException e) {
                                allcomments.add(new TopLevelComment(
                                        sdfdsf.optString("author", getString(R.string.comment_author_error_placeholder)),
                                        sdfdsf.optInt("score"),
                                        sdfdsf.optString("body", getString(R.string.comment_body_error_placholder)),
                                        null,
                                        1
                                ));
                            }
                        }
                    }

                }

                //This should never happen
                else if (children.get(i) instanceof JSONArray) {
                    Log.d(TAG, "Comment " + i + " is an Array");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allcomments;

    }

    private String getFullLink(String link) {

        // Takes the permalink of the article and adds to it to pull back the json version

        return getAbsoluteLink(link) + ".json";
    }

    private String getAbsoluteLink(String link) {
        return Tools.getAbsoluteLink(getString(R.string.domain_stub), link);
    }

    private class CommentArrayAdapter extends ArrayAdapter<TopLevelComment> {

        private CommentArrayAdapter(Context context, ArrayList<TopLevelComment> topLevelComments) {
            super(context, 0, topLevelComments);

        }


        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            View row;

            testingCommentTicker++;
            TopLevelComment topLevelComment = getItem(position);

            if (topLevelComment != null) {

                if (topLevelComment.getType() == 2) {
                    // This is the case for comments that have subcomments

                    if (convertView == null) {
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        row = inflater.inflate(R.layout.comment_block_with_replies, parent, false);
                    } else {
                        row = convertView;
                    }
                } else {
                    // This is the case for standalone comments, ie: ones without replies

                    if (convertView == null) {

                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        row = inflater.inflate(R.layout.comment_block, parent, false);

                    } else {
                        row = convertView;
                    }
                }


                TextView authorTextView = (TextView) row.findViewById(R.id.comment_author_textview);
                TextView commentTextView = (TextView) row.findViewById(R.id.comment_body);
                TextView scoreTextView = (TextView) row.findViewById(R.id.comment_number_textview);
                ListViewWithoutScroll subcomments = (ListViewWithoutScroll) row.findViewById(R.id.subcomment_holder);


                if (authorTextView != null)
                    authorTextView.setText(topLevelComment.getAuthor());
                if (commentTextView != null) commentTextView.setText(topLevelComment.getComment());
                if (scoreTextView != null)
                    scoreTextView.setText(Tools.formatScore(topLevelComment.getScore()));

                if (subcomments != null) {

                    // If the comment has responses then these are rendered in their own list below
                    // the top comment

                    ArrayList<SecondLevelComment> secondLevelComments = topLevelComment.getReplies();

                    if (secondLevelComments != null) {

                        SubCommentArrayAdapter adapter = new SubCommentArrayAdapter(getContext(),
                                secondLevelComments);
                        subcomments.setFocusable(false);
                        subcomments.setAdapter(adapter);
                    }

                }
            } else {
                // This should never happen but just in case a dead comment gets passed in...

                if (convertView == null) {

                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    row = inflater.inflate(R.layout.comment_block, parent, false);

                    TextView authorTextView = (TextView) row.findViewById(R.id.comment_author_textview);
                    TextView commentTextView = (TextView) row.findViewById(R.id.comment_body);

                    if (authorTextView != null)
                        authorTextView.setText(getString(R.string.comment_author_error_placeholder));
                    if (commentTextView != null) commentTextView.setText(
                            getString(R.string.comment_body_error_placholder));

                } else {

                    row = convertView;

                }
            }
            return row;
        }
    }

    private class SubCommentArrayAdapter extends ArrayAdapter<SecondLevelComment> {

        private SubCommentArrayAdapter(Context context, ArrayList<SecondLevelComment> secondLevelComments) {
            super(context, 0, secondLevelComments);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Subcomments are replies to the top level comment

            SecondLevelComment secondLevelComment = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();

                convertView = inflater.inflate(R.layout.subcommentrow, parent, false);

                TextView authorTextView = (TextView) convertView.findViewById(R.id.subcomment_author);
                TextView bodyTextView = (TextView) convertView.findViewById(R.id.subcomment_body);

                if (secondLevelComment != null) {
                    if (authorTextView != null)
                        authorTextView.setText(secondLevelComment.getAuthor());
                    if (bodyTextView != null) bodyTextView.setText(secondLevelComment.getComment());
                } else {
                    authorTextView.setText(getString(R.string.comment_author_error_placeholder));
                    bodyTextView.setText(getString(R.string.comment_body_error_placholder));
                }
            }

            return convertView;
        }
    }


}

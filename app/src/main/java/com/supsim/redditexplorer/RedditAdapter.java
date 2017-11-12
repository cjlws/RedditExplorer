package com.supsim.redditexplorer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.supsim.redditexplorer.Network.MySingleton;
import com.supsim.redditexplorer.data.RedditArticle;

import static com.supsim.redditexplorer.Tools.addRToSubreddit;
import static com.supsim.redditexplorer.Tools.formatAuthorAndTime;
import static com.supsim.redditexplorer.Tools.formatScore;
import static com.supsim.redditexplorer.Tools.removeXMLStringEncoding;

class RedditAdapter extends RecyclerView.Adapter<RedditAdapter.RedditAdapterViewHolder> {


    private Cursor mCursor;
    private boolean mTwoPane;
    private static final String TAG = "RedditAdapter";
    private Context mContext;
    private FragmentManager fragmentManager;
    private static final int TYPE_TOP_CARD = 1;
    private static final int TYPE_SECOND_CARD = 2;
    private static final int TYPE_SUB_CARD = 3;

    class RedditAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView subredditTextView;
        final TextView titleTextView;
        final TextView authorTextView;
        final NetworkImageView thumbnail;
        final TextView scoreTextView;
        final TextView commentsTextView;
        final TextView domainTextView;
        final NetworkImageView previewImage;


        RedditAdapterViewHolder(final View view) {
            super(view);
            mContext = view.getContext();
            this.subredditTextView = (TextView) view.findViewById(R.id.subreddittextview);
            this.titleTextView = (TextView) view.findViewById(R.id.titletextview);
            this.authorTextView = (TextView) view.findViewById(R.id.authortextview);
            this.thumbnail = (NetworkImageView) view.findViewById(R.id.thumbnailHolder);
            this.scoreTextView = (TextView) view.findViewById(R.id.list_content_top_score);
            this.commentsTextView = (TextView) view.findViewById(R.id.list_content_top_comments);
            this.domainTextView = (TextView) view.findViewById(R.id.item_list_top_domain);
            this.previewImage = (NetworkImageView) view.findViewById(R.id.previewImageHolder);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    RedditArticle redditArticle = new RedditArticle(mCursor);

                    if (mTwoPane) {

                        Bundle arguments = redditArticle.getRedditArticleBundle();
                        ItemDetailFragment fragment = new ItemDetailFragment();
                        fragment.setArguments(arguments);

                        try {
                            if (fragmentManager != null) {
                                fragmentManager.beginTransaction().replace(R.id.item_detail_container, fragment).commit();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!"tutorial".equals(redditArticle.getId())) {

                            Intent intent = new Intent(itemView.getContext(), ItemDetailActivity.class);
                            intent.putExtras(redditArticle.getRedditArticleBundle());
                            Log.d(TAG, redditArticle.getRedditArticleBundle().toString());
                            itemView.getContext().startActivity(intent);
                        }
                    }
                }
            }
        }
    }


    RedditAdapter(FragmentManager manager, Cursor cursor, boolean twoPane) {
        this.mCursor = cursor;
        this.mTwoPane = twoPane;
        this.fragmentManager = manager;
    }

    boolean hasFragmentManager() {
        return this.fragmentManager != null;
    }


    @Override
    public RedditAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {

            if (viewType == TYPE_TOP_CARD) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_content_top, viewGroup, false);
                view.setFocusable(true);
                return new RedditAdapterViewHolder(view);
            } else if (viewType == TYPE_SECOND_CARD) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_content_second, viewGroup, false);
                return new RedditAdapterViewHolder(view);
            } else {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_content, viewGroup, false);
                return new RedditAdapterViewHolder(view);
            }
        } else {
            throw new RuntimeException("Not Bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(final RedditAdapterViewHolder redditAdapterViewHolder, int position) {


        mCursor.moveToPosition(position);

        if (redditAdapterViewHolder.getItemViewType() == TYPE_TOP_CARD) {

            redditAdapterViewHolder.thumbnail.setImageUrl(null, null);  // Flatten thumbnail
            redditAdapterViewHolder.previewImage.setImageUrl(null, null);  // Flatten preview image
            if (redditAdapterViewHolder.previewImage.getVisibility() == View.VISIBLE) // Hide Preview
                redditAdapterViewHolder.previewImage.setVisibility(View.GONE);


            final RedditArticle redditArticle = new RedditArticle(mCursor);  // Recreate article from cursor

            if ("tutorial".equals(redditArticle.getId())) {
                // If the card type is the tutorial then special formatting needs to be done
                redditAdapterViewHolder.authorTextView.setVisibility(View.GONE);
                redditAdapterViewHolder.titleTextView.setMaxLines(20);
            } else {
                // If normal article then make sure any changes made by tutorial are undone
                if (redditAdapterViewHolder.authorTextView.getVisibility() != View.VISIBLE) {
                    redditAdapterViewHolder.authorTextView.setVisibility(View.VISIBLE);
                }

                redditAdapterViewHolder.authorTextView.setText(formatAuthorAndTime(mContext,
                        removeXMLStringEncoding(redditArticle.getAuthor()),
                        redditArticle.getCreated()
                ));

                redditAdapterViewHolder.scoreTextView.setText(formatScore(redditArticle.getScore()));
                redditAdapterViewHolder.domainTextView.setText(formatDomain(redditArticle.getDomain()));
            }


            redditAdapterViewHolder.subredditTextView.setText(Tools.addRToSubreddit(redditArticle.getSubreddit()));
            redditAdapterViewHolder.titleTextView.setText(redditArticle.getTitle());


            // Attempt to show some sort of image in the card (to make it all pretty)
            // First check to see if there is a preview image that is a good fit for the device
            // If there are then display it (either full-width or second best)
            // If there is not then see if there is a thumbnail available - use if there is
            // Failing all of the above just go with the text


            if (!redditArticle.getPreviewOptions().isEmpty()) {

                redditAdapterViewHolder.previewImage.setVisibility(View.VISIBLE);


                int width = redditAdapterViewHolder.previewImage.getMeasuredWidth();
                if (width == 0) {
                    try {

                        ViewTreeObserver viewTreeObserver = redditAdapterViewHolder.previewImage.getViewTreeObserver();

                        viewTreeObserver.addOnGlobalLayoutListener(
                                new ViewTreeObserver.OnGlobalLayoutListener() {
                                    @Override
                                    public void onGlobalLayout() {
                                        redditAdapterViewHolder.previewImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                                        int width = redditAdapterViewHolder.previewImage.getMeasuredWidth();
                                        if (width != 0) {
                                            determineAndDisplayBestImage(redditAdapterViewHolder, redditArticle, width);
                                        }
                                    }
                                });

                    } catch (Exception e) {
                        // Problem doing the whole width measurement thing so make so to clean up
                        if (redditAdapterViewHolder.previewImage.getVisibility() == View.VISIBLE) {
                            redditAdapterViewHolder.previewImage.setVisibility(View.GONE);
                        }
                        e.printStackTrace();
                    }


                } else {
                    determineAndDisplayBestImage(redditAdapterViewHolder, redditArticle, width);
                }

            } else {
                if (redditAdapterViewHolder.previewImage.getVisibility() == View.VISIBLE) {
                    redditAdapterViewHolder.previewImage.setVisibility(View.GONE);
                }

                if (!redditArticle.getSafeThumbnail().isEmpty()) {

                    ImageLoader imageLoader = MySingleton.getInstance(mContext).getImageLoader();
                    if (redditAdapterViewHolder.thumbnail.getVisibility() == View.GONE)
                        redditAdapterViewHolder.thumbnail.setVisibility(View.VISIBLE);
                    redditAdapterViewHolder.thumbnail.setAdjustViewBounds(true);
                    redditAdapterViewHolder.thumbnail.setImageUrl(redditArticle.getSafeThumbnail(), imageLoader);

                } else {
                    if (redditAdapterViewHolder.thumbnail.getVisibility() == View.VISIBLE)
                        redditAdapterViewHolder.thumbnail.setVisibility(View.GONE);
                }
            }


        } else {

            // This clause is for the second and subsequent articles

            String redditSubreddit = addRToSubreddit(mCursor.getString(ItemListActivity.COL_SUBREDDIT));
            String redditTitle = removeXMLStringEncoding(mCursor.getString(ItemListActivity.COL_TITLE));
            String redditAuthor = removeXMLStringEncoding(mCursor.getString(ItemListActivity.COL_AUTHOR));

            redditAdapterViewHolder.subredditTextView.setText(redditSubreddit);
            redditAdapterViewHolder.titleTextView.setText(redditTitle);
            redditAdapterViewHolder.authorTextView.setText(redditAuthor);

        }

    }

    private void determineAndDisplayBestImage(RedditAdapterViewHolder redditAdapterViewHolder, RedditArticle redditArticle, int width) {

        String bestPreview = redditArticle.getBestPreviewImage(width);

        if (bestPreview.isEmpty()) {

            // Big preview wasn't available so attempting to find alternatives

            redditAdapterViewHolder.previewImage.setVisibility(View.GONE);  // Hide preview image

            String nextBestPreview = redditArticle.getBestPreviewImage(width / 3);

            if (!nextBestPreview.isEmpty()) {

                // There is a semi-decent view available - put it in the thumbnail bit

                ImageLoader imageLoader = MySingleton.getInstance(mContext).getImageLoader();
                if (redditAdapterViewHolder.thumbnail.getVisibility() == View.GONE)
                    redditAdapterViewHolder.thumbnail.setVisibility(View.VISIBLE);
                redditAdapterViewHolder.thumbnail.setAdjustViewBounds(true);
                redditAdapterViewHolder.thumbnail.setImageUrl(nextBestPreview, imageLoader);
                redditAdapterViewHolder.thumbnail.setContentDescription(" To Do");

            } else {

                if (redditAdapterViewHolder.previewImage.getVisibility() == View.VISIBLE)
                    redditAdapterViewHolder.previewImage.setVisibility(View.GONE);

                if (!redditArticle.getSafeThumbnail().isEmpty()) {

                    // Case is that there is no suitable preview - trying thumbnail

                    ImageLoader imageLoader = MySingleton.getInstance(mContext).getImageLoader();
                    if (redditAdapterViewHolder.thumbnail.getVisibility() == View.GONE)
                        redditAdapterViewHolder.thumbnail.setVisibility(View.VISIBLE);
                    redditAdapterViewHolder.thumbnail.setAdjustViewBounds(true);
                    redditAdapterViewHolder.thumbnail.setImageUrl(redditArticle.getSafeThumbnail(), imageLoader);

                }
            }

        } else {

            if (redditAdapterViewHolder.thumbnail.getVisibility() == View.VISIBLE)
                redditAdapterViewHolder.thumbnail.setVisibility(View.GONE);

            // Best Preview wasn't empty so attempt to put the big image in place now

            redditAdapterViewHolder.previewImage.setVisibility(View.VISIBLE);
            ImageLoader imageLoader = MySingleton.getInstance(mContext).getImageLoader();
            redditAdapterViewHolder.previewImage.setImageUrl(bestPreview, imageLoader);
        }

    }


    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return TYPE_TOP_CARD;
            case 1:
                return TYPE_SECOND_CARD;
            default:
                return TYPE_SUB_CARD;
        }
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    String getItemID(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getString(ItemListActivity.COL_ID);
    }

    @NonNull
    private String formatDomain(@NonNull String domain) {
        return mContext.getString(
                R.string.formatting_format_domain_with_brackets, domain);
    }

}

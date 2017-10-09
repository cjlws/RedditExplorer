package com.supsim.redditexplorer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.supsim.redditexplorer.Network.MySingleton;
import com.supsim.redditexplorer.data.RedditArticleContract;

import static com.supsim.redditexplorer.ItemDetailFragment.interprocessLink;
import static com.supsim.redditexplorer.ItemDetailFragment.interprocessSubreddit;
import static com.supsim.redditexplorer.ItemDetailFragment.interprocessTitle;


public class RedditAdapter extends RecyclerView.Adapter<RedditAdapter.RedditAdapterViewHolder>  {


    private Cursor mCursor;
    private boolean mTwoPane;
    private static final String TAG = "RedditAdapter";
    private Context mContext;
    private FragmentManager fragmentManager;
    private static final int TYPE_TOP_CARD = 1;
    private static final int TYPE_SECOND_CARD = 2;
    private static final int TYPE_SUB_CARD = 3;

    public class RedditAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

//        String test = ItemDetailFragment.ARG_ITEM_ID;

    public final TextView subredditTextView;
        public final TextView titleTextView;
        public final TextView authorTextView;
        public final NetworkImageView thumbnail;

        public RedditAdapterViewHolder(final View view){
            super(view);
            mContext = view.getContext();
            this.subredditTextView = (TextView)view.findViewById(R.id.subreddittextview);
            this.titleTextView = (TextView)view.findViewById(R.id.titletextview);
            this.authorTextView = (TextView)view.findViewById(R.id.authortextview);
            this.thumbnail = (NetworkImageView)view.findViewById(R.id.listViewThumbnail);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){

            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                Log.d(TAG, "Positon: " + position);
                if (mCursor != null){
                    mCursor.moveToPosition(position);
                    String link = mCursor.getString(ItemListActivity.COL_PERMALINK);
                    if (link != null){
                        if(mTwoPane){
                            Bundle arguments = new Bundle();
                            arguments.putString(ItemDetailFragment.interprocessTitle, mCursor.getString(ItemListActivity.COL_TITLE));
                            arguments.putString(ItemDetailFragment.interprocessSubreddit, mCursor.getString(ItemListActivity.COL_SUBREDDIT));
                            arguments.putString(ItemDetailFragment.interprocessLink, mCursor.getString(ItemListActivity.COL_PERMALINK));
                            ItemDetailFragment fragment = new ItemDetailFragment();
                            fragment.setArguments(arguments);
                            try {
                                if(fragmentManager != null){
                                    fragmentManager.beginTransaction().replace(R.id.item_detail_container, fragment).commit();
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        } else {
                            Intent intent = new Intent(itemView.getContext(), ItemDetailActivity.class);
                            intent.putExtra(interprocessTitle, mCursor.getString(ItemListActivity.COL_TITLE));
                            intent.putExtra(interprocessSubreddit, mCursor.getString(ItemListActivity.COL_SUBREDDIT));
                            intent.putExtra(interprocessLink, link);
                            itemView.getContext().startActivity(intent);
                        }
                    } else {
                        Log.d(TAG, "Link was null");
                    }
                } else {
                    Log.d(TAG, "Cursor = null");
                }
            }
        }
    }



    public RedditAdapter(FragmentManager manager, Cursor cursor, boolean twoPane){
        mCursor = cursor;
        mTwoPane = twoPane;
        this.fragmentManager = manager;
    }

    @Override
    public RedditAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        if(viewGroup instanceof RecyclerView){

            if(viewType  == TYPE_TOP_CARD) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_content_top, viewGroup, false);
                view.setFocusable(true);
                return new RedditAdapterViewHolder(view);
            }
            else if(viewType == TYPE_SECOND_CARD){
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_content_second, viewGroup, false);
                view.setFocusable(true);
                return new RedditAdapterViewHolder(view);
            } else {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_content, viewGroup, false);
                view.setFocusable(true);
                return new RedditAdapterViewHolder(view);
            }
        } else {
            throw new RuntimeException("Not Bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(RedditAdapterViewHolder redditAdapterViewHolder, int position){
        mCursor.moveToPosition(position);

        if(redditAdapterViewHolder.getItemViewType() == TYPE_TOP_CARD){
//            int redditId = mCursor.getInt(ItemListActivity.COL_ID);
            String redditSubreddit = mCursor.getString(ItemListActivity.COL_SUBREDDIT);
            String redditTitle = mCursor.getString(ItemListActivity.COL_TITLE);
            String redditAuthor = mCursor.getString(ItemListActivity.COL_AUTHOR);
            String redditThumbnail = mCursor.getString(ItemListActivity.COL_THUMBNAIL);

            if(redditThumbnail != null){

                if(redditThumbnail.isEmpty() || redditThumbnail.equals("default")  || redditThumbnail.equals("self") || redditThumbnail.equals("image")){
                        redditAdapterViewHolder.thumbnail.setVisibility(View.GONE);
                    } else {

                        if(redditAdapterViewHolder.thumbnail != null){
                            Log.d(TAG, "Yay, there's a thumbnail holdr!");

                            redditAdapterViewHolder.thumbnail.setVisibility(View.VISIBLE);
                            ImageLoader imageLoader = MySingleton.getInstance(mContext).getImageLoader();
                            redditAdapterViewHolder.thumbnail.setImageUrl(redditThumbnail, imageLoader);

                        }

                    }

            } else {
                Log.d("THUMB", "Thumbnail was null");
            }

            redditAdapterViewHolder.subredditTextView.setText(redditSubreddit);
            redditAdapterViewHolder.titleTextView.setText(redditTitle);
            redditAdapterViewHolder.authorTextView.setText(redditAuthor);

            try{
                Log.d(TAG, "DATA: " + mCursor.getString(ItemListActivity.COL_PERMALINK));
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            String redditId = mCursor.getString(ItemListActivity.COL_ID);
            String redditSubreddit = mCursor.getString(ItemListActivity.COL_SUBREDDIT);
            String redditTitle = mCursor.getString(ItemListActivity.COL_TITLE);
            String redditAuthor = mCursor.getString(ItemListActivity.COL_AUTHOR);

            redditAdapterViewHolder.subredditTextView.setText(redditSubreddit);
            redditAdapterViewHolder.titleTextView.setText(redditId + " " + redditTitle);
            redditAdapterViewHolder.authorTextView.setText(redditAuthor);

            try{
                Log.d(TAG, "DATA: " + mCursor.getString(ItemListActivity.COL_PERMALINK));
            } catch (Exception e){
                e.printStackTrace();
            }
        }


    }

    @Override
    public int getItemViewType(int position){
        switch (position){
            case 0:
                return TYPE_TOP_CARD;
            case 1:
                return TYPE_SECOND_CARD;
            default:
                return TYPE_SUB_CARD;
        }
//        return position == 0 ? TYPE_TOP_CARD : TYPE_SUB_CARD;
    }

    @Override
    public int getItemCount(){
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public Cursor getCursor(){
        return mCursor;
    }

    public void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public String getItemID(int position){
        mCursor.moveToPosition(position);
        return mCursor.getString(ItemListActivity.COL_ID);
    }
}

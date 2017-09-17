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

import static com.supsim.redditexplorer.ItemDetailFragment.interprocessLink;
import static com.supsim.redditexplorer.ItemDetailFragment.interprocessSubreddit;
import static com.supsim.redditexplorer.ItemDetailFragment.interprocessTitle;


public class RedditAdapter extends RecyclerView.Adapter<RedditAdapter.RedditAdapterViewHolder> {


    private Cursor mCursor;
    private boolean mTwoPane;
    private static final String TAG = "RedditAdapter";
    private Context mContext;
    private FragmentManager fragmentManager;


    public class RedditAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        String test = ItemDetailFragment.ARG_ITEM_ID;

    public final TextView subredditTextView;
        public final TextView titleTextView;
        public final TextView authorTextView;

        public RedditAdapterViewHolder(final View view){
            super(view);
            this.subredditTextView = (TextView)view.findViewById(R.id.subreddittextview);
            this.titleTextView = (TextView)view.findViewById(R.id.titletextview);
            this.authorTextView = (TextView)view.findViewById(R.id.authortextview);
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
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_content, viewGroup, false);
            view.setFocusable(true);
            return new RedditAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not Bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(RedditAdapterViewHolder redditAdapterViewHolder, int position){
        mCursor.moveToPosition(position);

        int redditId = mCursor.getInt(ItemListActivity.COL_ID);
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
//        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}

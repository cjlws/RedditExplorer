package com.supsim.redditexplorer;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by johnrobinson on 11/09/2017.
 */

public class RedditAdapter extends RecyclerView.Adapter<RedditAdapter.RedditAdapterViewHolder> {


    private Cursor mCursor;
    final private Context mContext;
    final private RedditAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;
    //TODO Might not need itemchoice

    public class RedditAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView subredditTextView;
        public final TextView titleTextView;
        public final TextView authorTextView;

        public RedditAdapterViewHolder(View view){
            super(view);
            subredditTextView = (TextView)view.findViewById(R.id.subreddittextview);
            titleTextView = (TextView)view.findViewById(R.id.titletextview);
            authorTextView = (TextView)view.findViewById(R.id.authortextview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view){

        }
    }

    public static interface RedditAdapterOnClickHandler {
        //TODO not sure if this is needed
        void onClick(Long date, RedditAdapterViewHolder viewHolder);
    }

    public RedditAdapter(Context context, RedditAdapterOnClickHandler onClickHandler, View emptyView, int choiceMode){
        //TODO Look at refactoring these names
        mContext = context;
        mClickHandler = onClickHandler;
        mEmptyView = emptyView;

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
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}

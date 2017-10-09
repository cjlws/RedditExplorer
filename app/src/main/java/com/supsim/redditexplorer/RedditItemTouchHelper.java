package com.supsim.redditexplorer;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class RedditItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private RedditItemTouchHelperListener listener;

    public RedditItemTouchHelper(int dragDirs, int swipeDirs, RedditItemTouchHelperListener listener){
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target){
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction){
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    public interface RedditItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int poisition);
    }
}

package com.supsim.redditexplorer;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by johnrobinson on 12/10/2017.
 */

public class PhoneTutorialAdapter extends RecyclerView.Adapter<PhoneTutorialAdapter.PhoneTutorialViewHolder>{

    private FragmentManager fragmentManager;

    public class PhoneTutorialViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        public PhoneTutorialViewHolder(final View view){
            super(view);
        }

        @Override
        public void onClick(View view){

        }
    }

    public PhoneTutorialAdapter(FragmentManager manager){
        this.fragmentManager = manager;
    }

    @Override
    public PhoneTutorialViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){

        if(viewGroup instanceof RecyclerView){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.phone_tutorial_item, viewGroup, false);
            view.setFocusable(true);
            return new PhoneTutorialViewHolder(view);
        } else {
            throw new RuntimeException("Not Bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(PhoneTutorialViewHolder phoneTutorialViewHolder, int position){
        // Add text to view, etc
    }

    @Override
    public int getItemCount(){
        return 0;  //TODO
    }
}

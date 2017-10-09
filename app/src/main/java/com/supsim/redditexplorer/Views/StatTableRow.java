package com.supsim.redditexplorer.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.supsim.redditexplorer.R;
import com.supsim.redditexplorer.data.Stat;


public class StatTableRow extends LinearLayout {

    public StatTableRow(Context context){
        this(context, null);
    }

    public StatTableRow(Context context, AttributeSet attributeSet){
        this(context, attributeSet, 0);
    }

    public StatTableRow(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
        setOrientation(HORIZONTAL);
    }

    public void addStatRow(int rank, String subreddit, int views){
        addView(createRow(rank, subreddit, views));
    }



    private View createRow(int rank, String subreddit, int views){
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.stats_table_row, null);
        TextView rankText = (TextView)view.findViewById(R.id.stats_table_row_rank);
        TextView subText = (TextView)view.findViewById(R.id.stats_table_row_subreddit);
        TextView viewText = (TextView)view.findViewById(R.id.stats_table_row_views);

        rankText.setText(String.valueOf(rank));
        subText.setText(subreddit);
        viewText.setText(String.valueOf(views));

        return view;
    }
}

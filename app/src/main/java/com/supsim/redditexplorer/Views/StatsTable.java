package com.supsim.redditexplorer.Views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.supsim.redditexplorer.R;
import com.supsim.redditexplorer.Tools;
import com.supsim.redditexplorer.data.Stat;

public class StatsTable  extends LinearLayout {

    public static final int ODD_TABLE_ROW = 1;
    public static final int EVEN_TABLE_ROW = 2;
    public static final int HEADER_TABLE_ROW = 3;

    public StatsTable(Context context){
        this(context, null);
    }

    public StatsTable(Context context, AttributeSet attributeSet){
        this(context, attributeSet, 0);
    }

    public StatsTable(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
        setOrientation(VERTICAL);
    }

    public void addHeaderRow(String rankLabel, String subredditLabel, String viewsLabel){
        addView(createRow(rankLabel, subredditLabel, viewsLabel, HEADER_TABLE_ROW));
    }

    public void addStatRow(Stat stat, int rowType){
        addView(createRow(String.valueOf(stat.getRank()), stat.getSubreddit(),
                String.valueOf(stat.getNumberOfViews()), rowType));
    }

    private View createRow(String rank, String subreddit, String views, int rowType){
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        switch (rowType){
            case HEADER_TABLE_ROW:
                view = inflater.inflate(R.layout.stats_table_row_header, null);
                break;
            case ODD_TABLE_ROW:
                view = inflater.inflate(R.layout.stats_table_row_odd, null);
                break;
            case EVEN_TABLE_ROW:
                view = inflater.inflate(R.layout.stats_table_row_even, null);
                break;
            default:
                view = inflater.inflate(R.layout.stats_table_row, null);
        }

        TextView rankText = (TextView)view.findViewById(R.id.stats_table_row_rank);
        TextView subText = (TextView)view.findViewById(R.id.stats_table_row_subreddit);
        TextView viewText = (TextView)view.findViewById(R.id.stats_table_row_views);

        rankText.setText(rank);
        subText.setText((rowType == HEADER_TABLE_ROW)? subreddit : Tools.addRToSubreddit(subreddit));
        viewText.setText(views);

        return view;
    }

}

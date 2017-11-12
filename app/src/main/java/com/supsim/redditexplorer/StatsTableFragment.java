package com.supsim.redditexplorer;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.supsim.redditexplorer.Views.StatsTable;
import com.supsim.redditexplorer.data.Stat;
import com.supsim.redditexplorer.data.StatsRecordContract;

import java.util.ArrayList;

public class StatsTableFragment extends Fragment {

    StatsTable statsTable;
    TextView emptyTableTextView;

    public StatsTableFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.stats_table_holder, container, false);
        statsTable = (StatsTable)rootView.findViewById(R.id.stats_table_row_holder);
        emptyTableTextView = (TextView)rootView.findViewById(R.id.empty_stats_table_text_view);
        fillTable(getAllStats(container));
        return rootView;
    }

    private void fillTable(ArrayList<Stat> stats) {

        if(stats.isEmpty()) {
            if(emptyTableTextView.getVisibility() == View.GONE)
                emptyTableTextView.setVisibility(View.VISIBLE);

        } else {
            if(emptyTableTextView.getVisibility() == View.VISIBLE)
                emptyTableTextView.setVisibility(View.GONE);

            statsTable.addHeaderRow(getActivity().getString(R.string.stats_table_header_label_rank),
                    getActivity().getString(R.string.stats_table_header_label_sub),
                    getActivity().getString(R.string.stats_table_header_label_likes));

            int row = 1;
            for (Stat stat : stats) {

                if ((row % 2) == 0) {
                    statsTable.addStatRow(stat, StatsTable.EVEN_TABLE_ROW);
                } else {
                    statsTable.addStatRow(stat, StatsTable.ODD_TABLE_ROW);
                }
                row++;
            }
        }
    }

    private ArrayList<Stat> getAllStats(ViewGroup container) {

        ArrayList<Stat> allStats = new ArrayList<>();

        if(container != null) {
            Cursor cursor = container.getContext().getContentResolver().query(
                    StatsRecordContract.Stats.CONTENT_URI,
                    null,
                    null,
                    null,
                    StatsRecordContract.Stats.COL_STAT_COUNT + " DESC");



            if (cursor != null) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    allStats.add(new Stat(i + 1, cursor.getString(1), cursor.getInt(2)));
                    cursor.moveToNext();
                }
            }

            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        return allStats;

    }
}

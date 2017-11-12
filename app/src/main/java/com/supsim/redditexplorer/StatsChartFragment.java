package com.supsim.redditexplorer;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.supsim.redditexplorer.data.Stat;
import com.supsim.redditexplorer.data.StatsRecordContract;

import java.util.ArrayList;
import java.util.List;

public class StatsChartFragment extends Fragment {

    // Limits the maximum slices of the pie chart
    // to improve readability
    private static final int maxNumberSegments = 6;

    public StatsChartFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){

        return inflater.inflate(R.layout.stats_chart_holder, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PieChart chart = (PieChart)view.findViewById(R.id.main_pie_chart);

        ArrayList<Stat> allStats = getAllStats();
        List<PieEntry> entries = new ArrayList<>();

        int segment = 0;
        int otherViews = 0;
        for(Stat stat : allStats){
            if(segment < maxNumberSegments) {
                entries.add(stat.getPieEntry());
            } else {
                otherViews += stat.getNumberOfViews();
            }
            segment++;
        }
        if(otherViews > 0) entries.add((new Stat(
                maxNumberSegments+1,
                getString(R.string.stats_chart_label_for_other),
                otherViews)).getPieEntry());


        if(!entries.isEmpty()){

            PieDataSet set = new PieDataSet(entries, getString(R.string.stats_chart_label_for_data));
            set.setColors(ColorTemplate.VORDIPLOM_COLORS);
            set.setValueTextColor(ContextCompat.getColor(getContext(),
                    R.color.cardview_dark_background));
            set.setValueTextSize(12f);  // This does do percentages....


            PieData data = new PieData(set);

            data.setValueFormatter(new PercentFormatter());
            data.setDrawValues(true);  // Turns percentage on and off
            chart.setUsePercentValues(true);
            chart.setCenterText(getString(R.string.stats_chart_label_for_pie_centre));

            Description description = new Description();
            description.setEnabled(false);
            chart.setDescription(description);

            Legend legend = chart.getLegend();
            legend.setEnabled(false);

            chart.setEntryLabelColor(ContextCompat.getColor(getContext(),
                    R.color.cardview_dark_background)); 
            chart.setEntryLabelTextSize(10f);


            chart.animateX(1500);
            chart.setData(data);
        }

        chart.invalidate();

    }

    private ArrayList<Stat> getAllStats() {

        Cursor cursor = getActivity().getContentResolver().query(StatsRecordContract.Stats.CONTENT_URI,
                null,
                null,
                null,
                StatsRecordContract.Stats.COL_STAT_COUNT + " DESC");

        ArrayList<Stat> allStats = new ArrayList<>();

        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                allStats.add(new Stat(i + 1, cursor.getString(1), cursor.getInt(2)));
                cursor.moveToNext();
            }
        }

        if (cursor != null && !cursor.isClosed()) cursor.close();
        return allStats;
    }
}

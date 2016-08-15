package com.intelliviz.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.model.LineSet;
import com.intelliviz.stockhawk.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by edm on 7/21/2016.
 */
public class StockGraphFragment extends Fragment {
    private static final String EXTRA_STOCK_DATA = "data";
    private static final String EXTRA_STOCK_SYMBOL = "symbol";
    private String mData;
    private String mSymbol;

    @Bind(R.id.linechart) com.db.chart.view.LineChartView mLineChart;
    public static StockGraphFragment newInstance(String symbol, String data) {
        StockGraphFragment fragment = new StockGraphFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_STOCK_DATA, data);
        args.putString(EXTRA_STOCK_SYMBOL, symbol);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData = getArguments().getString(EXTRA_STOCK_DATA);
        mSymbol = getArguments().getString(EXTRA_STOCK_SYMBOL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_line_graph, container, false);
        ButterKnife.bind(this, view);

        String[] tokens = mData.split("\n");
        ArrayList<String> labels = new ArrayList<>();

        ArrayList<Float> values = new ArrayList<>();
        for(int i = 1; i < tokens.length; i++) {
            String[] entry = tokens[i].split(",");
            values.add(Float.parseFloat(entry[3]));
            labels.add("1");
        }

        float vals[] = new float[values.size()];
        int i = 0;
        for(Float f : values) {
            vals[i++] = f.floatValue();
        }
        LineSet dataset = new LineSet(labels.toArray(new String[0]), vals);
        dataset.setColor(Color.GREEN);
        dataset.setThickness(4);
        mLineChart.addData(dataset);
        mLineChart.setScaleY(1);
        //mLineChart.setInnerChartBottom(50);
        mLineChart.show();

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mSymbol);
        }
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

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

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.intelliviz.stockhawk.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private ArrayList<String> mDates;

    @Bind(R.id.lineChart) com.github.mikephil.charting.charts.LineChart mLineChart;
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
        //ArrayList<String> labels = new ArrayList<>();
        //ArrayList<Float> values = new ArrayList<>();
        mDates = new ArrayList<>();

        List<Entry> entries = new ArrayList<>();
        for(int i = 1; i < tokens.length; i++) {
            String[] entry = tokens[i].split(",");
            //values.add(Float.parseFloat(entry[4]));
            //labels.add(entry[0]);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = sdf.parse(entry[0]);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int mon = cal.get(Calendar.MONTH)+1;
                float value = Float.parseFloat(entry[4]);
                entries.add(new Entry(i-1, value));
                mDates.add(mon+"/"+day);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        LineDataSet dataSet = new LineDataSet(entries, "label");
        LineData lineData = new LineData(dataSet);
        mLineChart.setData(lineData);
        mLineChart.invalidate();
        //mLineChart.setViewPortOffsets(100,100,100,100);
        mLineChart.setExtraOffsets(5, 20, 5, 0);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setTextSize(20);
        xAxis.setTextColor(Color.GREEN);

        YAxis leftAxis = mLineChart.getAxisLeft();
        YAxis rightAxis = mLineChart.getAxisRight();
        leftAxis.setTextSize(20);
        rightAxis.setTextSize(20);
        leftAxis.setTextColor(Color.GREEN);
        rightAxis.setTextColor(Color.GREEN);


        AxisValueFormatter formatter = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mDates.get((int)value);
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };

        xAxis.setValueFormatter(formatter);



        //LineSet dataset = new LineSet(labels.toArray(new String[0]), vals);
        //dataset.setColor(Color.GREEN);
        //dataset.setThickness(4);
        //mLineChart.addData(dataset);
        //mLineChart.setScaleY(1);
        //mLineChart.setInnerChartBottom(50);
        //mLineChart.show();

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

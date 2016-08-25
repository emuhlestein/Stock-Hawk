package com.intelliviz.stockhawk.ui;

import android.os.AsyncTask;
import android.util.Log;

import com.intelliviz.stockhawk.rest.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by edm on 7/20/2016.
 */
public class StockHistoryAsyncTask extends AsyncTask<String, Void, String> {
    public static final String TAG = StockHistoryAsyncTask.class.getSimpleName();
    private StockListFragment.OnStockSelectListener mListener;
    private String mSymbol;

    public StockHistoryAsyncTask(StockListFragment.OnStockSelectListener listener, String symbol) {
        mListener = listener;
        mSymbol = symbol;
    }

    @Override
    protected String doInBackground(String... params) {
        int len = params.length;
        ArrayList<String> list = null;
        if(len >= 0) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            String endDate = dateFormat.format(cal.getTime());

            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -7);
            String startDate = dateFormat.format(cal.getTime());
            StringBuilder urlStringBuilder = new StringBuilder();
            String query = "select * from yahoo.finance.historicaldata where symbol in (\"YHOO\") and startDate = \'2016-07-11\' and endDate = \'2016-07-19\'";
            try {
                // Base URL for the Yahoo query
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata ", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode("where symbol = ", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode("\"" + mSymbol + "\"", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode("and startDate = \""+startDate+"\" and ", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode("endDate = \""+endDate+"\" ", "UTF-8"));
                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String data = loadDataFromUrl(urlStringBuilder.toString());
            if(data == null) {
                String s = data;
            } else {
                list = Utils.getJsonToHistoricVals(data);
            }

            data = loadDataFromUrl(list.get(1));

            String s = data.toString();
            return s;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if(result != null && mListener != null) {
            mListener.onSelectStock(mSymbol, result);
        }
    }

    private String loadDataFromUrl(String urlString) {

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line+"\n");
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error accessing internet: " + e.toString());
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }

            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream");
                }
            }
        }
        return null;
    }
}

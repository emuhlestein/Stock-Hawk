package com.intelliviz.stockhawk.syncadapter;

import android.accounts.Account;
import android.appwidget.AppWidgetManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.intelliviz.stockhawk.R;
import com.intelliviz.stockhawk.data.StockQuoteContract;
import com.intelliviz.stockhawk.rest.Utils;
import com.intelliviz.stockhawk.widget.WidgetProvider;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by edm on 7/16/2016.
 */
public class StockSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String EXTRA_COMMAND = "command";
    public static final String EXTRA_STOCK = "stock";
    public static final int SYNC_INTERVAL = 60;
    public static final String STOCKS_REFRESHED = "com.intelliviz.stocks.STOCKS_REFRESHED";
    private static final String TAG = StockSyncAdapter.class.getSimpleName();
    private ContentResolver mContentResolver;
    private StringBuilder mStoredSymbols = new StringBuilder();


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN, LOCATION_STATUS_SERVER_INVALID,
            LOCATION_STATUS_UNKNOWN, LOCATION_STATUS_INVALID, LOCATION_STATUS_STOCK_NOT_FOUND})
    public @interface LocationStatus{}
    public static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    public static final int LOCATION_STATUS_UNKNOWN = 3;
    public static final int LOCATION_STATUS_INVALID = 4;
    public static final int LOCATION_STATUS_STOCK_NOT_FOUND = 5;

    public StockSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        mStoredSymbols  = new StringBuilder();
        String command = extras.getString(EXTRA_COMMAND, "update");
        StringBuilder urlStringBuilder = new StringBuilder();
        String urlString;
        String getResponse;
        String data;

        if(command.equals("update")) {
            Cursor cursor = mContentResolver.query(StockQuoteContract.QuotesEntry.CONTENT_URI,
                    new String[]{StockQuoteContract.QuotesEntry.COLUMN_SYMBOL}, null,
                    null, null);
            int count = cursor.getCount();
            Log.d(TAG, "Count: " + count);
            if (cursor == null || cursor.getCount() == 0) {
                // add some default stocks
/*
                try {
                    // Base URL for the Yahoo query
                    urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                    urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                            + "in (", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Init task. Populates DB with quotes for the symbols seen below
                try {
                    urlStringBuilder.append(
                            URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // finalize the URL for the API query.
                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");
*/

                ArrayList<String> stockArray = new ArrayList<>();
                stockArray.add("YHOO");
                stockArray.add("AAPL");
                stockArray.add("GOOG");
                stockArray.add("MSFT");
                urlString = Utils.buildURL(stockArray);

                //urlString = urlStringBuilder.toString();
                data = loadDataFromUrl(urlString);
                if(data != null) {
                    //Utils.addStocksToDB(getContext(), data);
                }
                /*
                if (data != null) {
                    ArrayList stockList = Utils.quoteJsonToContentVals(data);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(StockQuoteContract.QuotesEntry.COLUMN_ISCURRENT, 0);
                    mContentResolver.update(StockQuoteContract.QuotesEntry.CONTENT_URI, contentValues,
                            null, null);

                    try {
                        mContentResolver.applyBatch(StockQuoteContract.CONTENT_AUTHORITY, stockList);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                    }
                }
                */
            } else if (cursor != null) {
                /*
                try{
                    // Base URL for the Yahoo query
                    urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                    urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                            + "in (", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                */

                ArrayList<String> stockArray = new ArrayList<>();
                DatabaseUtils.dumpCursor(cursor);
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    stockArray.add(cursor.getString(cursor.getColumnIndex("symbol")));
                    cursor.moveToNext();
                }
                urlString = Utils.buildURL(stockArray);
                /*
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                try {
                    urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                // finalize the URL for the API query.
                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");
                        */


                //urlString = urlStringBuilder.toString();
                data = loadDataFromUrl(urlString);
                if (data != null) {
                    try {
                        Utils.updateStocks(getContext(), data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        setLocationStatus(getContext(), LOCATION_STATUS_SERVER_INVALID);
                        return;
                    }
                    //ArrayList<ContentProviderOperation> stockList = Utils.quoteJsonToContentVals(data);

                    //ContentValues contentValues = new ContentValues();
                    //contentValues.put(StockQuoteContract.QuotesEntry.COLUMN_ISCURRENT, 0);
                    //mContentResolver.update(StockQuoteContract.QuotesEntry.CONTENT_URI, contentValues,
                    //       null, null);
/*
                    try {
                        mContentResolver.applyBatch(StockQuoteContract.CONTENT_AUTHORITY, stockList);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                    }
*/
                }
            }
            cursor.close();
        } else if(command.equals("add")) {
            String symbol = extras.getString(EXTRA_STOCK, "update");
            Cursor cursor = Utils.getStock(getContext(), symbol);


            if(cursor == null || cursor.getCount() == 0 ) {
                ArrayList<String> stockArray = new ArrayList<>();
                stockArray.add(symbol);
                urlString = Utils.buildURL(stockArray);
                /*
                try {
                    // Base URL for the Yahoo query
                    urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                    urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                            + "in (", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                try {
                    urlStringBuilder.append(URLEncoder.encode("\"" + symbol + "\")", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // finalize the URL for the API query.
                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");
                        */
                //urlString = urlStringBuilder.toString();
                data = loadDataFromUrl(urlString);
                try {
                    String result = Utils.addStocksToDB(getContext(), data);
                    Utils.updateStocks(this.getContext());
                    if(result == null) {
                        // stock not found
                        setLocationStatus(getContext(), LOCATION_STATUS_STOCK_NOT_FOUND);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    setLocationStatus(getContext(), LOCATION_STATUS_SERVER_INVALID);
                    return;
                }

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
                ComponentName appWidget = new ComponentName(getContext(), WidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget);

                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.collectionWidgetListView);
                /*
                try {
                    mContentResolver.applyBatch(StockQuoteContract.CONTENT_AUTHORITY,
                            Utils.quoteJsonToContentVals(data));
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                */
            } else {

                cursor.close();
            }
        }



/*
        if (cursor == null || cursor.getCount() == 0) {
            // Init task. Populates DB with quotes for the symbols seen below
            try {
                urlStringBuilder.append(
                        URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if(cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getCount();
            for (int i = 0; i < cursor.getCount(); i++) {
                mStoredSymbols.append("\"" +
                        cursor.getString(cursor.getColumnIndex("symbol")) + "\",");
                cursor.moveToNext();
            }
            mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
            try {
                urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if(cursor != null) {
            cursor.close();
        }

        String s = "String query = \"select * from yahoo.finance.historicaldata where symbol = \\'\" + msymbol + \"\\' and startDate = \\'2016-07-11\\' and endDate = \\'2016-07-19\\'\";";

        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");


*/
        setLocationStatus(getContext(), LOCATION_STATUS_OK);
    }

    private String loadDataFromUrl(String urlString) {

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(url == null) {
            return null;
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

            if(sb.length() == 0) {
                setLocationStatus(getContext(), LOCATION_STATUS_SERVER_DOWN);
                return null;
            }

            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error accessing internet: " + e.toString());
            setLocationStatus(getContext(), LOCATION_STATUS_SERVER_DOWN);
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

    private static void setLocationStatus(Context context, @LocationStatus int locationStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(context.getString(R.string.pref_location_status_key), locationStatus);
        editor.commit();
    }
}

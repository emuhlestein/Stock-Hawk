package com.intelliviz.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.intelliviz.stockhawk.R;
import com.intelliviz.stockhawk.data.StockQuoteContract;
import com.intelliviz.stockhawk.syncadapter.StockSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static String buildURL(ArrayList<String> symbols) {
        StringBuilder urlStringBuilder = new StringBuilder();
        StringBuilder symbolList = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                    + "in (", "UTF-8"));

            for (String symbol : symbols) {
                symbolList.append("\"" + symbol + "\",");
            }
            symbolList.replace(symbolList.length() - 1, symbolList.length(), ")");
            urlStringBuilder.append(URLEncoder.encode(symbolList.toString(), "UTF-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        return urlStringBuilder.toString();
    }

    public static Cursor getStock(Context context, String symbol) {
        Uri uri = StockQuoteContract.QuotesEntry.CONTENT_URI;
        String selectionClause = StockQuoteContract.QuotesEntry.COLUMN_SYMBOL + " = ?";
        String[] selectionArgs = {symbol};
        String[] projection = null; // get all columns
        Cursor cursor = context.getContentResolver().query(uri, projection, selectionClause, selectionArgs, null);

        return cursor;
    }

    public static String addStock(Context context, String symbol, String bidPrice, String change, int isUp, String percentChange){
        ContentValues values = new ContentValues();
        values.put(StockQuoteContract.QuotesEntry.COLUMN_SYMBOL, symbol);
        values.put(StockQuoteContract.QuotesEntry.COLUMN_BID_PRICE, bidPrice);
        values.put(StockQuoteContract.QuotesEntry.COLUMN_CREATED, "");
        values.put(StockQuoteContract.QuotesEntry.COLUMN_ISCURRENT, 1);
        values.put(StockQuoteContract.QuotesEntry.COLUMN_ISUP, isUp);
        values.put(StockQuoteContract.QuotesEntry.COLUMN_PERCENT_CHANGE, percentChange);
        values.put(StockQuoteContract.QuotesEntry.COLUMN_CHANGE, change);
        Uri uri = context.getContentResolver().insert(StockQuoteContract.QuotesEntry.CONTENT_URI, values);
        return uri.getLastPathSegment();
    }


    public static ArrayList getJsonToHistoricVals(String JSON) {
        ArrayList<String> contentList = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        String content;
        try {
            if (JSON == null) {
                Log.d("EDM", "JSON is null");
            }
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("diagnostics");
                    content = jsonObject.getString("content");
                    contentList.add(content);
                } else {
                    jsonObject = jsonObject.getJSONObject("diagnostics");
                    resultsArray = jsonObject.getJSONArray("url");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            content = jsonObject.getString("content");
                            contentList.add(content);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return contentList;
    }

    public static String addStocksToDB(Context context, String JSON) throws JSONException {
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        String id = null;

        if (JSON == null) {
            return null;
        }
        jsonObject = new JSONObject(JSON);
        if (jsonObject != null && jsonObject.length() != 0) {
            jsonObject = jsonObject.getJSONObject("query");
            int count = Integer.parseInt(jsonObject.getString("count"));
            if (count == 1) {
                jsonObject = jsonObject.getJSONObject("results").getJSONObject("quote");
                id = addStockToDB(context, jsonObject);
            } else {
                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        id = addStockToDB(context, jsonObject);
                    }
                }
            }
            return id;
        }

        return null;
    }

    public static int updateStocks(Context context, String JSON) throws JSONException {
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        int id = -1;

        if (JSON == null) {
            return 0;
        }
        jsonObject = new JSONObject(JSON);
        if (jsonObject != null && jsonObject.length() != 0) {
            jsonObject = jsonObject.getJSONObject("query");
            int count = Integer.parseInt(jsonObject.getString("count"));
            if (count == 1) {
                jsonObject = jsonObject.getJSONObject("results").getJSONObject("quote");
                id = updateStock(context, jsonObject);
            } else {
                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        id = updateStock(context, jsonObject);
                    }
                }
            }
            return id;
        }

        return 0;
    }

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            if (JSON == null) {
                Log.d("EDM", "JSON is null");
            }
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results").getJSONObject("quote");
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        if (bidPrice != null && !bidPrice.equals("null")) {
            bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        } else {
            bidPrice = "0.00";
        }
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(
                StockQuoteContract.QuotesEntry.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(StockQuoteContract.QuotesEntry.COLUMN_SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(StockQuoteContract.QuotesEntry.COLUMN_BID_PRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(StockQuoteContract.QuotesEntry.COLUMN_PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(StockQuoteContract.QuotesEntry.COLUMN_CHANGE, truncateChange(change, false));
            builder.withValue(StockQuoteContract.QuotesEntry.COLUMN_CREATED, 1);
            builder.withValue(StockQuoteContract.QuotesEntry.COLUMN_ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(StockQuoteContract.QuotesEntry.COLUMN_ISUP, 0);
            } else {
                builder.withValue(StockQuoteContract.QuotesEntry.COLUMN_ISUP, 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public static String addStockToDB(Context context, JSONObject jsonObject) {
        try {
            String change = jsonObject.getString("Change");
            int errCode = 0;
            if(jsonObject.has("cod")) {
                errCode = jsonObject.getInt("cod");
            }
            if(change.equals("null")) {
                return null;
            }
            String symbol = jsonObject.getString("symbol");
            String bidPrice = truncateBidPrice(jsonObject.getString("Bid"));
            String percentChange = truncateChange(jsonObject.getString("ChangeinPercent"), true);
            String chg = truncateChange(change, false);
            int isUp;
            if (chg.charAt(0) == '-') {
                isUp = 0;
            } else {
                isUp = 1;
            }
            String id = addStock(context, symbol, bidPrice, chg,  isUp, percentChange);
            return id;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int updateStock(Context context, JSONObject jsonObject) {
        try {
            String change = jsonObject.getString("Change");
            String symbol = jsonObject.getString("symbol");
            String bidPrice = truncateBidPrice(jsonObject.getString("Bid"));
            String percentChange = truncateChange(jsonObject.getString("ChangeinPercent"), true);
            String chg = truncateChange(change, false);
            int isUp;
            if (chg.charAt(0) == '-') {
                isUp = 0;
            } else {
                isUp = 1;
            }
            int id = updateStock(context, symbol, bidPrice, chg,  isUp, percentChange);
            return id;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int updateStock(Context context, String symbol, String bidPrice, String change, int isUp, String percentChange){
        ContentValues values = new ContentValues();
        values.put(StockQuoteContract.QuotesEntry.COLUMN_SYMBOL, symbol);
        values.put(StockQuoteContract.QuotesEntry.COLUMN_BID_PRICE, bidPrice);
        values.put(StockQuoteContract.QuotesEntry.COLUMN_CREATED, "");
        values.put(StockQuoteContract.QuotesEntry.COLUMN_ISCURRENT, 1);
        values.put(StockQuoteContract.QuotesEntry.COLUMN_ISUP, isUp);
        values.put(StockQuoteContract.QuotesEntry.COLUMN_PERCENT_CHANGE, percentChange);
        values.put(StockQuoteContract.QuotesEntry.COLUMN_CHANGE, change);
        Uri uri = StockQuoteContract.QuotesEntry.CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + symbol);
        int rc = context.getContentResolver().update(uri, values, null, null);
        return rc;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @SuppressWarnings("ResourceType")
    public static @StockSyncAdapter.LocationStatus
    int getLoctionStatus(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.pref_location_status_key), StockSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }
}

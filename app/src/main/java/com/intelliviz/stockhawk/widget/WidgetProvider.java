package com.intelliviz.stockhawk.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.intelliviz.stockhawk.R;
import com.intelliviz.stockhawk.data.StockQuoteContract;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by edm on 6/13/2016.
 */
public class WidgetProvider extends AppWidgetProvider {
    private StringBuilder mStoredSymbols = new StringBuilder();

    public void updateStockQuotes(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for(int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, StockRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.stock_collection_widget_layout);

            views.setRemoteAdapter(R.id.collectionWidgetListView, intent);

            //Intent templateIntent = new Intent(context, )
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    public void updateStockQuotesOld(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Cursor initQueryCursor;
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                    + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        initQueryCursor = context.getContentResolver().query(StockQuoteContract.QuotesEntry.CONTENT_URI,
                new String[]{"Distinct " + StockQuoteContract.QuotesEntry.COLUMN_SYMBOL, StockQuoteContract.QuotesEntry.COLUMN_BID_PRICE}, null,
                null, null);
        if (initQueryCursor != null) {
            DatabaseUtils.dumpCursor(initQueryCursor);
            int count = initQueryCursor.getCount();
            initQueryCursor.moveToFirst();
            for (int i = 0; i < initQueryCursor.getCount(); i++) {
                mStoredSymbols.append("\"" +
                        initQueryCursor.getString(initQueryCursor.getColumnIndex(StockQuoteContract.QuotesEntry.COLUMN_SYMBOL)) + "\",");
                mStoredSymbols.append("\"" +
                        initQueryCursor.getString(initQueryCursor.getColumnIndex(StockQuoteContract.QuotesEntry.COLUMN_BID_PRICE)) + "\",");
                initQueryCursor.moveToNext();
            }
            mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
            try {
                urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        initQueryCursor.close();

        final int N = appWidgetIds.length;
        for(int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_provider_layout);
            views.setTextViewText(R.id.stock_text, mStoredSymbols.toString());
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    public void updateStockQuotes(Context context) {
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        updateStockQuotes(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        updateStockQuotes(context, appWidgetManager, appWidgetIds);
    }


    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, WidgetService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private static void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_list,
                new Intent(context, WidgetService.class));
    }
}

package com.intelliviz.stockhawk.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.intelliviz.stockhawk.R;;
import com.intelliviz.stockhawk.data.StockQuoteContract;

/**
 * Created by edm on 7/13/2016.
 */
public class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private ContentResolver mCr;
    private Cursor mCursor;

    public StockRemoteViewsFactory(Context context) {
        mContext = context;
        mCr = context.getContentResolver();
    }


    @Override
    public void onCreate() {
        mCursor = executeQuery();
    }

    @Override
    public void onDataSetChanged() {
        mCursor = executeQuery();
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if(mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        mCursor.moveToPosition(position);

        int symbolIndex = mCursor.getColumnIndex(StockQuoteContract.QuotesEntry.COLUMN_SYMBOL);
        int bidIndex = mCursor.getColumnIndex(StockQuoteContract.QuotesEntry.COLUMN_BID_PRICE);

        String symbol = mCursor.getString(symbolIndex);
        String bid = mCursor.getString(bidIndex);

        RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                R.layout.stock_collection_item_layout);
        rv.setTextViewText(R.id.stockNameTextView, symbol);
        rv.setTextViewText(R.id.stockQuoteTextView, bid);

        Intent fillInIntent = new Intent();
        //fillInIntent.setData();
        //rv.setOnClickFillInIntent();
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if(mCursor != null) {
            return mCursor.getInt(mCursor.getColumnIndex(StockQuoteContract.QuotesEntry._ID));
        } else {
            return position;
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private Cursor executeQuery() {
        String[] projection = new String[] {
                StockQuoteContract.QuotesEntry._ID,
                StockQuoteContract.QuotesEntry.COLUMN_SYMBOL,
                StockQuoteContract.QuotesEntry.COLUMN_BID_PRICE
        };

        Cursor cursor = mContext.getContentResolver().query(StockQuoteContract.QuotesEntry.CONTENT_URI,
                projection, null, null, null);

        return cursor;
    }
}

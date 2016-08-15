package com.intelliviz.stockhawk.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by edm on 7/26/2016.
 */
public class StockQuoteContract {
    public static final String CONTENT_AUTHORITY = "com.intelliviz.stockhawk.data.StockQuoteProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_QUOTES = "quotes";

    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "query.yahooapis.com";
    // The account name
    public static final String ACCOUNT = "stockaccount";

    public static final class QuotesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUOTES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUOTES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUOTES;

        public static final String TABLE_NAME = PATH_QUOTES;
        public static final String COLUMN_SYMBOL = "symbol";
        public static final String COLUMN_PERCENT_CHANGE = "percent_change";
        public static final String COLUMN_CHANGE = "change";
        public static final String COLUMN_BID_PRICE = "bid_price";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_ISUP = "is_up";
        public static final String COLUMN_ISCURRENT = "is_current";
    }
}

package com.intelliviz.stockhawk.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by edm on 7/26/2016.
 */
public class StockQuoteProvider extends ContentProvider {
    private static final String TAG = StockQuoteProvider.class.getSimpleName();
    private SqliteHelper mSqliteHelper;
    private static final String DBASE_NAME = "quotes";
    private static final int DBASE_VERSION = 3;
    private static final int QUOTES_LIST = 101;
    private static final int QUOTES_ID = 102;
    private static final int STATUS_ID = 201;

    private static UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher((UriMatcher.NO_MATCH));

        // all movies
        sUriMatcher.addURI(StockQuoteContract.CONTENT_AUTHORITY, StockQuoteContract.PATH_QUOTES, QUOTES_LIST);

        // a particular movie
        sUriMatcher.addURI(StockQuoteContract.CONTENT_AUTHORITY, StockQuoteContract.PATH_QUOTES + "/*", QUOTES_ID);

        // status
        sUriMatcher.addURI(StockQuoteContract.CONTENT_AUTHORITY, StockQuoteContract.PATH_STATUS, STATUS_ID);
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        final SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                ContentProviderOperation operation = operations.get(i);
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase sqlDB = mSqliteHelper.getWritableDatabase();
        String[] projection = {StockQuoteContract.QuotesEntry.COLUMN_SYMBOL};
        sqlDB.beginTransaction();
        try {
            for (ContentValues cv : values) {
                String symbol = cv.getAsString(StockQuoteContract.QuotesEntry.COLUMN_SYMBOL);
                SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
                sqLiteQueryBuilder.setTables(StockQuoteContract.QuotesEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(StockQuoteContract.QuotesEntry.COLUMN_SYMBOL +
                        "=" + symbol);
                Cursor cursor = sqLiteQueryBuilder.query(sqlDB, projection, null, null, null, null, null);
                if(cursor.getCount() > 0) {
                    continue;
                }
                long newID = sqlDB.insertOrThrow(StockQuoteContract.QuotesEntry.TABLE_NAME, null, cv);
            }
            sqlDB.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(uri, null);
        } finally {
            sqlDB.endTransaction();
        }
        return values.length;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mSqliteHelper = new SqliteHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        switch(sUriMatcher.match(uri)) {
            case QUOTES_LIST:
                // get all quotes: "quotes/"
                sqLiteQueryBuilder.setTables(StockQuoteContract.QuotesEntry.TABLE_NAME);
                break;
            case QUOTES_ID:
                // get a particular quote: "quote/#"
                sqLiteQueryBuilder.setTables(StockQuoteContract.QuotesEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(StockQuoteContract.QuotesEntry.COLUMN_SYMBOL +
                        "=" + uri.getLastPathSegment());
                break;
            case STATUS_ID:
                // get a particular quote: "status/#"
                sqLiteQueryBuilder.setTables(StockQuoteContract.StatusEntry.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri");
        }

        SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        Cursor cursor = sqLiteQueryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)) {
            case QUOTES_LIST:
                return StockQuoteContract.QuotesEntry.CONTENT_TYPE;
            case QUOTES_ID:
                return StockQuoteContract.QuotesEntry.CONTENT_ITEM_TYPE;
            case STATUS_ID:
                return StockQuoteContract.StatusEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri");
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId;
        SQLiteDatabase db;
        Uri returnUri;

        db = mSqliteHelper.getWritableDatabase();

        switch(sUriMatcher.match(uri)) {
            case QUOTES_LIST:
                // The second parameter will allow an empty row to be inserted. If it was null, then no row
                // can be inserted if values is empty.
                String symbol = values.getAsString(StockQuoteContract.QuotesEntry.COLUMN_SYMBOL);
                Log.d(TAG, "Insert " + symbol + " into table");
                rowId = db.insert(StockQuoteContract.QuotesEntry.TABLE_NAME, null, values);
                if (rowId > -1) {
                    returnUri = ContentUris.withAppendedId(uri, rowId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri.toString());
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        int rowsDeleted = 0;
        String symbol;

        switch(sUriMatcher.match(uri)) {
            case QUOTES_LIST:
                rowsDeleted = db.delete(StockQuoteContract.QuotesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case QUOTES_ID:
                symbol = uri.getLastPathSegment();
                String[] whereArgs = new String[]{symbol};
                rowsDeleted = db.delete(StockQuoteContract.QuotesEntry.TABLE_NAME,
                        StockQuoteContract.QuotesEntry.COLUMN_SYMBOL + "=?", whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri");
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        String symbol;

        switch(sUriMatcher.match(uri)) {
            case QUOTES_ID:
                symbol = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(StockQuoteContract.QuotesEntry.TABLE_NAME,
                            values,
                            StockQuoteContract.QuotesEntry.COLUMN_SYMBOL + "=?",
                            new String[]{symbol});
                } else {
                    rowsUpdated = db.update(StockQuoteContract.QuotesEntry.TABLE_NAME,
                            values,
                            StockQuoteContract.QuotesEntry.COLUMN_SYMBOL + "=" + symbol
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            case QUOTES_LIST:
                rowsUpdated = db.update(StockQuoteContract.QuotesEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case STATUS_ID:
                rowsUpdated = db.update(StockQuoteContract.StatusEntry.TABLE_NAME,
                        values, null, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private static class SqliteHelper extends SQLiteOpenHelper {

        public SqliteHelper(Context context) {
            super(context, DBASE_NAME, null, DBASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create the movie table
            String sql = "CREATE TABLE " + StockQuoteContract.QuotesEntry.TABLE_NAME +
                    " ( " + StockQuoteContract.QuotesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    StockQuoteContract.QuotesEntry.COLUMN_SYMBOL + " TEXT NOT NULL, " +
                    StockQuoteContract.QuotesEntry.COLUMN_PERCENT_CHANGE + " TEXT NOT NULL, " +
                    StockQuoteContract.QuotesEntry.COLUMN_CHANGE + " TEXT NOT NULL, " +
                    StockQuoteContract.QuotesEntry.COLUMN_BID_PRICE + " TEXT NOT NULL, " +
                    StockQuoteContract.QuotesEntry.COLUMN_CREATED + " TEXT NOT NULL, " +
                    StockQuoteContract.QuotesEntry.COLUMN_ISUP + " INTEGER NOT NULL, " +
                    StockQuoteContract.QuotesEntry.COLUMN_ISCURRENT + " INTEGER NOT NULL);";


            db.execSQL(sql);

            // create the state table
            sql = "CREATE TABLE " + StockQuoteContract.StatusEntry.TABLE_NAME +
                    " ( " + StockQuoteContract.StatusEntry._ID + " INTEGER NOT NULL, " +
                    StockQuoteContract.StatusEntry.COLUMN_STATUS + " INTEGER NOT NULL);";

            db.execSQL(sql);

            String ROW = "INSERT INTO " + StockQuoteContract.StatusEntry.TABLE_NAME + " Values ('0', '0');";
            db.execSQL(ROW);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + StockQuoteContract.QuotesEntry.TABLE_NAME);
            onCreate(db);
        }
    }
}

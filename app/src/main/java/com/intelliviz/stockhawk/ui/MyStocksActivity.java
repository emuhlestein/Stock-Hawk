package com.intelliviz.stockhawk.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.intelliviz.stockhawk.R;
import com.intelliviz.stockhawk.data.StockQuoteContract;
import com.intelliviz.stockhawk.rest.Utils;
import com.intelliviz.stockhawk.widget.WidgetProvider;


public class MyStocksActivity extends AppCompatActivity implements StockListFragment.OnStockSelectListener {
    private static final String LIST_FRAG_TAG = "list frag tag";
    private static final String STOCK_FRAG_TAG = "stock frag tag";
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stocks);


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName appWidget = new ComponentName(this, WidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.collectionWidgetListView);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment;

        fragment = fm.findFragmentByTag(LIST_FRAG_TAG);
        if (fragment == null) {
            fragment = StockListFragment.newInstance();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_holder, fragment, LIST_FRAG_TAG);
            ft.commit();
        }

        mTitle = getTitle();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Cursor cursor = getContentResolver().query(StockQuoteContract.QuotesEntry.CONTENT_URI, null, null, null, null);
                DatabaseUtils.dumpCursor(cursor);
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;

            case R.id.action_change_units:
                // this is for changing stock changes from percent value to dollar value
                Utils.showPercent = !Utils.showPercent;
                getContentResolver().notifyChange(StockQuoteContract.QuotesEntry.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectStock(String symbol, String data) {
        // parse data
        String[] tokens = data.split("\n");
        for(String token : tokens) {
            token.split(",");

        }

        FragmentManager fm = getSupportFragmentManager();
        StockGraphFragment fragment;


        fragment = StockGraphFragment.newInstance(symbol, data);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_holder, fragment);
        ft.addToBackStack(null);
        ft.commit();

        // launch fragment to show data in graph
    }
}

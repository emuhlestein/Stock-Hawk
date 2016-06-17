package com.intelliviz.stockhawk.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.intelliviz.stockhawk.R;
import com.intelliviz.stockhawk.data.QuoteProvider;
import com.intelliviz.stockhawk.rest.Utils;


public class MyStocksActivity extends AppCompatActivity {
    private static final String LIST_FRAG_TAG = "list frag tag";
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stocks);

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
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }
}

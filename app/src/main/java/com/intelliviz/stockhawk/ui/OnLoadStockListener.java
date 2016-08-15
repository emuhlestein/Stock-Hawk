package com.intelliviz.stockhawk.ui;

import android.database.Cursor;

/**
 * Created by edm on 7/18/2016.
 */
public interface OnLoadStockListener {
    void onLoadStock(Cursor cursor);
}

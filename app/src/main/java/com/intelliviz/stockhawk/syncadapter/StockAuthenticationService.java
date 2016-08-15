package com.intelliviz.stockhawk.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by edm on 7/16/2016.
 */
public class StockAuthenticationService extends Service{
    private StockAccountAuthenticator mStockAccountAuthenticator;

    @Override
    public void onCreate() {
        mStockAccountAuthenticator = new StockAccountAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mStockAccountAuthenticator.getIBinder();
    }
}

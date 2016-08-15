package com.intelliviz.stockhawk.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by edm on 7/16/2016.
 */
public class StockSyncService extends Service {
    private static StockSyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if(sSyncAdapter == null) {
                sSyncAdapter = new StockSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        /**
         * Get the object that allows external processes to call onPerformSync().
         * The object is created in the base class code when the SyncAdatper
         * constructors call super()
         */
        return sSyncAdapter.getSyncAdapterBinder();
    }
}

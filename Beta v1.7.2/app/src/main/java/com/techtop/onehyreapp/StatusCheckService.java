package com.techtop.onehyreapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;


public class StatusCheckService extends Service {

    String status = "offline";
    String test;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // background processing here
        if (status.equals("offline"))
        {
            Intent activityIntent = new Intent(this, SystemOfflineActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Log.d(test, "dataRetrievalTest " + test);
        }

        // Return START_STICKY to keep the service running in the background
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

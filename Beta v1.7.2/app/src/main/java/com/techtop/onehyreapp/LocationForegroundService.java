package com.techtop.onehyreapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class LocationForegroundService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "LocationServiceChannel";

    private LocationHelper locationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        locationHelper = new LocationHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);

        locationHelper.startLocationUpdates();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationHelper.stopLocationUpdates();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return null because this is not a bound service
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Getting real-time location updates")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }
}

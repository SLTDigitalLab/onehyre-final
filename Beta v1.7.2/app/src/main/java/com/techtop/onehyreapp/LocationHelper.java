package com.techtop.onehyreapp;

import static android.content.Context.MODE_PRIVATE;
import static com.techtop.onehyreapp.MainActivity.SHARED_PREFS;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class LocationHelper {

    private static final long MIN_TIME_INTERVAL = 1000; // Minimum time interval between location updates in milliseconds
    private static final float MIN_DISTANCE_INTERVAL = 10; // Minimum distance interval between location updates in meters

    private Context context;
    private LocationManager locationManager;

    public LocationHelper(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_INTERVAL,
                    MIN_DISTANCE_INTERVAL,
                    locationListener
            );
        } catch (SecurityException e) {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopLocationUpdates() {
        locationManager.removeUpdates(locationListener);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Handle the updated location here
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            //Toast.makeText(context, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();

            String liveLocation = longitude + ", " + latitude;

            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            if (sharedPreferences.contains("tel")) {
                String driverPhoneSP = sharedPreferences.getString("tel", "");
                new bgLocationUploader().execute(driverPhoneSP, liveLocation);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Called when the GPS provider is enabled
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Called when the GPS provider is disabled
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Called when the GPS provider status changes
        }
    };
}

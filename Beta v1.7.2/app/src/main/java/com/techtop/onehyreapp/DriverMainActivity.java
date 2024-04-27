package com.techtop.onehyreapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class DriverMainActivity extends AppCompatActivity {
    ToggleButton btnForceOffline;
    private long backPressedTime;
    String currentStatus;

    private TextView textViewLatitude, textViewLongitude, textViewspeed, textViewDistance, textViewFee;
    private LocationManager locationManager;
    Button btn_start, btn_stop;

    public static final String SHARED_PREFS = "sharedPrefs";

    private static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 1;
    ImageView imageViewWait_start, imageViewWait_pause, ImageViewTrip_details, ImageViewHireHisory;
    long start_time, end_time, start_short_time, end_short_time, wait_time_start, wait_time_end, wait_difference;
    long wait_tot;
    long time_difference;

    String end_time_send;
    String start_time_send;

    float starting_fee = 0;
    float two_km_fee = 0;
    float wait_min_fee = 0;

    //stop watch
    private long pauseOffset;
    private boolean running;

    private long p_pauseOffset;
    private boolean p_running;

    DateFormat dateFormat = new SimpleDateFormat("hh.mm aa");

    Chronometer chorono_wait_time, chrono_hire_time;


    int count = 1;
    double start_latitude = 0.0;
    double start_longitude = 0.0;

    double end_latitude = 0.0;
    double end_longitude = 0.0;
    double cont_latitude = 0.0, end_lat_address = 0.0;
    double cont_longitude = 0.0, end_lon_address = 0.0;
    double tot_distance;
    double dis;

    double wait_time;
    double fee;

    float speed = 0;
    float speed_first = 0;
    double main_lat = 0.0, main_lon = 0.0;
    float getSpeed;

    private static final DecimalFormat df = new DecimalFormat("0.00");

    String start_point_address = null;
    String end_point_address = null;

    //json objects
    String jsonString;
    JSONArray coordinatesArray = new JSONArray();
    String arrayToString;
    String driverPhoneNumber, userStatus = "Unknown", checkLanguage = "err";


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        systemStatusCheck();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);
        //getSupportActionBar().hide(); //hide the title bar

        if (hasLocationPermission())
        {
            Intent serviceIntent = new Intent(DriverMainActivity.this, LocationForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
        else
        {
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
            {
                showCustomAlert("Location Permission", "This app needs Location permissions to run correctly", "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                }, "Cancel", null);
            }
            else
            {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("tel")) {
            driverPhoneNumber = sharedPreferences.getString("tel", "");

            /*

            //Following part is only for checking if the selected language saves to shared preferences or not

            checkLanguage = sharedPreferences.getString("lng", "");
            Toast.makeText(this, "" + checkLanguage, Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("lng");
            editor.apply();
            Toast.makeText(getApplicationContext(), "Language data deleted.", Toast.LENGTH_SHORT).show();

             */
        }
        userStatus = "Online";
        updateUserStatus(driverPhoneNumber,userStatus);

        textViewspeed = (TextView) findViewById(R.id.speedtxt);
        textViewDistance = (TextView) findViewById(R.id.distancetxt);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        btn_start = (Button) findViewById(R.id.btn_update);
        textViewFee = (TextView) findViewById(R.id.feetxt);
        btn_stop = (Button) findViewById(R.id.stopbtn);
        btnForceOffline = findViewById(R.id.btnToggleForceOffline);

        chrono_hire_time = (Chronometer) findViewById(R.id.chrono_hire_time);
        chorono_wait_time = (Chronometer) findViewById(R.id.chrono_wait_time);

        imageViewWait_start = (ImageView) findViewById(R.id.p_start);
        imageViewWait_pause = (ImageView) findViewById(R.id.p_pause);
        ImageViewTrip_details = (ImageView) findViewById(R.id.trip_details);
        ImageViewHireHisory = (ImageView) findViewById(R.id.tripHistory);

        //Hire History
        ImageViewHireHisory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverMainActivity.this, TripDetailActivity.class);
                startActivity(intent);
            }
        });


        btnForceOffline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (btnForceOffline.isChecked()){
                    Toast.makeText(DriverMainActivity.this, "Force offline is on - You won't be appeared as online anymore", Toast.LENGTH_SHORT).show();

                    btnForceOffline.setBackgroundResource(R.color.cardinal_red);
                    userStatus = "Offline";
                    updateUserStatus(driverPhoneNumber,userStatus);
                }
                else {
                    Toast.makeText(DriverMainActivity.this, "Force offline is off - You will appear as online again", Toast.LENGTH_SHORT).show();
                    userStatus = "Online";
                    updateUserStatus(driverPhoneNumber,userStatus);

                    btnForceOffline.setBackgroundResource(R.color.onlineBtnColor);
                }
            }
        });

        //bgLocationUploader
        /*SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("tel")) {
            String driverPhoneSP = sharedPreferences.getString("tel", "");
            new bgLocationUploader().execute(driverPhoneSP, String.valueOf("currLocation"));
        }*/

        //trip details
        ImageViewTrip_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DriverMainActivity.this, AmountActivity.class));
            }
        });


        //wait start action
        imageViewWait_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!p_running && running) {
                    chorono_wait_time.setBase(SystemClock.elapsedRealtime() - p_pauseOffset);
                    chorono_wait_time.start();
                    p_running = true;
                    imageViewWait_start.setVisibility(View.GONE);
                    wait_time_start = (new Date().getTime()) / 1000;
                } else {
                    Toast.makeText(DriverMainActivity.this, "You can't use wait before you starts the trip 👨‍🔧", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //wait pause action
        imageViewWait_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (p_running) {
                    wait_time_end = (new Date().getTime()) / 1000;
                    wait_difference = (wait_time_end - wait_time_start) / 60;

                    wait_tot = wait_tot + wait_difference;
                    chorono_wait_time.stop();
                    p_pauseOffset = SystemClock.elapsedRealtime() - chorono_wait_time.getBase();
                    p_running = false;
                    imageViewWait_start.setVisibility(View.VISIBLE);
                }
            }
        });


        //action of the stop button
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View view) {

                tripRunDataUploader("0.0", "0");


                Log.d("endPointCoordinates: ", "lon :" + String.valueOf(cont_longitude + " lat: " + cont_longitude));
                if (cont_longitude != 0) {
                    getEndPointAddress(cont_latitude, cont_longitude);
                }

                //chrono stop
                chrono_hire_time.setBase(SystemClock.elapsedRealtime());
                pauseOffset = 0;

                end_time = (new Date().getTime()) / 1000;

                time_difference = (end_time - start_time);
                //Log.d(String.valueOf(end_time), "endTimeTotalFareTime ");
                //Log.d(String.valueOf(start_time), "startTimeTotalFareTime ");
                //Log.d(String.valueOf(time_difference), "totalFareTimeDiffInMins ");

                end_time_send = String.valueOf(dateFormat.format(new Date()));

                DateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
                String dateString2 = dateFormat2.format(new Date()).toString();

                Intent intent_send = new Intent(getBaseContext(), BillActivity.class);
                intent_send.putExtra("distance", String.valueOf(df.format(tot_distance / 1000)) + "km");
                intent_send.putExtra("hire_time", String.valueOf(time_difference) + "min");
                intent_send.putExtra("kmone_fee", String.valueOf(starting_fee) + "/=");
                intent_send.putExtra("kmtwo_fee", String.valueOf(two_km_fee) + "/=");
                intent_send.putExtra("wait_fee", String.valueOf(wait_min_fee) + "/=");
                intent_send.putExtra("started_time", start_time_send);
                intent_send.putExtra("end_time", end_time_send);
                intent_send.putExtra("date", dateString2);
                intent_send.putExtra("total_wait_time", String.valueOf(wait_tot) + "min");
                intent_send.putExtra("total_fare", "Rs." + String.valueOf(Math.ceil(fee)) + "/=");
                intent_send.putExtra("start_point_address", start_point_address);
                intent_send.putExtra("end_point_address", end_point_address);
                intent_send.putExtra("coordinates_lat_lon", arrayToString);

                //restart intent to fix the bug of refreshing
                Intent intent = getIntent();
                startActivity(intent);
                //finish();

                //*******************************************

                startActivity(intent_send);
                //startActivity(new Intent(DriverMainActivity.this, BillActivity.class));
                Toast.makeText(DriverMainActivity.this, "Trip ended!✋", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        //action of the start button
        btn_start.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                try {
                    if (hasLocationPermission()) {
                        tripRun();

                        userStatus = "Busy";
                        updateUserStatus(driverPhoneNumber, userStatus);
                        btn_start.setVisibility(View.GONE);
                        Toast.makeText(DriverMainActivity.this, "Trip Started!\uD83C\uDFC1", Toast.LENGTH_SHORT).show();
                    } else {

                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showCustomAlert("Location Permission", "This app needs Location permissions to run correctly", "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                                }
                            }, "Cancel", null);
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(DriverMainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void tripRun() {

        if (!running) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            chrono_hire_time.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chrono_hire_time.start();
            running = true;
        }

        if (ActivityCompat.checkSelfPermission(DriverMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DriverMainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, new LocationListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onLocationChanged(@NonNull Location location) {


                if (count == 1) {

                    start_latitude = location.getLatitude();
                    start_longitude = location.getLongitude();
                    end_latitude = location.getLatitude();
                    end_longitude = location.getLongitude();

                    cont_latitude = start_latitude;
                    cont_longitude = start_longitude;

                    //getting start point address
                    getStartPointAddress(start_latitude, start_longitude);

                    //jsonArray
                    try {
                        getRouteCoordinates(cont_longitude, cont_latitude);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    start_time = (new Date().getTime()) / 1000;
                    start_short_time = (new Date().getTime());
                    start_time_send = String.valueOf(dateFormat.format(new Date()));

                } else if (count % 2 == 0) {
                    end_latitude = location.getLatitude();
                    end_longitude = location.getLongitude();

                    cont_latitude = end_latitude;
                    cont_longitude = end_longitude;

                    //jsonArray
                    try {
                        getRouteCoordinates(cont_longitude, cont_latitude);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    end_short_time = (new Date().getTime());

                } else {
                    start_latitude = location.getLatitude();
                    start_longitude = location.getLongitude();

                    cont_latitude = start_latitude;
                    cont_longitude = start_longitude;

                    //jsonArray
                    try {
                        getRouteCoordinates(cont_longitude, cont_latitude);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    start_short_time = (new Date().getTime());
                }

                if (count != 1) {


                    speed_first = (Float.parseFloat(String.valueOf(location.getSpeed()))); //ms-1

                    speed = (speed_first * Float.parseFloat(String.valueOf(3.6)));// ms-1 to kmh-1

                    long timeDiff = ((Math.abs(end_short_time - start_short_time)));

                    float timeHrs = ((Float.parseFloat(String.valueOf(Math.abs(end_short_time - start_short_time)))) / 3600);
                    Log.d("timeDividedBy3600", String.valueOf(timeHrs));
                    dis = timeHrs * speed;
                    tot_distance = tot_distance + dis;

                    tripRunDataUploader(String.valueOf(speed), String.valueOf(df.format(tot_distance / 1000)));
                }

                if (String.valueOf(Math.ceil(speed)) != "Infinity") {
                    textViewspeed.setText(String.valueOf(df.format(Double.parseDouble(String.valueOf(speed)))));
                }

                textViewDistance.setText(String.valueOf(df.format(tot_distance / 1000)));

                fee = feeCal(Double.parseDouble(String.valueOf(df.format(tot_distance / 1000))), wait_tot);

                textViewFee.setText("Rs." + String.valueOf(fee));

                count++;
            }
        });
    }

    @SuppressLint("LongLogTag")
    private void getRouteCoordinates(double lon, double lat) throws JSONException {

        List<LatLng> route = new ArrayList<>();
        route.add(new LatLng(lon, lat));

        for (LatLng point : route) {
            JSONArray pointArray = new JSONArray();
            pointArray.put(point.latitude);
            pointArray.put(point.longitude);
            coordinatesArray.put(pointArray);
        }
        arrayToString = coordinatesArray.toString();
    }


    public double feeCal(double distance, double wait_min) {

        double tot_fee = 0;

        Formatter format = new Formatter();

        Calendar gfg_calender = Calendar.getInstance();

        format = new Formatter();
        format.format("%tH", gfg_calender, gfg_calender);

        System.out.println(format);
        int time = Integer.parseInt(String.valueOf(format));

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        if ((time > 6 && time <= 20)) {
            //morning 6pm-8.59pm

            //take different 6 shared preference values night and day times

            starting_fee = Float.parseFloat(sharedPreferences.getString("day_sFee", ""));
            two_km_fee = Float.parseFloat(sharedPreferences.getString("day_twokmFee", ""));
            wait_min_fee = Float.parseFloat(sharedPreferences.getString("day_waitFee", ""));
        } else {
            //night 9.00pm-5.59am

            starting_fee = Float.parseFloat(sharedPreferences.getString("night_sFee", ""));
            two_km_fee = Float.parseFloat(sharedPreferences.getString("night_twokmFee", ""));
            wait_min_fee = Float.parseFloat(sharedPreferences.getString("night_waitFee", ""));
        }

        if (distance <= 1) {
            tot_fee = starting_fee + (wait_min_fee * wait_min); //tot_fee = starting_fee * distance + (wait_min_fee * wait_min);
        } else {
            tot_fee = (starting_fee * 1) + (two_km_fee * (distance - 1)) + (wait_min_fee * wait_min);
        }
        return tot_fee;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem premiumMenuItem = menu.findItem(R.id.premium_button);
        if (BuildVariant.premium == false) {
            // Free version show "Activate Premium" Button
            premiumMenuItem.setVisible(true);
        } else {
            // Pro version hide "Activate Premium" Button
            premiumMenuItem.setVisible(false);
        }
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.hire_history_itm:
                Toast.makeText(this, "Hire history", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DriverMainActivity.this, TripDetailActivity.class));
                break;
            case R.id.driver_details_itm:
                startActivity(new Intent(DriverMainActivity.this, DriverProfileActivity.class));
                Toast.makeText(this, "Driver Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.change_hire_details_itm:
                startActivity(new Intent(DriverMainActivity.this, AmountActivity.class));
                Toast.makeText(this, "Change hire rates", Toast.LENGTH_SHORT).show();
                break;
            case R.id.driver_logout:
                startActivity(new Intent(DriverMainActivity.this, LogOutActivity.class));
                Toast.makeText(this, "Go to Logout", Toast.LENGTH_SHORT).show();
                break;
            case R.id.premium_button:
                startActivity(new Intent(DriverMainActivity.this, ActivatePremiumActivity.class));
                break;
            case R.id.settings_button:
                startActivity(new Intent(DriverMainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_support:

                AlertDialog.Builder builder = new AlertDialog.Builder(DriverMainActivity.this);
                builder.setMessage("This may cause charges on your mobile account. Are you sure you want to call the Technical Support?");
                builder.setTitle("SLT MY TAXI Meter is about to make an outgoing voice call.");
                Drawable icon = getResources().getDrawable(R.drawable.ic_cause_charges64);
                builder.setIcon(icon);
                builder.setCancelable(false);

                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        callTechnicalSupport();
                    } else {
                        if(shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE))
                        {
                            showCustomAlert("Phone Permission", "This app needs Call permission to perform this action.", "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
                                }
                            }, "Cancel", null);
                        }
                        else
                        {
                            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
                        }
                    }
                });
                builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {

                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                break;
        }
        return true;
    }

    private ActivityResultLauncher<String> requestCallPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @SuppressLint("NewApi")
        @Override
        public void onActivityResult(Boolean isGranted) {

            if (isGranted){
                Log.d("isGranted", "onActivityResult: granted");
            }
            else
            {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE))
                {
                    showCustomAlert("Call Permission", "This app needs 'Phone' permission to perform this action. Please go to settings to manually grant this permission.", "Go to Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" +BuildConfig.APPLICATION_ID));
                            startActivity(intent);
                        }
                    }, "cancel", null);
                }
                Log.d("isGranted", "onActivityResult: not granted");
            }
        }
    });

    private void callTechnicalSupport() {
        String phoneNumber = "0762082709";
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        }
    }

    public void getStartPointAddress(double spLat, double spLon) {
        Geocoder geocoder = new Geocoder(DriverMainActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(spLat, spLon, 1);
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this, "coordinates error: "+e, Toast.LENGTH_SHORT).show();
        }catch (IOException e) {
            Toast.makeText(this, "geocoder error: "+e, Toast.LENGTH_SHORT).show();
        }
        start_point_address = addresses.get(0).getAddressLine(0);
    }

    public void getEndPointAddress(double epLat, double epLon) {
        Geocoder geocoderEnd = new Geocoder(DriverMainActivity.this, Locale.getDefault());
        List<Address> endAddresses = null;

        try {
            endAddresses = geocoderEnd.getFromLocation(epLat, epLon, 1);
        } catch (Exception e) {
            Log.e(String.valueOf(e), "getEndPointAddressException: ");
        }
        end_point_address = endAddresses.get(0).getAddressLine(0);
        Log.d(end_point_address, "checkingEndPointAddress: " + end_point_address);
    }


    @Override
    public void onBackPressed() {

        if (running == true)
        {
            Toast.makeText(this, "Cannot go back while a Trip is in progress.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if (backPressedTime + 1000 > System.currentTimeMillis()){
                super.onBackPressed();
                return;
            }
            else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            }
        }
        backPressedTime = System.currentTimeMillis();
    }
    public void systemStatusCheck() {

        //Intent intent = new Intent(DriverMainActivity.this, DriverMainActivity.class);
        //startActivity(intent);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("SystemStatus");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SystemStatus systemStatus = dataSnapshot.getValue(SystemStatus.class);
                if (systemStatus != null) {
                    currentStatus = systemStatus.getCurrentStatus();
                    Log.d(currentStatus, "System status is: " + currentStatus);
                }
                if (currentStatus.equals("offline")){
                    //starting of the status checking service
                    Intent intent = new Intent(DriverMainActivity.this, StatusCheckService.class);
                    startService(intent);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                }
                else if (currentStatus.equals("online")){
                }
                else {
                    Intent intent = new Intent(DriverMainActivity.this, SystemErrorActivity.class);
                    startActivity(intent);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(String.valueOf(error), "Failed to read value.", error.toException());
            }
        });
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @SuppressLint("NewApi")
        @Override
        public void onActivityResult(Boolean isGranted) {

            if (isGranted){
                Log.d("isGranted", "onActivityResult: granted");
            }
            else
            {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    showCustomAlert("Location Permission", "This app needs Location permissions to run correctly. Please goto settings to allow this permission.", "Go to Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" +BuildConfig.APPLICATION_ID));
                            startActivity(intent);
                        }
                    }, "cancel", null);
                }
                Log.d("isGranted", "onActivityResult: not granted");
            }
        }
    });

    @SuppressLint("NewApi")
    private boolean hasLocationPermission(){
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    void showCustomAlert(String title, String message,
                         String positiveBtnTitle, DialogInterface.OnClickListener positiveListener,
                         String negativeBtnTitle, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveBtnTitle, positiveListener)
                .setNegativeButton(negativeBtnTitle, negativeListener);
        builder.create().show();
    }

    /*
    protected void accAvailabilityCheck(String driverPhoneNumber, Context context) {
        userAccAvailabilityCheck accCheck = new userAccAvailabilityCheck();
        accCheck.checkUserAccAvailability(driverPhoneNumber, getApplicationContext());
    }
    */
    protected void updateUserStatus(String driverPhoneNumber, String currentStatus) {

        userStatusUpdater userStatusUpdater = new userStatusUpdater();
        userStatusUpdater.updateUserStatus(driverPhoneNumber, currentStatus);
    }

    private void tripRunDataUploader(String speed, String totalDistance)
    {
        DatabaseReference nodeRef = FirebaseDatabase.getInstance().getReference().child("Employee");
        Query query = nodeRef.orderByChild("phoneNumber").equalTo(driverPhoneNumber);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                    childSnapshot.getRef().child("speed").setValue(speed + " km/h");
                    childSnapshot.getRef().child("totalDistance").setValue(totalDistance + " km");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DriverMainActivity.this, "Error while uploading tripRun data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //when re-opened from recent apps
    @Override
    protected void onResume() {
        super.onResume();
        userStatus = "Online";
        updateUserStatus(driverPhoneNumber,userStatus);
    }

    //when in background
    @Override
    protected void onPause() {
        super.onPause();
        if (!isAppInFullScreen()) {
            userStatus = "idle";
            updateUserStatus(driverPhoneNumber,userStatus);
        }
    }

    //when exited
    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            userStatus = "Offline";
            updateUserStatus(driverPhoneNumber,userStatus);
        }
    }

    //when the app is force closed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        userStatus = "Offline";
        updateUserStatus(driverPhoneNumber,userStatus);
    }

    //check if the app is running in foreground or in background
    private boolean isAppInFullScreen() {
        int flags = getWindow().getAttributes().flags;
        return (flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }
}
package com.techtop.onehyreapp;

import static com.techtop.onehyreapp.MainActivity.SHARED_PREFS;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashScreenActivity extends AppCompatActivity {
    private BillingClient billingClient;
    boolean isSuccess = false, isSysOnline = false, isQuerySynced = false;
    String currentStatus;
    String accStatus, pendingStatus = "";
    String driverPhone;
    ProgressBar progressBar,progressBarIndeterminate;
    TextView p_feed;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();

        pendingStatus = "";

        progressBar = findViewById(R.id.progress_bar_determinate);
        progressBarIndeterminate = findViewById(R.id.progress_bar_indeterminate);
        p_feed = findViewById(R.id.progress_feedback);

        isInternetAvailable(getApplicationContext());


    }

    // checking for the internet connection availability
    public boolean isInternetAvailable(Context context) {

        p_feed.setText("Checking network availability...");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                progressBarIndeterminate.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                updateProgressBar(10);
                p_feed.setText("Network available");

                OnStartSystemStatusCheckTask task = new OnStartSystemStatusCheckTask(this);
                task.execute();

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (isSysOnline){
                            //checks subscription status
                            billingClient = BillingClient.newBuilder(SplashScreenActivity.this)
                                    .setListener(purchasesUpdatedListener)
                                    .enablePendingPurchases()
                                    .build();
                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

                            p_feed.setText("Checking for registered user data...");
                            if (sharedPreferences.contains("tel")) {
                                String phone = sharedPreferences.getString("tel", "");
                                updateProgressBar(35);
                                downloadingAccDetails(phone);
                                p_feed.setText("User Data found. Checking for subscription type...");
                                query_purchase();
                            }
                            else {
                                query_purchase();
                            }
                        }else {
                            // If the first run is not completed, delay the second run by a short interval
                            new Handler().postDelayed(this, 2000); // Adjust the delay interval as needed
                        }
                    }
                    });
                return true;
            }
            else {
                progressBar.setVisibility(View.GONE);
                progressBarIndeterminate.setVisibility(View.VISIBLE);
                p_feed.setText("Network not available, Connection Lost");
                //Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                builder.setMessage("No internet connection available. Try Again later.");
                builder.setTitle("Connection Disconnected");
                Drawable icon = getResources().getDrawable(R.drawable.no_connection48);
                builder.setIcon(icon);
                builder.setCancelable(false);

                builder.setPositiveButton("Retry", (DialogInterface.OnClickListener) (dialog, which) -> {
                    isInternetAvailable(context);
                });
                builder.setNegativeButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) -> {
                    finish();
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
        return false;
    }
    public void systemStatusCheck() {

        p_feed.setText("Checking system...");
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
                    p_feed.setText("System offline");
                    updateProgressBar(100);
                    //starting of the status checking service
                    Intent intent = new Intent(SplashScreenActivity.this, StatusCheckService.class);
                    startService(intent);
                    finish();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                else if (currentStatus.equals("online")){
                    updateProgressBar(30);
                    p_feed.setText("System is online.");

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (isQuerySynced){

                                stateUpdater();
                            }
                            else {
                                new Handler().postDelayed(this, 2000);
                            }
                        }
                    });

                    isSysOnline = true;

                }
                else {
                    p_feed.setText("System error");
                    updateProgressBar(0);
                    Intent intent = new Intent(SplashScreenActivity.this, SystemErrorActivity.class);
                    startActivity(intent);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                p_feed.setText("Database connection error");
                Log.w(String.valueOf(error), "Failed to read value.", error.toException());
            }
        });
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> Purchase) {

        }
    };
    private void query_purchase() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        try {
                            billingClient.queryPurchasesAsync(
                                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                                    ((billingResult1, purchaseList) -> {
                                        for (Purchase purchase: purchaseList){
                                            if (purchase!=null && purchase.isAcknowledged()){
                                                isSuccess = true;
                                            }
                                        }
                                    })
                            );
                        }catch (Exception ex){

                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                }
                                catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                                if (isSuccess){
                                    p_feed.setText("Premium subscription available. Saving subscription info...");
                                    updateProgressBar(50);
                                    Toast.makeText(SplashScreenActivity.this, "Premium User!", Toast.LENGTH_SHORT).show();
                                    BuildVariant.premium = true;
                                    BuildVariant.removeAds = true;
                                }

                                else {
                                    p_feed.setText("No subscription available. Saving info...");
                                    updateProgressBar(50);
                                    Toast.makeText(SplashScreenActivity.this, "Normal User", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    });
                }
            }
        });
        isQuerySynced = true;
    }

    private void stateUpdater() {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("tel")) {
            String driverPhoneSP = sharedPreferences.getString("tel", "");
            updateProgressBar(75);
            p_feed.setText("Connecting to database...");
            Log.d(driverPhoneSP, "statusUpdatePhoneNumber");

            // Introduce a delay of 2 seconds (adjust as needed)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    DatabaseReference nodeRef = FirebaseDatabase.getInstance().getReference().child("Employee");
                    Query query = nodeRef.orderByChild("phoneNumber").equalTo(driverPhoneSP);
                    updateProgressBar(85);
                    p_feed.setText("Database connection established");

                    // Attach a listener to the query to update the matching child node
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                p_feed.setText("Logging In...");
                                //Toast.makeText(SplashScreenActivity.this, "Connection to server succeed", Toast.LENGTH_SHORT).show();
                                // Account exists in Firebase
                                // Loop through the matching child nodes
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    // Update the data in the child node
                                    updateProgressBar(95);

                                    Employee employee = childSnapshot.getValue(Employee.class);
                                    accStatus = employee.getAccountStatus();
                                    pendingStatus = employee.getIsApproved();

                                    if(accStatus.equals("Enterprise")) {
                                        BuildVariant.isEnterprise = true;
                                        BuildVariant.premium = true;
                                        AdUnitID.AD_UNIT_ID=null;

                                        if (pendingStatus.equals("pending")){

                                            childSnapshot.getRef().child("driverStatus").setValue("Offline");
                                            startActivity(new Intent(SplashScreenActivity.this, ApprovalPendingActivity.class));
                                            finish();
                                        }
                                        else if (pendingStatus.equals("approved"))
                                        {
                                            if (!sharedPreferences.contains("wlc"))
                                            {
                                                startActivity(new Intent(SplashScreenActivity.this, WelcomexActivity.class));
                                                finish();
                                            }
                                            else
                                            {
                                                childSnapshot.getRef().child("driverStatus").setValue("Online");
                                                Toast.makeText(SplashScreenActivity.this, "Logged in as " + employee.getName(), Toast.LENGTH_SHORT).show();
                                                updateProgressBar(100);
                                                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        }
                                        else {
                                            p_feed.setText("User profile fetch failed");
                                            Toast.makeText(SplashScreenActivity.this, "Error occurred while fetching user profile data", Toast.LENGTH_SHORT).show();
                                        }

                                    }else if(accStatus.equals("PremiumUser")){
                                        BuildVariant.isEnterprise = true;
                                        BuildVariant.premium = true;
                                        AdUnitID.AD_UNIT_ID=null;
                                    }else {
                                        childSnapshot.getRef().child("driverStatus").setValue("Online");
                                        BuildVariant.isEnterprise = false;
                                        Toast.makeText(SplashScreenActivity.this, "Logged in as " + employee.getName(), Toast.LENGTH_SHORT).show();
                                        updateProgressBar(100);

                                        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }
                            } else {
                                updateProgressBar(100);
                                p_feed.setText("Log in closed, Account not found");
                                // Account not found in Firebase
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.apply();

                                Toast.makeText(SplashScreenActivity.this, "Connection to server succeed", Toast.LENGTH_SHORT).show();
                                Toast.makeText(SplashScreenActivity.this, "Your account has been deleted. Please Sign up again.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                                finish();

                                //showAccountDeletedDialog(getApplicationContext());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle the error
                            Toast.makeText(SplashScreenActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }, 1000); // Delay of 2 seconds
        } else {
            updateProgressBar(100);
            p_feed.setText("No user data found");
            Toast.makeText(this, "Please Sign up to Continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            finish();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }
    }

    //filling the progressbar
    private void updateProgressBar(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        else {
            Toast.makeText(this, "progress bar err", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadingAccDetails(String tp) {

        DatabaseReference nodeRef = FirebaseDatabase.getInstance().getReference().child("Employee");
        Query query = nodeRef.orderByChild("phoneNumber").equalTo(tp);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                        Employee employee = childSnapshot.getValue(Employee.class);
                        userDataLocalStore.driverPhone = employee.getPhoneNumber();
                        userDataLocalStore.driverName = employee.getName();
                        userDataLocalStore.nic = employee.getNic();
                        userDataLocalStore.vehicleNumber = employee.getVehicleNo();
                        userDataLocalStore.vehicleType = employee.getvType();
                        userDataLocalStore.vehicleModel = employee.getvModel();
                        userDataLocalStore.companyId = employee.getCompanyID();

                        accStatus = employee.getAccountStatus();
                        if(accStatus.equals("Enterprise")) {
                            enterpriseDetailsFetch(userDataLocalStore.companyId);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error
                Toast.makeText(SplashScreenActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enterpriseDetailsFetch(String id) {

        DatabaseReference ref;
        ref = FirebaseDatabase.getInstance().getReference().child("Enterprise");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot entSnapshot : snapshot.getChildren()) {
                        String enterpriseId = entSnapshot.child("entID").getValue(String.class);
                        if (enterpriseId.equals(id)) {
                            userDataLocalStore.companyName = entSnapshot.child("entName").getValue(String.class);
                            userDataLocalStore.companyMail = entSnapshot.child("mail").getValue(String.class);
                            userDataLocalStore.companyPhone = entSnapshot.child("phone").getValue(String.class);

                            //Toast.makeText(SplashScreenActivity.this, ""+userDataLocalStore.companyPhone + userDataLocalStore.companyName, Toast.LENGTH_SHORT).show();
                            break;

                        }
                    }
                } else {
                    Toast.makeText(SplashScreenActivity.this, "Enterprise you registered with is no longer available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showAccountDeletedDialog(Context context) {
        // Create a custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Account Deleted")
                .setMessage("Your account has been deleted. Please Sign up again.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Redirect to the sign-up activity or any other relevant action
                        startActivity(new Intent(context, MainActivity.class));
                    }
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
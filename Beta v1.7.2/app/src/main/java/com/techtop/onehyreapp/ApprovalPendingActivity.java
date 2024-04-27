package com.techtop.onehyreapp;

import static com.techtop.onehyreapp.MainActivity.SHARED_PREFS;

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
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;



import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApprovalPendingActivity extends AppCompatActivity {



    // ad view variables
    private static final String TAG = "LanguageSelect";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);







    String driverPhone;
    String accStatusAp, pendingStatusAp;
    String driverPhoneNumber, userStatus = "Unknown";
    String entCodeSharedPref, enterpriseName, enterpriseMail, enterprisePhone;
    TextView txtPendingEntMail, txtPendingEntPhone, txtPendingNote;
    Button btnCallSupport, btnCallEnterprise, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval_pending);

        pendingStatusAp = "";

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("tel")) {
            String driverPhoneSP = sharedPreferences.getString("tel", "");
            entCodeSharedPref = sharedPreferences.getString("enterprise_id", "");

            enterpriseSearch(entCodeSharedPref);
            approvalCheck();

        }

        txtPendingEntMail = findViewById(R.id.pendingEntMail);
        txtPendingEntPhone = findViewById(R.id.pendingEntPhone);
        txtPendingNote = findViewById(R.id.pendingNotice);
        btnCallEnterprise = findViewById(R.id.btnCallEnterprise);
        btnCallSupport = findViewById(R.id.btnExtraSupport);
        btnLogout = findViewById(R.id.btnLogOut);

        btnCallEnterprise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeOutgoingCall(enterprisePhone, "the Enterprise");
            }
        });

        btnCallSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeOutgoingCall("0762082709", "Technical Support");
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth firebaseAuth;
                firebaseAuth = FirebaseAuth.getInstance();
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                if (sharedPreferences.contains("tel")) {
                    driverPhoneNumber = sharedPreferences.getString("tel", "");
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.clear();
                editor.commit();
                firebaseAuth.signOut();

                startActivity(new Intent(ApprovalPendingActivity.this, SplashScreenActivity.class));
                //finish();

                userStatus = "Offline";
                updateUserStatus(driverPhoneNumber,userStatus);
            }
        });




        //ad view oncreate methods start

        adContainerView = findViewById(R.id.ad_view_container);

        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());

        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        googleMobileAdsConsentManager.gatherConsent(
                this,
                consentError -> {
                    if (consentError != null) {
                        // Consent not obtained in current session.
                        Log.w(
                                TAG,
                                String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
                    }

                    if (googleMobileAdsConsentManager.canRequestAds()) {
                        initializeMobileAdsSdk();
                    }

                    if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()) {
                        // Regenerate the options menu to include a privacy setting.
                        invalidateOptionsMenu();
                    }
                });

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk();
        }

        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adContainerView
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        () -> {
                            if (!initialLayoutComplete.getAndSet(true)
                                    && googleMobileAdsConsentManager.canRequestAds()) {
                                loadBanner();
                            }
                        });

        // Set your test devices. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device."
        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345")).build());


        //ad view oncreate methods end








    }
    protected void updateUserStatus(String driverPhoneNumber, String currentStatus) {

        userStatusUpdater userStatusUpdater = new userStatusUpdater();
        userStatusUpdater.updateUserStatus(driverPhoneNumber, currentStatus);
    }
    @SuppressLint("NewApi")
    private void makeOutgoingCall(String phoneNumber, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ApprovalPendingActivity.this);
        builder.setMessage("This may cause charges on your mobile account. Are you sure you want to call " + message + "?");
        builder.setTitle("SLT MY TAXI Meter is about to make an outgoing voice call.");
        Drawable icon = getResources().getDrawable(R.drawable.ic_cause_charges64);
        builder.setIcon(icon);
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                }

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


    private void enterpriseSearch(String id) {
        DatabaseReference ref;
        ref = FirebaseDatabase.getInstance().getReference().child("Enterprise");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean enterpriseFound = false;
                for (DataSnapshot entSnapshot : snapshot.getChildren()) {
                    String enterpriseId = entSnapshot.child("entID").getValue(String.class);
                    if (enterpriseId.equals(id)) {
                        String compName = entSnapshot.child("entName").getValue(String.class);
                        String compMail = entSnapshot.child("mail").getValue(String.class);
                        String compTp = entSnapshot.child("phone").getValue(String.class);

                        enterpriseName = compName;
                        txtPendingEntMail.setText("Company email: " + compMail);
                        txtPendingEntPhone.setText("Company hotline: " + compTp);
                        enterprisePhone = compTp;
                        txtPendingNote.setText("Your join request has been sent successfully to " + compName + " and is awaiting their approval.");

                        enterpriseFound = true;
                        break; // Exit the loop once the matching enterpriseId is found
                    }
                }

                if (!enterpriseFound) {
                    // This block runs when no matching enterprise is found
                    Toast.makeText(ApprovalPendingActivity.this, "No matching Enterprise", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the database error if needed
            }
        });
    }



    private void approvalCheck() {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("tel")) {
            String driverPhoneSP = sharedPreferences.getString("tel", "");
            Log.d(driverPhoneSP, "statusUpdatePhoneNumber");

                    DatabaseReference nodeRef = FirebaseDatabase.getInstance().getReference().child("Employee");
                    Query query = nodeRef.orderByChild("phoneNumber").equalTo(driverPhoneSP);

                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    //childSnapshot.getRef().child("driverStatus").setValue("Online");

                                    Employee employee = childSnapshot.getValue(Employee.class);
                                    accStatusAp = employee.getAccountStatus();
                                    pendingStatusAp = employee.getIsApproved();
                                    //Toast.makeText(ApprovalPendingActivity.this, " "+pendingStatusAp+ " "+accStatusAp, Toast.LENGTH_SHORT).show();



                                    if (pendingStatusAp.equals("approved")) {
                                        //Toast.makeText(ApprovalPendingActivity.this, "You can continue Now", Toast.LENGTH_SHORT).show();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(ApprovalPendingActivity.this);
                                        builder.setMessage("Your request to join " +enterpriseName + " has been accepted. Please restart the application to apply changes.");
                                        builder.setTitle("Request Approved");
                                        Drawable icon = getResources().getDrawable(R.drawable.ic_user_approved64);
                                        builder.setIcon(icon);
                                        builder.setCancelable(false);

                                        builder.setPositiveButton("Restart", (DialogInterface.OnClickListener) (dialog, which) -> {
                                            Intent intent = new Intent(ApprovalPendingActivity.this, SplashScreenActivity.class);
                                            startActivity(intent);
                                            //finish();
                                        });

                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();

                                    }
                                    else
                                    {
                                        //Toast.makeText(ApprovalPendingActivity.this, "Still waiting", Toast.LENGTH_SHORT).show();

                                    }

                                }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle the error
                            Toast.makeText(ApprovalPendingActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            else
            {
            Toast.makeText(this, "No driver phone number found", Toast.LENGTH_SHORT).show();
        }
    }






    //ad view loading banner
    private void loadBanner() {
        // Create a new ad view.
        adView = new AdView(this);
        adView.setAdUnitId(AdUnitID.AD_UNIT_ID);
        adView.setAdSize(getAdSize());

        // Replace ad container with new ad view.
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        // Start loading the ad in the background.
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {}
                });

        // Load an ad.
        if (initialLayoutComplete.get()) {
            loadBanner();
        }
    }


    //ad view get size of the ad
    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }









}


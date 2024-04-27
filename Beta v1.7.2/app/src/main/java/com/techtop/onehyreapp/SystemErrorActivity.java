package com.techtop.onehyreapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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


public class SystemErrorActivity extends AppCompatActivity {



    // ad view variables
    private static final String TAG = "LanguageSelect";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);



    String currStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_error);
        getSupportActionBar().hide();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("SystemStatus");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SystemStatus systemStatus = dataSnapshot.getValue(SystemStatus.class);
                if (systemStatus != null) {
                    currStatus = systemStatus.getCurrentStatus();
                    Log.d(currStatus, "System down message is: " + currStatus);

                    if (currStatus.equals("offline")) {
                        Intent intent = new Intent(SystemErrorActivity.this, SystemOfflineActivity.class);
                        startActivity(intent);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        finish();
                    }
                    else if (currStatus.equals("online")){
                        Intent intent = new Intent(SystemErrorActivity.this, MainActivity.class);
                        startActivity(intent);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        finish();
                    }
                    else {

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(String.valueOf(error), "Failed to read value.", error.toException());
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
    @Override
    public void onBackPressed() {
        // Do nothing to block going back
        Toast.makeText(this, "There was an error in our Systems. Try Again Later.", Toast.LENGTH_SHORT).show();
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
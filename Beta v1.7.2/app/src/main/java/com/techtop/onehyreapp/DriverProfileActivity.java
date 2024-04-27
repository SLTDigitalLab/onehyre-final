package com.techtop.onehyreapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;




import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;





public class DriverProfileActivity extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    Button btn_update;
    TextView name, driverTypeState, driverProfileState;
    TabLayout tabLayout;
    TabItem tabPersonalInfo, tabEntInfo, tabStats;
    ViewPager2 viewPager2;
    PagerAdapter pagerAdapter;





    // ad view variables
    private static final String TAG = "LanguageSelect";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);











    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);
        getSupportActionBar().setTitle("Profile");

        tabLayout = findViewById(R.id.tabLayout);
        tabPersonalInfo = findViewById(R.id.tabPersonalInfo);
        tabEntInfo = findViewById(R.id.tabEntInfo);
        tabStats = findViewById(R.id.tabStatsInfo);
        viewPager2 = findViewById(R.id.viewPager);

        pagerAdapter = new PagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);

        // setting the tab to the 3rd tab when redirecting from edit profile
        int selectedTab = getIntent().getIntExtra("selectedTab", 0);
        viewPager2.setCurrentItem(selectedTab);

        name = (TextView) findViewById(R.id.txtProfileDriverName);
        driverTypeState = findViewById(R.id.txtProfileDriverState);
        driverProfileState = findViewById(R.id.txtProfilePremiumState);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        if (sharedPreferences.contains("name")||sharedPreferences.contains("tel")||sharedPreferences.contains("vehicle")) {
            loaddata();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });


/*
        btn_update = (Button) findViewById(R.id.btn_update);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DriverProfileActivity.this, UpdateDriverProfile.class));
            }
        });*/
//






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

    public void loaddata(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        name.setText(sharedPreferences.getString("name","~Not set~"));

        if (BuildVariant.premium == true){
            driverProfileState.setText("Premium Member");
        }
        else {
            driverProfileState.setText("Basic Member");
        }

        if (BuildVariant.isEnterprise == true){
            driverTypeState.setText("Enterprise Driver");
            driverProfileState.setText("Premium Member");
        }
        else {
            driverTypeState.setText("Individual Driver");
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
package com.techtop.onehyreapp;

import static com.techtop.onehyreapp.MainActivity.SHARED_PREFS;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;




import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


public class SettingsChangeAppLanguage extends AppCompatActivity {


    // ad view variables
    private static final String TAG = "LanguageSelect";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);




    String currentLang;
    TextView currentLangTextView, selectEnglishLang, selectSinLang, selectTamilLang;
    Boolean isAnimated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_change_app_language);
        getSupportActionBar().setTitle("Change Language");

        selectEnglishLang = findViewById(R.id.textViewEnglishLang);
        selectSinLang = findViewById(R.id.textViewSinhalaLang);
        selectTamilLang = findViewById(R.id.textViewTamilLang);
        currentLangTextView = findViewById(R.id.textViewCurrentLang);

        checkCurrentLang();







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


    public void selectEnglishClick(View view) {
        TextView textView = (TextView) view;
        textView.setBackgroundResource(R.color.highlight);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.FadeOut).duration(100).repeat(0).playOn(textView);
                isAnimated = true;
            }
        }, 100);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (isAnimated){
                    currentLangTextView.setText("English");
                    selectEnglishLang.setVisibility(View.GONE);
                    selectSinLang.setVisibility(View.VISIBLE);
                    selectSinLang.setBackgroundResource(R.color.background2);
                    selectTamilLang.setVisibility(View.VISIBLE);
                    selectTamilLang.setBackgroundResource(R.color.background2);

                    saveData("lngEn");
                }
                else {
                    new Handler().postDelayed(this, 200);
                }
            }
        });
    }

    public void selectSinhalaClick(View view) {
        TextView textView = (TextView) view;
        textView.setBackgroundResource(R.color.highlight);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.FadeOut).duration(100).repeat(0).playOn(textView);
                isAnimated = true;
            }
        }, 100);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (isAnimated){
                    currentLangTextView.setText("සිංහල");
                    selectSinLang.setVisibility(View.GONE);
                    selectTamilLang.setVisibility(View.VISIBLE);
                    selectTamilLang.setBackgroundResource(R.color.background2);
                    selectEnglishLang.setVisibility(View.VISIBLE);
                    selectEnglishLang.setBackgroundResource(R.color.background2);

                    saveData("lngSi");
                }
                else {
                    new Handler().postDelayed(this, 200);
                }
            }
        });
    }

    public void selectTamilClick(View view) {
        TextView textView = (TextView) view;
        textView.setBackgroundResource(R.color.highlight);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.FadeOut).duration(100).repeat(0).playOn(textView);
                isAnimated = true;
            }
        }, 100);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (isAnimated){
                    currentLangTextView.setText("தமிழ்");
                    selectTamilLang.setVisibility(View.GONE);
                    selectEnglishLang.setVisibility(View.VISIBLE);
                    selectEnglishLang.setBackgroundResource(R.color.background2);
                    selectSinLang.setVisibility(View.VISIBLE);
                    selectSinLang.setBackgroundResource(R.color.background2);

                    saveData("lngTa");
                }
                else {
                    new Handler().postDelayed(this, 200);
                }
            }
        });
    }
    private void saveData(String lng) {
        changeLocale(lng);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lng", lng);
        editor.apply();
        finish();
    }

    private void changeLocale(String language){

        if(language.equals("lngEn"))
        {
            LocaleManager.setLocale(this, "en");
            Toast.makeText(this, "App Language changed to English", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SettingsChangeAppLanguage.this, SplashScreenActivity.class);
            startActivity(intent);
            finishAffinity();
        }
        else if(language.equals("lngSi"))
        {
            LocaleManager.setLocale(this, "si");
            Toast.makeText(this, "App Language changed to Sinhala", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SettingsChangeAppLanguage.this, SplashScreenActivity.class);
            startActivity(intent);
            finishAffinity();
        }
        else if(language.equals("lngTa"))
        {
            LocaleManager.setLocale(this, "ta");
            Toast.makeText(this, "App Language changed to Tamil", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SettingsChangeAppLanguage.this, SplashScreenActivity.class);
            startActivity(intent);
            finishAffinity();
        }

    }

    private void checkCurrentLang(){

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("lng")) {
            currentLang = sharedPreferences.getString("lng", "");
        }

        if (currentLang.equals("lngEn"))
        {
            currentLangTextView.setText("English");
            selectEnglishLang.setVisibility(View.GONE);
        }
        else if (currentLang.equals("lngSi"))
        {
            currentLangTextView.setText("සිංහල");
            selectSinLang.setVisibility(View.GONE);
        }
        else if (currentLang.equals("lngTa"))
        {
            currentLangTextView.setText("தமிழ்");
            selectTamilLang.setVisibility(View.GONE);
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
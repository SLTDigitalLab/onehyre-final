package com.techtop.onehyreapp;

import static com.techtop.onehyreapp.MainActivity.SHARED_PREFS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;



import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


public class RegisteredUserVerifyingActivity extends AppCompatActivity {



    // ad view variables
    private static final String TAG = "LanguageSelect";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);





    private FirebaseAuth firebaseAuth;
    Button btnVerify;
    TextView btnTvResend, tvPhoneNumber, tvEnterOTP;
    String PhoneNumber, OTPCode;
    String IRPhoneNo, IRDriverName, IRVehicleNo, IRVehicleType;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_user_verifying);

        btnVerify = findViewById(R.id.btnVerifyOTP);
        btnTvResend = findViewById(R.id.btnTvResendOTP);
        tvEnterOTP = findViewById(R.id.etEnterOtp);
        tvPhoneNumber = findViewById(R.id.tvTitleNote);

        getSupportActionBar().hide(); //hide the title bar
        intentReceive();

        firebaseAuth = FirebaseAuth.getInstance();

        //retrieving telephone number from shared preference
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("tel")) {
            String driverPhoneSP = sharedPreferences.getString("tel", "");
            OTPCode = sharedPreferences.getString("verification_id", "");
            Log.d("OTPCheckIntentTwo", OTPCode);
            PhoneNumber = "+94"+driverPhoneSP;
            tvPhoneNumber.setText("Verification code sent to " + PhoneNumber);
        }


        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String OTPCode = tvEnterOTP.getText().toString().trim();
                if (TextUtils.isEmpty(OTPCode)) {
                    Toast.makeText(RegisteredUserVerifyingActivity.this, "Enter OTP", Toast.LENGTH_SHORT).show();
                }
                else {
                    verifySignInCode();
                }
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

    public void intentReceive() {
        IRDriverName = getIntent().getStringExtra("driverName");
        IRPhoneNo = getIntent().getStringExtra("driverTelephone");
        IRVehicleNo = getIntent().getStringExtra("vehicleNumber");
        IRVehicleType = getIntent().getStringExtra("vehicle_type");

        Log.d("checkIntentReceivedData", IRDriverName + IRVehicleNo + IRPhoneNo);
    }
    public void saveToSP() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", IRDriverName);
        editor.putString("tel", IRPhoneNo);
        editor.putString("vehicle", IRVehicleNo);
        editor.putString("vehicle_type", IRVehicleType);
        editor.putInt("txt", 1);
        editor.apply();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(RegisteredUserVerifyingActivity.this, "Please while we restart the app for you", Toast.LENGTH_SHORT).show();
                        saveToSP();

                        startActivity(new Intent(RegisteredUserVerifyingActivity.this, SplashScreenActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisteredUserVerifyingActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifySignInCode() {
        // getting the sent otp code's verification id from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String verificationId = sharedPreferences.getString("verification_id", "");

        // getting the user entered code from edittext
        String code = tvEnterOTP.getText().toString();

        // Create the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // Sign in with the credential
        signInWithPhoneAuthCredential(credential);
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
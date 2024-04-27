package com.techtop.onehyreapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techtop.onehyreapp.databinding.ActivityMainBinding;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


public class MainActivity extends AppCompatActivity {





    // ad view variables
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);



    private ActivityMainBinding binding;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String mVerificationId;
    String phoneNumber;
    String driverName;
    String vehicleNumber;
    String currentStatus;
    private static String TAG = "Main_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog pd;

    private CheckBox checkBoxTermsCond;

    public static final String SHARED_PREFS = "sharedPrefs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (!sharedPreferences.contains("lng")) {
            Intent intentLng = new Intent(MainActivity.this, LanguageSelect.class);
            startActivity(intentLng);
            intentLng.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
        }
        else {
            String language = sharedPreferences.getString("lng", "");
            //Toast.makeText(this, "your primary lng: " + language, Toast.LENGTH_SHORT).show();
            if(language.equals("lngEn"))
            {
                LocaleManager.setLocale(this, "en");
            }
            else if(language.equals("lngSi"))
            {
                LocaleManager.setLocale(this, "si");
            }
            else if(language.equals("lngTa"))
            {
                LocaleManager.setLocale(this, "ta");
            }
        }

        if (sharedPreferences.contains("txt")) {
            Intent intent = new Intent(MainActivity.this, DriverMainActivity.class);
            startActivity(intent);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //startActivity(new Intent(MainActivity.this, DriverMainActivity.class));
            finish();
        }

        binding.regli.setVisibility(View.VISIBLE); //visible main
        binding.codeLi.setVisibility(View.GONE); //hide code layout
        binding.phoneContinueBtn.setEnabled(false);

        binding.checkBoxTermsAndConditions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                binding.phoneContinueBtn.setEnabled(isChecked);
            }
        });

        binding.textViewShowTermsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTermsAndConditions();
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(this);
        pd.setTitle("Please wait...");
        pd.setCanceledOnTouchOutside(false);

        //test UIs here

//        Button test_btn = (Button) findViewById(R.id.btn_test);
//        test_btn.setOnClickListener(v->{
//            startActivity(new Intent(MainActivity.this, WelcomexActivity.class));
//        });


        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                pd.dismiss();
                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, forceResendingToken);

                Log.d(TAG, "onCodeSentMainActivity: " + verificationId);

                mVerificationId = verificationId;
                forceResendingToken = token;
                pd.dismiss();

                binding.codeLi.setVisibility(View.VISIBLE);
                binding.regli.setVisibility(View.GONE); //hide main

                Toast.makeText(MainActivity.this, "Verification code sent...", Toast.LENGTH_SHORT).show();
                binding.codeSentDescription.setText("Verification code sent to +94 " + binding.phoneEt.getText().toString().trim());

            }
        };

        binding.phoneContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = binding.phoneEt.getText().toString().trim();

                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(MainActivity.this, "Please Enter the phone number", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Employee");

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                //checking firebase for a matching Phone Number
                                if (dataSnapshot.getValue().toString().contains(phone)) {

                                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("tel", phone);
                                    editor.apply();

                                    //Log.d("FirebasePhoneNoCheck", "Value already exists in the database");
                                    Toast.makeText(MainActivity.this, "Registered phone number!\nPlease continue to verify", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(MainActivity.this, RegisteredAccRestore.class));
                                    finish();

                                } else {
                                    startPhoneNumberVerification("+94" + phone);
                                    Log.d("FirebasePhoneNoCheck", "Value does not exist in the database");
                                }
                            } else {
                                Log.d("FirebasePhoneNoCheck", "Data does not exist in the database");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(MainActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                            Log.e("Firebase", "Error checking value", databaseError.toException());
                        }
                    });
                }
            }
        });

        binding.resendCodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = binding.phoneEt.getText().toString().trim();
                phoneNumber = phone;
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(MainActivity.this, "Please Enter the phone number", Toast.LENGTH_SHORT).show();
                } else {
                    resendVerificationCode("+94" + phone, forceResendingToken);
                }
            }
        });

        binding.codeSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = binding.codeEt.getText().toString().trim();
                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(MainActivity.this, "Please Enter verification code", Toast.LENGTH_SHORT).show();
                } else {
                    verifyPhoneNumberWithCode(mVerificationId, code);
                }
            }
        });


/*

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
        
 */















    }

    private void startPhoneNumberVerification(String phone) {
        pd.setMessage("Verifying Phone Number");
        pd.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallback)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void resendVerificationCode(String phone, PhoneAuthProvider.ForceResendingToken token) {
        pd.setMessage("Resending Code");
        pd.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallback)
                .setForceResendingToken(token)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void verifyPhoneNumberWithCode(String mVerificationId, String code) {
        pd.setMessage("Verifying Code");
        pd.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithAuthCredential(credential);
    }

    private void signInWithAuthCredential(PhoneAuthCredential credential) {
        pd.setMessage("Logging in");

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        pd.dismiss();
                        String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                        Toast.makeText(MainActivity.this, "Verification Success!", Toast.LENGTH_SHORT).show();

                        //change here to test intent
                        startActivity(new Intent(MainActivity.this, DriverTypeSelection.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void systemStatusCheck() {

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
                if (currentStatus.equals("offline")) {
                    //starting of the status checking service
                    Intent intent = new Intent(MainActivity.this, StatusCheckService.class);
                    startService(intent);
                    finish();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                } else if (currentStatus.equals("online")) {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(MainActivity.this, SystemErrorActivity.class);
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

    private void showTermsAndConditions() {

        String termsAndConditions = getResources().getString(R.string.terms_and_conditions_agreement);

        // Convert the message to a Spanned object with HTML formatting
        Spanned formattedMessage = Html.fromHtml(termsAndConditions);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Terms and Conditions.");
        builder.setMessage(formattedMessage);
        Drawable icon = getResources().getDrawable(R.drawable.ic_terms_and_conditions_64);
        builder.setIcon(icon);
        builder.setCancelable(false);

        builder.setPositiveButton("Got it", (DialogInterface.OnClickListener) (dialog, which) -> {
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
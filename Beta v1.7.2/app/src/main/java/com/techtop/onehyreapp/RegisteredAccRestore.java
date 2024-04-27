package com.techtop.onehyreapp;

import static com.techtop.onehyreapp.MainActivity.SHARED_PREFS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;




public class RegisteredAccRestore extends AppCompatActivity {



    // ad view variables
    private static final String TAG = "LanguageSelect";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);




    private String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth firebaseAuth;
    TextView tvDriverName, tvPhone, tvVehicleNo, tvDriverNic, tvVehicleType, tvVehicleModel;
    ImageView btnGoBack;
    Button btnContinue;
    String driverPhone, driverNic, vModel, vNumber, vehicleType, entId, entName, entMail, entTp;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_acc_restore);

        tvDriverName = findViewById(R.id.tvDisplayDriverNameRAR);
        tvPhone = findViewById(R.id.tvDisplayPhoneRAR);
        tvVehicleNo = findViewById(R.id.tvDisplayVehicleNumberRAR);
        tvDriverNic = findViewById(R.id.txtDIdRAR);
        tvVehicleModel = findViewById(R.id.txtDVehicleModelRAR);
        tvVehicleType = findViewById(R.id.txtDVehicleCategoryRAR);

        //btnGoBack = findViewById(R.id.ivBackButton);
        btnContinue = findViewById(R.id.btnTest);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("tel")) {
            //tvPhone.setText(sharedPreferences.getString("tel", ""));
            String driverPhoneSP = sharedPreferences.getString("tel", "");
            driverPhone = "+94"+driverPhoneSP;
        }
        fetchDataThree();

        firebaseAuth = FirebaseAuth.getInstance();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sendData();
                startPhoneNumberVerification(driverPhone);
            }
        });

        /*
        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisteredAccRestore.this, MainActivity.class));
                finish();
            }
        }); */





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

    public void fetchDataThree () {

        tvDriverName.setText("Retrieving...");
        tvPhone.setText("Retrieving...");
        tvVehicleNo.setText("Retrieving...");
        tvDriverNic.setText("Retrieving...");
        tvVehicleModel.setText("Retrieving...");
        tvVehicleType.setText("Retrieving...");

        String value = driverPhone;

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Query query = rootRef.child("Employee").orderByChild("phoneNumber").equalTo(value);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {

                    Employee employee = ds.getValue(Employee.class);
                    tvPhone.setText(employee.getPhoneNumber());
                    tvDriverName.setText(employee.getName());
                    driverNic = employee.getNic();
                    tvDriverNic.setText(driverNic);
                    tvVehicleNo.setText(employee.getVehicleNo());
                    vehicleType = employee.getvType();
                    tvVehicleType.setText(vehicleType);
                    vModel = employee.getvModel();
                    tvVehicleModel.setText(vModel);
                    vNumber = employee.getVehicleNo();
                    entId = employee.getCompanyID();
                    enterpriseDetailsFetch(entId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(eventListener);
    }

    public void sendData (){
        saveData();
        String driverName, driveTP, vehicleNo, vType;

        driverName = tvDriverName.getText().toString();
        driveTP = tvPhone.getText().toString();
        vehicleNo = tvVehicleNo.getText().toString();

        Intent intent_send = new Intent(getBaseContext(), RegisteredUserVerifyingActivity.class);
        intent_send.putExtra("driverName", driverName);
        intent_send.putExtra("driverTelephone", driveTP);
        intent_send.putExtra("vehicleNumber", vehicleNo);
        intent_send.putExtra("vehicle_type", vehicleType);

        startActivity(intent_send);
        finish();
    }

    @SuppressLint("LongLogTag")
    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        Log.d("phoneNumberForVerification", phoneNumber);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            signInWithPhoneAuthCredential(credential);
        }

        private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // There was an error
            Toast.makeText(RegisteredAccRestore.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

            super.onCodeSent(verificationId, mResendToken);

            mVerificationId = verificationId;
            mResendToken = token;

            Log.d(verificationId,"onCodeSentOTP: "+ verificationId);

            Toast.makeText(RegisteredAccRestore.this, "Verification code sent...", Toast.LENGTH_SHORT).show();

            // Storing the verification ID in shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("verification_id", verificationId);
            editor.apply();

            // Starting verification activity inside the sendData method
            sendData();
        }
    };

    public void saveData() {

        userDataLocalStore.driverPhone = String.valueOf(tvPhone.getText());
        userDataLocalStore.driverName = String.valueOf(tvDriverName.getText());
        userDataLocalStore.nic = driverNic;
        userDataLocalStore.vehicleNumber = String.valueOf(tvVehicleNo.getText());
        userDataLocalStore.vehicleType = vehicleType;
        userDataLocalStore.vehicleModel = vModel;
        userDataLocalStore.companyId = entId;
        userDataLocalStore.companyName = entName;
        userDataLocalStore.companyMail = entMail;
        userDataLocalStore.companyPhone = entTp;
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
                            entName = entSnapshot.child("entName").getValue(String.class);
                            entMail = entSnapshot.child("mail").getValue(String.class);
                            entTp = entSnapshot.child("phone").getValue(String.class);

                            //Toast.makeText(SplashScreenActivity.this, ""+userDataLocalStore.companyPhone + userDataLocalStore.companyName, Toast.LENGTH_SHORT).show();
                            break;

                        }
                    }
                } else {
                    Toast.makeText(RegisteredAccRestore.this, "Enterprise you registered with is no longer available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
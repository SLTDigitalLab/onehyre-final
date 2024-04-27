package com.techtop.onehyreapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;



import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


public class DetailsActivity extends AppCompatActivity {


    // ad view variables
    private static final String TAG = "LanguageSelect";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);







    FirebaseAuth firebaseAuth;
    TextView edit_phone,sign_in;
    String authPhone, edit_vehicle, selected_v_type, selected_v_model,v_no_letters, v_no_numbers;
    EditText edit_name, vehicle_no_letters, vehicle_no_numbers;
    Spinner vehicle_type, vehicle_model;
    //EditText edit_vehicle;
    DAOEmployee dao = new DAOEmployee();
    public static final String SHARED_PREFS = "sharedPrefs";
    String vehicleNo;

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_phone = (TextView) findViewById(R.id.edit_tel);
        sign_in = (TextView) findViewById(R.id.sign_in);
        vehicle_no_numbers = findViewById(R.id.edit_vehicle_Numbers);
        vehicle_no_letters = findViewById(R.id.edit_vehicle);
        vehicle_no_letters.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        Button btn = (Button) findViewById(R.id.register);

        vehicle_model = findViewById(R.id.dropdown_menu_v_sub_category);
        vehicle_type = findViewById(R.id.dropdown_menu_v_type);

        edit_phone.setText(authPhone);

        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.vehicle_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        vehicle_type.setAdapter(adapter);

        vehicle_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selected_vehicle_type = adapterView.getItemAtPosition(position).toString();
                //Toast.makeText(DetailsActivity.this, selectedValue, Toast.LENGTH_SHORT).show();
                setVehicleModel(selected_vehicle_type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sign_in.setOnClickListener(v->{
            startActivity(new Intent(DetailsActivity.this, MainActivity.class));
        });

        btn.setOnClickListener(v->{

            edit_vehicle = vehicle_no_letters.getText().toString() + vehicle_no_numbers.getText().toString();
            Log.d("vehicleNumberNewFormat", edit_vehicle);

            if(TextUtils.isEmpty(edit_name.getText().toString().trim()))
            {
                Toast.makeText(DetailsActivity.this, "Please Enter the Name", Toast.LENGTH_SHORT).show();
            }
            else if(TextUtils.isEmpty(vehicle_no_letters.getText().toString().trim())){
                Toast.makeText(DetailsActivity.this, "Please Enter the Vehicle Reg.no", Toast.LENGTH_SHORT).show();
            }
            else if(TextUtils.isEmpty(vehicle_no_numbers.getText().toString().trim())){
                Toast.makeText(DetailsActivity.this, "Please Enter the Vehicle Reg.no", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //insert action
                selected_v_type = vehicle_type.getSelectedItem().toString();
                selected_v_model = vehicle_model.getSelectedItem().toString();
                vehicleNo = edit_vehicle;
                checkDbPhone();

                Log.d(selected_v_type + selected_v_model, "vehicle type and vehicle model ");
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

    private void checkUserStatus() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null){
            //user logged in
            String phone = firebaseUser.getPhoneNumber();
            //edit_phone.setText(firebaseUser.getPhoneNumber().toString());
            authPhone = phone;
        }else {
            //user not logged in
            finish();
        }

    }

    public void checkDbPhone() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Employee");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    //checking firebase for the matching Vehicle Number
                    if (dataSnapshot.getValue().toString().contains(vehicleNo))
                    {
                        Log.d("FirebaseVehicleNoCheck", "Value already exists in the database");
                        Toast.makeText(DetailsActivity.this, "This vehicle is already registered!", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        /*
                        //data inserting
                        Employee emp = new Employee(edit_name.getText().toString(), authPhone, edit_vehicle.toString(), "online", "",
                                vehicle_type.getSelectedItem().toString(), vehicle_model.getSelectedItem().toString());
                        dao.add(emp).addOnSuccessListener(suc->
                        {
                            saveData();
                            Toast.makeText(DetailsActivity.this, "Registered Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(DetailsActivity.this, WelcomexActivity.class));
                        }).addOnFailureListener(er->{
                            Toast.makeText(DetailsActivity.this, "Error! " + er.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                        Log.d("FirebaseVehicleNoCheck", "Value does not exist in the database");*/
                    }
                }
                else
                {
                    Log.d("FirebaseVehicleNoCheck", "Data does not exist in the database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error checking value", databaseError.toException());
            }
        });

    }
    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", String.valueOf(edit_name.getText()));
        editor.putString("tel", String.valueOf(authPhone));
        editor.putString("vehicle", String.valueOf(edit_vehicle));
        editor.putString("vehicle_type", selected_v_type);
        editor.putString("vehicle_model", selected_v_model);
        editor.putInt("txt", 1);
        editor.apply();

        Log.d(String.valueOf(vehicle_type), "vehicleType: ");
    }

    public void setVehicleModel(String type){
        //Toast.makeText(this, type, Toast.LENGTH_SHORT).show();
        if (type.equals("Tuks")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.tuks_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            vehicle_model.setAdapter(adapter1);
        }

        if (type.equals("Flex")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.flex_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            vehicle_model.setAdapter(adapter1);
        }

        if (type.equals("Mini")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.mini_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            vehicle_model.setAdapter(adapter1);
        }

        if (type.equals("Cars")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.cars_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            vehicle_model.setAdapter(adapter1);
        }

        if (type.equals("Minivans")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.minivans_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            vehicle_model.setAdapter(adapter1);
        }

        if (type.equals("Vans")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.vans_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            vehicle_model.setAdapter(adapter1);
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
package com.techtop.onehyreapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;



import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.BillingClientStateListener;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterNewUserActivity extends AppCompatActivity {


    Button btnSubraction;
    FirebaseAuth firebaseAuth;
    TextView ePriseTopTitle, expandGuide, driverInfoTitle, textViewDName, textViewVNumber, textViewDNic, textViewVModel, textViewVType, textViewDPhone, textViewDriverPhone, textViewEntFoundTitle, textviewEntFoundMail, textviewEntFoundPhone;
    EditText editTextDriverName, editTextNic, editTextVNumberLetters, editTextVNumberDigits, editTextEnterpriseCode;
    String combinedVNumber, authPhone, selectedVType, selectedVModel, vehicleRegNo, driverCourse, companyIDCode, approvalStatus, enterpriseName, enterpriseIDCode, compDriverPackage, compDriverCount,billingDate,billingOrderID,isPlatformCharges;
    String livLoc = "79.84651166666667, 6.937011666666667", compMail, compTp;
    Group enterpriseInfoGroup, enterpriseFoundGroup, driverInfoGroup;
    CardView enterpriseInfoCard, driverInfoCard;
    Button btnSignUp, btnGotoSignUp, btnEntFoundContinue;
    Spinner spinnerVModel, spinnerVType;
    DAOEmployee dao = new DAOEmployee();
    ImageView enterpriseFound;
    private String sts, txtPrice; // Declare sts here

    String response, des, sku;
    boolean isSuccess = false;
    private BillingClient billingClient;

    public static final String SHARED_PREFS = "sharedPrefs";
    boolean isEPInfoCardExpanded = false, isEnterpriseFound = false, isDriverInfoCardExpanded;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new_user);
        getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        // Enterprise card Fields
        editTextEnterpriseCode = findViewById(R.id.editTextEnterpriseCode);
        btnGotoSignUp = findViewById(R.id.btnSubmitCode);
        enterpriseFound = findViewById(R.id.imgViewEnterpriseFound);

        // Enterprise card -Enterprise found group fields
        textViewEntFoundTitle = findViewById(R.id.ePFoundEntNameTitle);
        textviewEntFoundMail = findViewById(R.id.ePFoundEntEmail);
        textviewEntFoundPhone = findViewById(R.id.ePFoundEntPhone);
        btnEntFoundContinue = findViewById(R.id.epFoundContinueButton);

        // Driver Info Fields
        textViewDName = findViewById(R.id.textViewDName);
        editTextDriverName = findViewById(R.id.editTextDriverName);
        textViewDNic = findViewById(R.id.textViewDNic);
        editTextNic = findViewById(R.id.editTextNic);
        textViewDPhone = findViewById(R.id.textViewDPhone);
        textViewDriverPhone = findViewById(R.id.textViewDriverPhone);
        textViewVType = findViewById(R.id.textViewVType);
        spinnerVType = findViewById(R.id.spinnerVType);
        textViewVModel = findViewById(R.id.textViewVModel);
        spinnerVModel = findViewById(R.id.spinnerVModel);
        textViewVNumber = findViewById(R.id.textViewVNumber);
        editTextVNumberLetters = findViewById(R.id.editTextVNumberLetters);
        editTextVNumberDigits = findViewById(R.id.editTextVNumberDigits);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSubraction=findViewById(R.id.btn_sub);

        textViewDriverPhone.setText(authPhone);

        // Making Edit Text Letters all caps
        editTextVNumberLetters.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        // Cards
        driverInfoCard = findViewById(R.id.personalInfoCard);
        enterpriseInfoCard = findViewById(R.id.enterpriseInfoCard);

        // Groups
        driverInfoGroup = findViewById(R.id.groupPersonalInfo);
        enterpriseInfoGroup = findViewById(R.id.groupEnterpriseInfo);
        enterpriseFoundGroup = findViewById(R.id.groupEnterpriseFound);

        // TextViews
        ePriseTopTitle = findViewById(R.id.txtEnterpriseTitle);
        expandGuide = findViewById(R.id.expandGuideTextView);
        driverInfoTitle = findViewById(R.id.txtViewDriverInfoTitle);

        // Spinner drop down menu items
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.vehicle_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerVType.setAdapter(adapter);

        spinnerVType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        if (!BuildVariant.isEnterprise){
            ePriseTopTitle.setText("Content unavailable");
            expandGuide.setText("Stand-alone employees cannot access this content.");
        }

        driverInfoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverInfoCardExpand();
                if (isEPInfoCardExpanded){
                    isEPInfoCardExpanded = false;
                    updateCardState(isEPInfoCardExpanded, enterpriseInfoGroup);
                    updateCardState(isEPInfoCardExpanded, enterpriseFoundGroup);
                }
            }
        });

        enterpriseInfoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BuildVariant.isEnterprise){
                    Toast.makeText(RegisterNewUserActivity.this, "Sorry you are not eligible for this service.", Toast.LENGTH_SHORT).show();
                }
                else {
                    isEPInfoCardExpanded = !isEPInfoCardExpanded;
                    if (isEnterpriseFound){
                        updateCardState(isEPInfoCardExpanded, enterpriseFoundGroup);
                    }
                    else {
                        updateCardState(isEPInfoCardExpanded, enterpriseInfoGroup);
                    }
                }
            }
        });


        // Enterprise code submit button actions
        btnGotoSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterpriseFound.setVisibility(View.GONE);

                if (editTextEnterpriseCode.getText().toString().isEmpty()){
                    Toast.makeText(RegisterNewUserActivity.this, "Enterprise code field is empty", Toast.LENGTH_SHORT).show();
                }
                if (!editTextEnterpriseCode.getText().toString().isEmpty()){
                    companyIDCode = editTextEnterpriseCode.getText().toString();

                    enterpriseSearch(companyIDCode);

                    driverInfoCardExpand();
                    if (isEPInfoCardExpanded){
                        isEPInfoCardExpanded = false;
                        updateCardState(isEPInfoCardExpanded, enterpriseInfoGroup);
                    }
                }
            }
        });

        btnEntFoundContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverInfoCardExpand();
                if (isEPInfoCardExpanded){
                    isEPInfoCardExpanded = false;
                    updateCardState(isEPInfoCardExpanded, enterpriseInfoGroup);
                    updateCardState(isEPInfoCardExpanded, enterpriseFoundGroup);
                }
            }
        });

        // Register Button actions
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Getting driver course, Enterprise or Stand-Alone and Enterprise code
                if (BuildVariant.isEnterprise)
                {
                    driverCourse = "Enterprise";
                    approvalStatus = "pending";

                    if (!isEnterpriseFound)
                    {
                        Toast.makeText(RegisterNewUserActivity.this, "You haven't joined to an Enterprise yet", Toast.LENGTH_SHORT).show();

                        isEPInfoCardExpanded = !isEPInfoCardExpanded;
                        updateCardState(isEPInfoCardExpanded, enterpriseInfoGroup);
                    }
                    else
                    {
                        combinedVNumber = editTextVNumberLetters.getText().toString() + editTextVNumberDigits.getText().toString();

                        if(TextUtils.isEmpty(editTextDriverName.getText().toString().trim()))
                        {
                            Toast.makeText(RegisterNewUserActivity.this, "Please Enter the Name", Toast.LENGTH_SHORT).show();
                        }
                        else if (TextUtils.isEmpty(editTextNic.getText().toString().trim()))
                        {
                            Toast.makeText(RegisterNewUserActivity.this, "Please Enter your NIC Number", Toast.LENGTH_SHORT).show();
                        }
                        else if(TextUtils.isEmpty(editTextVNumberLetters.getText().toString().trim())){

                            Toast.makeText(RegisterNewUserActivity.this, "Please Enter the Vehicle Reg.no", Toast.LENGTH_SHORT).show();
                        }
                        else if(TextUtils.isEmpty(editTextVNumberDigits.getText().toString().trim()))
                        {
                            Toast.makeText(RegisterNewUserActivity.this, "Please Enter the Vehicle Reg.no", Toast.LENGTH_SHORT).show();
                        }
                      /*  else if(!isSuccess){
                            Toast.makeText(RegisterNewUserActivity.this, "Please pay the platform chrges", Toast.LENGTH_SHORT).show();

                        }

                       */

                        else
                        {

                            //insert action
                            selectedVType = spinnerVType.getSelectedItem().toString();
                            selectedVModel = spinnerVModel.getSelectedItem().toString();
                            vehicleRegNo = combinedVNumber;
                            checkDbPhone();
                            //Toast.makeText(RegisterNewUserActivity.this, "registered phase", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else
                {

                    driverCourse = "freeIndividual";
                    companyIDCode = "stand_alone";
                    editTextEnterpriseCode.setText("stand_alone");
                    approvalStatus = "stand_alone";

                    combinedVNumber = editTextVNumberLetters.getText().toString() + editTextVNumberDigits.getText().toString();

                    if(TextUtils.isEmpty(editTextDriverName.getText().toString().trim()))
                    {
                        Toast.makeText(RegisterNewUserActivity.this, "Please Enter the Name", Toast.LENGTH_SHORT).show();
                    }
                    else if (TextUtils.isEmpty(editTextNic.getText().toString().trim()))
                    {
                        Toast.makeText(RegisterNewUserActivity.this, "Please Enter your NIC Number", Toast.LENGTH_SHORT).show();
                    }
                    else if(TextUtils.isEmpty(editTextVNumberLetters.getText().toString().trim())){

                        Toast.makeText(RegisterNewUserActivity.this, "Please Enter the Vehicle Reg.no", Toast.LENGTH_SHORT).show();
                    }
                    else if(TextUtils.isEmpty(editTextVNumberDigits.getText().toString().trim()))
                    {
                        Toast.makeText(RegisterNewUserActivity.this, "Please Enter the Vehicle Reg.no", Toast.LENGTH_SHORT).show();
                    }
                    else if(!isSuccess){
                        Toast.makeText(RegisterNewUserActivity.this, "Please pay the platform chrges", Toast.LENGTH_SHORT).show();

                    }
                    else
                    {

                        //insert action
                        selectedVType = spinnerVType.getSelectedItem().toString();
                        selectedVModel = spinnerVModel.getSelectedItem().toString();
                        vehicleRegNo = combinedVNumber;
                        checkDbPhone();
                        //Toast.makeText(RegisterNewUserActivity.this, "registered phase", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



        //platform charges paying

        initializeBillingClient();

        btnSubraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(RegisterNewUserActivity.this, "button sub clicked", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterNewUserActivity.this);
                builder.setTitle("Platform charges");
                builder.setMessage("The platform charges 50 LKR Are you sure want to pay?");
                Drawable icon = getResources().getDrawable(R.drawable.ic_warning32);
                builder.setIcon(icon);
                builder.setCancelable(false);

                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {

                    initiateBillingFlow();
                });
                builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


    }

    public void checkDbPhone() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Employee");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    //checking firebase for the matching Vehicle Number
                    if (dataSnapshot.getValue().toString().contains(vehicleRegNo))
                    {
                        Log.d("FirebaseVehicleNoCheck", "Value already exists in the database");
                        Toast.makeText(RegisterNewUserActivity.this, "This vehicle is already registered!", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        //data inserting
                        Employee emp = new Employee(editTextDriverName.getText().toString(), authPhone, combinedVNumber.toString(), "offline", livLoc,
                                spinnerVType.getSelectedItem().toString(), spinnerVModel.getSelectedItem().toString(), driverCourse, editTextEnterpriseCode.getText().toString(), approvalStatus, editTextNic.getText().toString(),true,billingDate,billingOrderID);
                        dao.add(emp).addOnSuccessListener(suc->
                        {
                            saveData();

                            if (BuildVariant.isEnterprise){
                                int newDriverCount = Integer.parseInt(compDriverCount) + 1;
                                updateCompDriverCount(String.valueOf(newDriverCount));

                                String pendingStatus = emp.getIsApproved();

                                if (pendingStatus.equals("pending")){
                                    Toast.makeText(RegisterNewUserActivity.this, "Request is pending", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterNewUserActivity.this, ApprovalPendingActivity.class));
                                    finish();
                                }
                                else
                                {
                                    Toast.makeText(RegisterNewUserActivity.this, "Registered Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterNewUserActivity.this, WelcomexActivity.class));
                                    finish();

                                }
                            }
                            else {
                                Toast.makeText(RegisterNewUserActivity.this, "Registered Successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterNewUserActivity.this, WelcomexActivity.class));
                                finish();
                            }

                        }).addOnFailureListener(er->{
                            Toast.makeText(RegisterNewUserActivity.this, "Error! " + er.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                        Log.d("FirebaseVehicleNoCheck", "Value does not exist in the database");
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

        userDataLocalStore.driverPhone = String.valueOf(authPhone);
        userDataLocalStore.driverName = String.valueOf(editTextDriverName.getText());
        userDataLocalStore.nic = String.valueOf(editTextNic.getText());
        userDataLocalStore.vehicleNumber = String.valueOf(combinedVNumber);
        userDataLocalStore.vehicleType = selectedVType;
        userDataLocalStore.vehicleModel = selectedVModel;
        userDataLocalStore.companyId = enterpriseIDCode;
        userDataLocalStore.companyName = enterpriseName;
        userDataLocalStore.companyMail = compMail;
        userDataLocalStore.companyPhone = compTp;

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", String.valueOf(editTextDriverName.getText()));
        editor.putString("tel", String.valueOf(authPhone));
        editor.putString("vehicle", String.valueOf(combinedVNumber));
        editor.putString("vehicle_type", selectedVType);
        editor.putString("vehicle_model", selectedVModel);
        editor.putString("enterprise_name", enterpriseName);
        editor.putString("enterprise_id", enterpriseIDCode);
        editor.putString("req_status", approvalStatus);
        editor.putInt("txt", 1);
        editor.apply();
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

    private void updateCardState(boolean isExpanded, Group group) {
        if (isExpanded) {
            expandCard(group);
        } else {
            collapseCard(group);
        }
    }

    private void collapseCard(Group group) {
        group.setVisibility(View.GONE);
        ePriseTopTitle.setVisibility(View.VISIBLE);
        expandGuide.setVisibility(View.VISIBLE);
        driverInfoCardExpand();
    }

    private void expandCard(Group group) {
        ePriseTopTitle.setVisibility(View.GONE);
        group.setVisibility(View.VISIBLE);
        expandGuide.setVisibility(View.GONE);
        driverInfoCardColl();
    }

    private void driverInfoCardColl(){
        driverInfoTitle.setVisibility(View.VISIBLE);
        driverInfoGroup.setVisibility(View.GONE);
    }

    private void driverInfoCardExpand(){
        driverInfoTitle.setVisibility(View.GONE);
        driverInfoGroup.setVisibility(View.VISIBLE);
    }

    public void setVehicleModel(String type){
        //Toast.makeText(this, type, Toast.LENGTH_SHORT).show();
        if (type.equals("Tuks")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.tuks_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinnerVModel.setAdapter(adapter1);
        }

        if (type.equals("Flex")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.flex_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinnerVModel.setAdapter(adapter1);
        }

        if (type.equals("Mini")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.mini_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinnerVModel.setAdapter(adapter1);
        }

        if (type.equals("Cars")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.cars_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinnerVModel.setAdapter(adapter1);
        }

        if (type.equals("Minivans")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.minivans_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinnerVModel.setAdapter(adapter1);
        }

        if (type.equals("Vans")){
            ArrayAdapter<CharSequence> adapter1=ArrayAdapter.createFromResource(this, R.array.vans_types, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinnerVModel.setAdapter(adapter1);
        }
    }

    private void enterpriseSearch(String id){
        DatabaseReference ref;
        ref = FirebaseDatabase.getInstance().getReference().child("Enterprise");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean found = false;
                    for (DataSnapshot entSnapshot : snapshot.getChildren()) {
                        String enterpriseId = entSnapshot.child("entID").getValue(String.class);
                        if (enterpriseId.equals(id)) {
                            String compName = entSnapshot.child("entName").getValue(String.class);
                            compMail = entSnapshot.child("mail").getValue(String.class);
                            compTp = entSnapshot.child("phone").getValue(String.class);
                            compDriverPackage = entSnapshot.child("driverPackage").getValue(String.class);
                            compDriverCount = entSnapshot.child("RegisteredDrivers").getValue(String.class);

                            //Toast.makeText(RegisterNewUserActivity.this, "Driver Package = "+compDriverPackage+", Driver Count = "+compDriverCount, Toast.LENGTH_SHORT).show();

                            if (Integer.parseInt(compDriverCount) >= Integer.parseInt(compDriverPackage)){
                                Toast.makeText(RegisterNewUserActivity.this, "Driver limit has reached", Toast.LENGTH_SHORT).show();

                                enterpriseFound.setImageResource(R.drawable.ic_exclamtion_64_2);
                                enterpriseFound.setVisibility(View.VISIBLE);
                                expandGuide.setText("The enterprise you are trying to connect with has reached the limit of its driver quota.");

                                isEnterpriseFound = false;
                            }
                            else {
                                enterpriseName = compName;
                                enterpriseIDCode = enterpriseId;
                                isEnterpriseFound = true;

                                enterpriseFound.setImageResource(R.drawable.ic_ok_green_tick96);
                                enterpriseFound.setVisibility(View.VISIBLE);

                                ePriseTopTitle.setText(compName);

                                expandGuide.setText("To be a part of "+ compName +", Continue and Sign up to the app.");
                                textViewEntFoundTitle.setText(compName);
                                textviewEntFoundPhone.setText("Company email: " + compMail);
                                textviewEntFoundMail.setText("Company hotline: " + compTp);

                                //Toast.makeText(RegisterNewUserActivity.this, "Selected Enterprise: " +compName, Toast.LENGTH_SHORT).show();
                            }

                            found = true;
                            break; // Exit the loop once the matching tariff_id is found
                        }
                    }
                    if (!found) {
                        enterpriseFound.setImageResource(R.drawable.ic_error_cross64);
                        enterpriseFound.setVisibility(View.VISIBLE);
                        isEnterpriseFound = false;
                    }
                } else {
                    Toast.makeText(RegisterNewUserActivity.this, "err", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateCompDriverCount(String newCount){

        //Toast.makeText(this, ""+newCount, Toast.LENGTH_SHORT).show();
        DatabaseReference refToUpdate = FirebaseDatabase.getInstance().getReference().child("Enterprise");

        Query query = refToUpdate.orderByChild("entID").equalTo(enterpriseIDCode);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot entSnapshot : snapshot.getChildren()) {
                        entSnapshot.getRef().child("RegisteredDrivers").setValue(newCount)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Toast.makeText(RegisterNewUserActivity.this, "RegisteredDrivers updated successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        //Toast.makeText(RegisterNewUserActivity.this, "Failed to update RegisteredDrivers.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        break;
                    }
                } else {
                    //Toast.makeText(RegisterNewUserActivity.this, "No matching Enterprise", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void initializeBillingClient() {
        billingClient = BillingClient.newBuilder(RegisterNewUserActivity.this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        getPrice();
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> Purchase) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && Purchase != null) {
                for (Purchase purchase : Purchase) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                sts="Already Subscribed";
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                sts="Option does not support";
            } else {
                Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void initiateBillingFlow() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                List<QueryProductDetailsParams.Product> productList = List.of(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("platform_charge_sub")
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()
                );
                QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build();
                billingClient.queryProductDetailsAsync(params, (billingResult1, productDetailsList) -> {
                    for (ProductDetails productDetails : productDetailsList) {
                        String offerToken = productDetails.getSubscriptionOfferDetails()
                                .get(0).getOfferToken();
                        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = List.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken(offerToken)
                                        .build()
                        );
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build();
                        billingClient.launchBillingFlow(RegisterNewUserActivity.this, billingFlowParams);
                    }
                });
            }
        });
    }

    private void handlePurchase(Purchase purchase) {

         billingOrderID = purchase.getOrderId();

        // Retrieve the purchase time in milliseconds
        long purchaseTimeMillis = purchase.getPurchaseTime();

        // Convert purchase time to a human-readable date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
         billingDate = sdf.format(new Date(purchaseTimeMillis));


        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                Toast.makeText(getApplicationContext(), "Error : invalid purchase", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                //sts.setText("Subscribed");

            } else {
                //sts.setText("Already Subscribed");

            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            sts="Sub Pending";
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {

            sts="Sub UNSPECIFIED_STATE";
        }
    }



    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                isSuccess = true;

            }
        }
    };

    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwZdOTDYgujm/XJgGCGcfNhXIZjOOBRDEEkExJhhVBPS/x21w5uBTCrxuK4t3jOlB61iIMA2+SeIQjHf5NMxH+1bOuiVviTDNQDeNvjujiM+0Rd6dS+o15qtFT+O2rNEEpz4SAK3RWTDL1J0+mxwNEldTpCWSDCpI72kW5lJnN5h9AF5i9BsBoU9loPmYWxJsMylags4lkBLPenXd7JYYnVjxLIdXPNkeZARun0Sz19ILcevvYulBD5HOmE5el6nNKuaTroMmSRBx6TH3XR12scoViNaj30zprXOeluEsmt4fTQ2D3yPUTpCXhqOZ1Rt+ASnQIuITxbACZ+a0Hp4RPQIDAQAB";
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }


    private void getPrice() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                billingClient.startConnection(new BillingClientStateListener() {
                    @Override
                    public void onBillingServiceDisconnected() {

                    }

                    @Override
                    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                        List<QueryProductDetailsParams.Product> productList = List.of(
                                QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId("platform_charge_sub")
                                        .setProductType(BillingClient.ProductType.SUBS)
                                        .build());
                        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                                .setProductList(productList)
                                .build();

                        billingClient.queryProductDetailsAsync(
                                params,
                                new ProductDetailsResponseListener() {
                                    @Override
                                    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> productDetailsList) {
                                        for (ProductDetails productDetails : productDetailsList) {
                                            response = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases()
                                                    .getPricingPhaseList().get(0).getFormattedPrice();
                                            sku = productDetails.getName();
                                            String ds = productDetails.getDescription();
                                            des = sku + " " + ds + " " + "Price " + response;
                                        }
                                    }
                                }
                        );
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                       // txtPrice.setText(response);
                        //desc.setText(des);
                        //tvSub.setText(sku);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }



}
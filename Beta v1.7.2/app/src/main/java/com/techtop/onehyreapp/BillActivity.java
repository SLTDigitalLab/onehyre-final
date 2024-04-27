package com.techtop.onehyreapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


public class BillActivity extends AppCompatActivity implements CustomAlertbox.CustomAlertBoxListener {


    // ad view variables
    private static final String TAG = "LanguageSelect";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);

    private AlertDialog alertDialog;
    View qrDialogBox;
    Bitmap bitmapQr;
    TextView dateTxt,vnoTxt,phoneTxt,oneKmRateTxt,twoKmRate,startTime,waitTime,waitRate,distance,totFare,endTime,totHireTime;
    Button btnCancle, btnSave;
    public static final String SHARED_PREFS = "sharedPrefs";
    String vno,pno, dname = "~Not set~";
    private String recMail, recPhone, recName;
    private String tripID, tripIDMain = "invalid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);
        getSupportActionBar().setTitle("Bill");

        BuildVariant.isSavedPremium = false;
        BuildVariant.isSavedFree = false;

        dateTxt = (TextView) findViewById(R.id.datetxt);
        vnoTxt = (TextView) findViewById(R.id.v_numtxt);
        phoneTxt = (TextView) findViewById(R.id.pnotxt);
        oneKmRateTxt = (TextView) findViewById(R.id.onekmtxt);
        twoKmRate = (TextView) findViewById(R.id.twokmtxt);
        startTime = (TextView) findViewById(R.id.stimetxt);
        waitTime = (TextView) findViewById(R.id.wtimetxt);
        waitRate = (TextView) findViewById(R.id.wratetxt);
        distance = (TextView) findViewById(R.id.distnacetxt);
        totFare = (TextView) findViewById(R.id.tothiretxt);
        endTime = (TextView) findViewById(R.id.end_time);
        totHireTime = (TextView) findViewById(R.id.tot_hire_time);
        btnCancle = (Button) findViewById(R.id.canclebtn);
        btnSave = (Button) findViewById(R.id.savebtn);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        if (sharedPreferences.contains("name")||sharedPreferences.contains("tel")||sharedPreferences.contains("vehicle")) {
            loaddata();
        }

        dateTxt.setText(getIntent().getStringExtra("date"));
        vnoTxt.setText(vno);
        phoneTxt.setText(pno);
        oneKmRateTxt.setText(getIntent().getStringExtra("kmone_fee"));
        twoKmRate.setText(getIntent().getStringExtra("kmtwo_fee"));
        startTime.setText(getIntent().getStringExtra("started_time"));
        waitTime.setText(getIntent().getStringExtra("total_wait_time"));
        waitRate.setText(getIntent().getStringExtra("wait_fee"));
        distance.setText(getIntent().getStringExtra("distance"));
        totFare.setText(getIntent().getStringExtra("total_fare"));
        totHireTime.setText(getIntent().getStringExtra("hire_time"));
        endTime.setText(getIntent().getStringExtra("end_time"));

        generatingTripID();

        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIntent().removeExtra("start_point_address");
                getIntent().removeExtra("end_point_address");

                //go back confirmation for pro version
                if (BuildVariant.premium){
                    if (BuildVariant.isSavedPremium)
                    {
                        startActivity(new Intent(BillActivity.this, MainActivity.class));
                        finish();
                    }
                    else {
                        warningGoBackDialog();
                    }
                }
                //go back confirmation for free version
                else {
                    if (BuildVariant.isSavedFree){
                        startActivity(new Intent(BillActivity.this, MainActivity.class));
                        finish();
                    }
                    else {
                        warningGoBackDialog();
                    }
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BuildVariant.premium)
                {
                    if (BuildVariant.isSavedPremium == false)
                    {
                        openDialogAlertBox();
                    }
                    else
                    {
                        alreadySavedAlert();
                    }
                }
                if (BuildVariant.premium == false)
                {
                    if (BuildVariant.isSavedFree == false)
                    {
                        fetchDataFreeVersion();
                    }
                    else
                    {
                        alreadySavedAlert();
                    }
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

    private void alreadySavedAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Already Saved");
        builder.setMessage("You have already saved this trip's details and cannot save again.");
        Drawable icon = getResources().getDrawable(R.drawable.ic_warning_octagon_yellow65);
        builder.setIcon(icon);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
            if (BuildVariant.premium){
                if (BuildVariant.isSavedPremium)
                {
                    startActivity(new Intent(BillActivity.this, MainActivity.class));
                    finish();
                }
                else {
                    warningGoBackDialog();
                }
            }
            //go back confirmation for free version
            else {
                if (BuildVariant.isSavedFree){
                    startActivity(new Intent(BillActivity.this, MainActivity.class));
                    finish();
                }
                else {
                    warningGoBackDialog();
                }
            }
    }

    private void warningGoBackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unsaved Progress");
        builder.setMessage("You haven't saved current trip's details. If you go back this trip's details will be permanently lost and we won't be able to help you get them back. Are you sure?");
        Drawable icon = getResources().getDrawable(R.drawable.ic_warning32);
        builder.setIcon(icon);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(BillActivity.this, MainActivity.class));
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, dismiss the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void generatingTripID() {
        Date date = new Date();
        long timeInMill = date.getTime();
        tripIDMain = "TRIP" + timeInMill;

    }

    private void openDialogAlertBox() {
        CustomAlertbox customAlertbox = new CustomAlertbox();
        customAlertbox.show(getSupportFragmentManager(), "Custom Alert Box");
    }

    public void fetchDataFreeVersion() {
        Date date = new Date();
        long timeMill = date.getTime();
        tripID = tripIDMain;

        DAOTripData dao = new DAOTripData();

        TripData tripData = new TripData(getIntent().getStringExtra("date"),
                pno,
                vno,
                getIntent().getStringExtra("kmone_fee"),
                getIntent().getStringExtra("kmtwo_fee"),
                getIntent().getStringExtra("started_time"),
                getIntent().getStringExtra("total_wait_time"),
                getIntent().getStringExtra("wait_fee"),
                getIntent().getStringExtra("distance"),
                getIntent().getStringExtra("total_fare"),
                getIntent().getStringExtra("hire_time"),
                getIntent().getStringExtra("end_time"),
                String.valueOf(timeMill),
                String.valueOf(recName = "Free Version User"),
                String.valueOf(recMail = "Free Version User"),
                String.valueOf(recPhone = "Free Version User"),
                String.valueOf(tripID),
                getIntent().getStringExtra("start_point_address"),
                getIntent().getStringExtra("end_point_address"),
                getIntent().getStringExtra("coordinates_lat_lon")
        );
        Log.d(getIntent().getStringExtra("end_point_address"), "checkEndPointAddressBillActivity: ");
        Log.d(getIntent().getStringExtra("start_point_address"), "checkStartPointAddressBillActivity: ");

        dao.add(tripData).addOnSuccessListener(suc->{
            Toast.makeText(BillActivity.this, "Inserted to Hire Details!", Toast.LENGTH_SHORT).show();
            BuildVariant.isSavedFree = true;
        }).addOnFailureListener(er->{
            Toast.makeText(BillActivity.this, "Error occurred: "+er.getMessage(), Toast.LENGTH_SHORT).show();
        });
        generateQR();
        showQrDialogBoxNew();
    }
    //fetching data into firebase
    public void fetchData() {

        Date date = new Date();
        long timeMill = date.getTime();
        tripID = tripIDMain;

        DAOTripData dao = new DAOTripData();

        TripData tripData = new TripData(getIntent().getStringExtra("date"),
                pno,
                vno,
                getIntent().getStringExtra("kmone_fee"),
                getIntent().getStringExtra("kmtwo_fee"),
                getIntent().getStringExtra("started_time"),
                getIntent().getStringExtra("total_wait_time"),
                getIntent().getStringExtra("wait_fee"),
                getIntent().getStringExtra("distance"),
                getIntent().getStringExtra("total_fare"),
                getIntent().getStringExtra("hire_time"),
                getIntent().getStringExtra("end_time"),
                String.valueOf(timeMill),
                String.valueOf(recName),
                String.valueOf(recMail.toString()),
                String.valueOf(recPhone),
                String.valueOf(tripID),
                getIntent().getStringExtra("start_point_address"),
                getIntent().getStringExtra("end_point_address"),
                getIntent().getStringExtra("coordinates_lat_lon")
        );
        Log.d(getIntent().getStringExtra("end_point_address"), "checkEndPointAddressBillActivity: ");
        Log.d(getIntent().getStringExtra("start_point_address"), "checkStartPointAddressBillActivity: ");

        dao.add(tripData).addOnSuccessListener(suc->{
            Toast.makeText(BillActivity.this, "Inserted to Hire Details!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(er->{
            Toast.makeText(BillActivity.this, "Error occurred: "+er.getMessage(), Toast.LENGTH_SHORT).show();
        });
        generateQR();
    }

    //sending mail
    private void sendMail() {
        String mail = "" + recMail;

        String subject = "Receipt for the customer";

        String message = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta content=\"width=device-width, initial-scale=1\" name=\"viewport\">\n" +
                "    <meta name=\"x-apple-disable-message-reformatting\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta content=\"telephone=no\" name= \"format-detection\">\n" +
                "    <title></title>\n" +
                "    <!--[if (mso 16)]>\n" +
                "    <style type=\"text/css\">\n" +
                "    a {text-decoration: none;}\n" +
                "    </style>\n" +
                "    <![endif]-->\n" +
                "    <!--[if gte mso 9]><style>sup { font-size: 100% !important; }</style><![endif]-->\n" +
                "    <!--[if gte mso 9]>\n" +
                "<xml>\n" +
                "    <o:OfficeDocumentSettings>\n" +
                "    <o:AllowPNG></o:AllowPNG>\n" +
                "    <o:PixelsPerInch>96</o:PixelsPerInch>\n" +
                "    </o:OfficeDocumentSettings>\n" +
                "</xml>\n" +
                "<![endif]-->\n" +
                "    <!--[if !mso]><!-- -->\n" +
                "    <link href=\"https://fonts.googleapis.com/css?family=Roboto:400,400i,700,700i\" rel=\"stylesheet\">\n" +
                "    <!--<![endif]-->\n" +
                "</head>\n" +
                "\n" +
                "<body data-new-gr-c-s-loaded=\"14.1091.0\">\n" +
                "    <div class=\"es-wrapper-color\">\n" +
                "        <!--[if gte mso 9]>\n" +
                "\t\t\t<v:background xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"t\">\n" +
                "\t\t\t\t<v:fill type=\"tile\" color=\"#f6f6f6\"></v:fill>\n" +
                "\t\t\t</v:background>\n" +
                "\t\t<![endif]-->\n" +
                "        <table class=\"es-wrapper\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "            <tbody>\n" +
                "                <tr>\n" +
                "                    <td class=\"esd-email-paddings\" valign=\"top\">\n" +
                "                        <table class=\"esd-header-popover es-header\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n" +
                "                            <tbody>\n" +
                "                                <tr>\n" +
                "                                    <td class=\"esd-stripe\" align=\"center\">\n" +
                "                                        <table class=\"es-header-body\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#ffffff\" align=\"center\">\n" +
                "                                            <tbody>\n" +
                "                                                <tr>\n" +
                "                                                    <td class=\"es-p20t es-p20r es-p20l esd-structure\" align=\"left\" bgcolor=\"#d0e0e3\" style=\"background-color: #d0e0e3;\">\n" +
                "                                                        <!--[if mso]><table width=\"560\" cellpadding=\"0\"\n" +
                "                            cellspacing=\"0\"><tr><td width=\"180\" valign=\"top\"><![endif]-->\n" +
                "                                                        <table class=\"es-left\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\">\n" +
                "                                                            <tbody>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td class=\"es-m-p0r es-m-p20b esd-container-frame\" width=\"180\" valign=\"top\" align=\"center\">\n" +
                "                                                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                                            <tbody>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td align=\"center\" class=\"esd-block-text\">\n" +
                "                                                                                        <p style=\"font-size: 20px;\"><strong><span style=\"font-family:roboto,'helvetica neue',helvetica,arial,sans-serif;\"><span style=\"font-size:28px;\">SLT MY TAXI&nbsp;</span><br></span><span style=\"font-size:14px;\">Email Receipt for Trip ID: TRIP1678947487382 </span></strong></p>\n" +
                "                                                                                    </td>\n" +
                "                                                                                </tr>\n" +
                "                                                                            </tbody>\n" +
                "                                                                        </table>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                            </tbody>\n" +
                "                                                        </table>\n" +
                "                                                        <!--[if mso]></td><td width=\"20\"></td><td width=\"360\" valign=\"top\"><![endif]-->\n" +
                "                                                        <table class=\"es-right\" cellspacing=\"0\" cellpadding=\"0\" align=\"right\">\n" +
                "                                                            <tbody>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td class=\"esd-container-frame\" width=\"360\" align=\"left\">\n" +
                "                                                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                                            <tbody>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td align=\"center\" class=\"esd-block-image\" style=\"font-size: 0px;\"><a target=\"_blank\"><img class=\"adapt-img\" src=\"https://island.lk/wp-content/uploads/2021/08/slt-2.jpg\" alt style=\"display: block;\" width=\"360\"></a></td>\n" +
                "                                                                                </tr>\n" +
                "                                                                            </tbody>\n" +
                "                                                                        </table>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                            </tbody>\n" +
                "                                                        </table>\n" +
                "                                                        <!--[if mso]></td></tr></table><![endif]-->\n" +
                "                                                    </td>\n" +
                "                                                </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                            </tbody>\n" +
                "                        </table>\n" +
                "                        <table class=\"es-content\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n" +
                "                            <tbody>\n" +
                "                                <tr>\n" +
                "                                    <td class=\"esd-stripe\" align=\"center\">\n" +
                "                                        <table class=\"es-content-body\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#ffffff\" align=\"center\">\n" +
                "                                            <tbody>\n" +
                "                                                <tr>\n" +
                "                                                    <td class=\"es-p20t es-p20r es-p20l esd-structure\" align=\"left\" bgcolor=\"#fff2cc\" style=\"background-color: #fff2cc;\">\n" +
                "                                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                            <tbody>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td class=\"esd-container-frame\" width=\"560\" valign=\"top\" align=\"center\">\n" +
                "                                                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                                            <tbody>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td align=\"left\" class=\"esd-block-text\">\n" +
                "                                                                                        <p><strong>Customer Name: </strong>"+Html.fromHtml(recName)+"<br><strong>Customer Telephone </strong>: "+Html.fromHtml(recPhone)+"<strong><br>Date</strong>: "+Html.fromHtml(dateTxt.getText().toString())+"<br><strong>Driver Name </strong>: " +Html.fromHtml(dname)+ "<br><strong>Driver's Phone Number</strong>: " +Html.fromHtml(pno)+ "<br><strong>1km rate</strong>: " +Html.fromHtml(oneKmRateTxt.getText().toString())+ "<br><strong>2km rate</strong>: "+Html.fromHtml(twoKmRate.getText().toString())+"<br><strong>Starting time</strong>: "+Html.fromHtml(startTime.getText().toString())+"<br><strong>Waiting time</strong>: "+Html.fromHtml(waitTime.getText().toString())+"<br><strong>Waiting rate</strong>: "+Html.fromHtml(waitRate.getText().toString())+"<br><strong>Distance</strong>: "+Html.fromHtml(distance.getText().toString())+" km<br><strong>End time</strong>: "+Html.fromHtml(endTime.getText().toString())+"<br><strong>Total hire time</strong>: "+Html.fromHtml(totHireTime.getText().toString())+"</p>\n" +
                "                                                                                        <p style=\"text-align: center; font-size: 24px;\"><strong>Total hire value : "+Html.fromHtml(totFare.getText().toString())+"</strong></p>\n" +
                "                                                                                        <p>&nbsp;</p>\n" +
                "                                                                                    </td>\n" +
                "                                                                                </tr>\n" +
                "                                                                            </tbody>\n" +
                "                                                                        </table>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                            </tbody>\n" +
                "                                                        </table>\n" +
                "                                                    </td>\n" +
                "                                                </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                            </tbody>\n" +
                "                        </table>\n" +
                "                        <table class=\"esd-footer-popover es-footer\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n" +
                "                            <tbody>\n" +
                "                                <tr>\n" +
                "                                    <td class=\"esd-stripe\" align=\"center\">\n" +
                "                                        <table class=\"es-footer-body\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#ffffff\" align=\"center\">\n" +
                "                                            <tbody>\n" +
                "                                                <tr>\n" +
                "                                                    <td class=\"esd-structure es-p20t es-p20b es-p20r es-p20l\" align=\"left\" bgcolor=\"#000100\" style=\"background-color: #000100;\">\n" +
                "                                                        <!--[if mso]><table width=\"560\" cellpadding=\"0\" \n" +
                "                        cellspacing=\"0\"><tr><td width=\"270\" valign=\"top\"><![endif]-->\n" +
                "                                                        <table class=\"es-left\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\">\n" +
                "                                                            <tbody>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td class=\"es-m-p20b esd-container-frame\" width=\"270\" align=\"left\">\n" +
                "                                                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                                            <tbody>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td align=\"center\" class=\"esd-block-button\">\n" +
                "                                                                                        <!--[if mso]><a href=\"https://slt.lk\" target=\"_blank\" hidden>\n" +
                "\t<v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" esdevVmlButton href=\"https://slt.lk\" \n" +
                "                style=\"height:39px; v-text-anchor:middle; width:142px\" arcsize=\"50%\" strokecolor=\"#2cb543\" strokeweight=\"2px\" fillcolor=\"#31cb4b\">\n" +
                "\t\t<w:anchorlock></w:anchorlock>\n" +
                "\t\t<center style='color:#ffffff; font-family:arial, \"helvetica neue\", helvetica, sans-serif; font-size:14px; font-weight:400; line-height:14px;  mso-text-raise:1px'>Visit our site</center>\n" +
                "\t</v:roundrect></a>\n" +
                "<![endif]-->\n" +
                "                                                                                        <!--[if !mso]><!-- --><span class=\"msohide es-button-border\"><a href=\"https://slt.lk\" class=\"es-button\" target=\"_blank\">Visit our site</a></span>\n" +
                "                                                                                        <!--<![endif]-->\n" +
                "                                                                                    </td>\n" +
                "                                                                                </tr>\n" +
                "                                                                            </tbody>\n" +
                "                                                                        </table>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                            </tbody>\n" +
                "                                                        </table>\n" +
                "                                                        <!--[if mso]></td><td width=\"20\"></td><td width=\"270\" valign=\"top\"><![endif]-->\n" +
                "                                                        <table class=\"es-right\" cellspacing=\"0\" cellpadding=\"0\" align=\"right\">\n" +
                "                                                            <tbody>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td class=\"esd-container-frame\" width=\"270\" align=\"left\">\n" +
                "                                                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                                            <tbody>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td align=\"left\" class=\"esd-block-text\">\n" +
                "                                                                                        <p style=\"color: #ffffff;\">SLT My Taxi © 2021&nbsp;- 2022&nbsp;All rights reserved.</p>\n" +
                "                                                                                    </td>\n" +
                "                                                                                </tr>\n" +
                "                                                                            </tbody>\n" +
                "                                                                        </table>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                            </tbody>\n" +
                "                                                        </table>\n" +
                "                                                        <!--[if mso]></td></tr></table><![endif]-->\n" +
                "                                                    </td>\n" +
                "                                                </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                            </tbody>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </tbody>\n" +
                "        </table>\n" +
                "    </div>\n" +
                "</body>\n" +
                "\n" +
                "</html>";


        JavaMailAPI javaMailAPI = new JavaMailAPI(this,mail,subject,message);
        javaMailAPI.execute();


    }

    public void loaddata(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        pno = sharedPreferences.getString("tel","~Not set~");
        vno = sharedPreferences.getString("vehicle","~Not set~");
        dname = sharedPreferences.getString("name", "~Not set~");
    }

    @Override
    public void applyTexts(String clientPNo, String clientMail, String clientName) {

        this.recMail = recMail;
        this.recPhone = recPhone;
        this.recName = recName;
        recMail = clientMail;
        recPhone = clientPNo;
        recName = clientName;
        sendMail();
        fetchData();
        //showQrDialogBox();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (BuildVariant.isMailSent){
                    showQrDialogBoxNew();
                    BuildVariant.isMailSent = false;
                }
                else {
                    new Handler().postDelayed(this, 2000);
                }
            }
        });
    }

    private void generateQR() {

        String tripDataLink = "https://sltmytaxi.online/searchresult.php?tripID=" + tripIDMain;

        ArrayList<String> data = new ArrayList<>();
        data.add(dateTxt.getText().toString());
        data.add(vnoTxt.getText().toString());
        data.add(phoneTxt.getText().toString());
        data.add(oneKmRateTxt.getText().toString());
        data.add(twoKmRate.getText().toString());
        data.add(startTime.getText().toString());
        data.add(waitTime.getText().toString());
        data.add(waitRate.getText().toString());
        data.add(distance.getText().toString());
        data.add(totFare.getText().toString());
        data.add(totHireTime.getText().toString());
        data.add(endTime.getText().toString());

        ArrayList<String> orderedData = new ArrayList<>();
        orderedData.add("\nDate: " + data.get(0));
        orderedData.add("\nVehicle Number: " + data.get(1));
        orderedData.add("\nPhone Number: " + data.get(2));
        //orderedData.add("\n1 km Rate: " + data.get(3));
        //orderedData.add("\n2 km Rate: " + data.get(4));
        orderedData.add("\nStarted Time: " + data.get(5));
        orderedData.add("\nTotal Wait Time: " + data.get(6));
        //orderedData.add("\nWait Rate: " + data.get(7));
        orderedData.add("\nDistance: " + data.get(8));
        orderedData.add("\nTotal Fare: " + data.get(9));
        orderedData.add("\nTotal Hire Time: " + data.get(10));
        orderedData.add("\nEnd Time: " + data.get(11));
        orderedData.add("\nFrom: " + getIntent().getStringExtra("start_point_address"));
        orderedData.add("\nTo: " + getIntent().getStringExtra("end_point_address"));

        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(tripDataLink, BarcodeFormat.QR_CODE, 800, 800);

            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmapQr = encoder.createBitmap(matrix);

        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    public void showQrDialogBox() {

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        qrDialogBox = LayoutInflater.from(this).inflate(R.layout.custom_dialog_box_qr, null);

        ImageView qrCode = qrDialogBox.findViewById(R.id.imgQrCode);
        TextView tripId = qrDialogBox.findViewById(R.id.txtTripId);
        Button btnContinue = qrDialogBox.findViewById(R.id.btnQrDone);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BillActivity.this, MainActivity.class));
                finish();
            }
        });
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        qrCode.setImageBitmap(bitmapQr);
        tripId.setText(tripIDMain);
        dialogBuilder.setView(qrDialogBox);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
     */

    public void showQrDialogBoxNew() {

        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayoutDialogBox);
        View view = LayoutInflater.from(BillActivity.this).inflate(R.layout.cus_alert_dialog_box_layout, constraintLayout);


        ImageView qrCode = view.findViewById(R.id.imgQrCode);
        TextView tripId = view.findViewById(R.id.txtTripId);
        Button btnContinue = view.findViewById(R.id.btnQrDone);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BillActivity.this, MainActivity.class));
                finish();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(BillActivity.this);
        qrCode.setImageBitmap(bitmapQr);
        tripId.setText(tripIDMain);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        if(alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
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
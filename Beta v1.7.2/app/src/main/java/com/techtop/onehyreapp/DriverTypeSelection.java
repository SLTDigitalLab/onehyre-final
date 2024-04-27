package com.techtop.onehyreapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DriverTypeSelection extends AppCompatActivity {

    private boolean isContentVisible = false, isEntConVisible = false;
    private Button btnIndividual, btnEnterprise;
    String btnText;
    private CardView individualCard, toolBarCard;
    private CardView enterpriseCard;
    TextView moreInfoIndividual, moreInfoEnterprise, mainTitleTop;
    ImageView imgVSupport, imgVQuit, imgVHelp;
    Group groupIndividual, groupEnterprise, groupTopTitle;
    private boolean isIndividualCardExpanded = false;
    private boolean isEnterpriseCardExpanded = false;
    private int originalHeight;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_type_selection);
        getSupportActionBar().hide();

        BuildVariant.isEnterprise = false;

        //ImageViews
        imgVHelp = findViewById(R.id.imageViewHelp);
        imgVQuit = findViewById(R.id.imageViewQuit);
        imgVSupport = findViewById(R.id.imageViewSupport);

        // Cards
        individualCard = findViewById(R.id.individualCard);
        enterpriseCard = findViewById(R.id.enterpriseCard);
        toolBarCard = findViewById(R.id.toolBarCard);

        // TextViews
        moreInfoIndividual = findViewById(R.id.moreInfoTextViewIndividual);
        moreInfoEnterprise = findViewById(R.id.moreInfoTextViewEnterprise);
        mainTitleTop = findViewById(R.id.txtTopTitle);

        // Groups
        groupIndividual = findViewById(R.id.groupIndividual);
        groupEnterprise = findViewById(R.id.groupEnterprise);
        groupTopTitle = findViewById(R.id.groupTopTitle);

        // Buttons
        btnIndividual = findViewById(R.id.btnIndividual);
        btnEnterprise = findViewById(R.id.btnEnterprise);


        btnIndividual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BuildVariant.isEnterprise = false;
                startActivity(new Intent(DriverTypeSelection.this, RegisterNewUserActivity.class));
            }
        });

        btnEnterprise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BuildVariant.isEnterprise = true;
                startActivity(new Intent(DriverTypeSelection.this, RegisterNewUserActivity.class));
            }
        });

        imgVSupport.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverTypeSelection.this);
                builder.setMessage("This may cause charges on your mobile account. Are you sure you want to call the Technical Support?");
                builder.setTitle("SLT MY TAXI Meter is about to make an outgoing voice call.");
                Drawable icon = getResources().getDrawable(R.drawable.ic_cause_charges64);
                builder.setIcon(icon);
                builder.setCancelable(false);

                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {

                    if (ContextCompat.checkSelfPermission(DriverTypeSelection.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        callTechnicalSupport();
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
        });

        imgVQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverTypeSelection.this);
                builder.setTitle("Are you sure you want to cancel the setup?");
                builder.setMessage("You are about to exit from the app and your setup progress won't be saved.");
                Drawable icon = getResources().getDrawable(R.drawable.ic_warning32);
                builder.setIcon(icon);
                builder.setCancelable(false);

                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                    finish();
                });
                builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        imgVHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpNoticePopUp();
            }
        });

        individualCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnterpriseCardExpanded) {
                    isEnterpriseCardExpanded = false;
                    updateCardState(isEnterpriseCardExpanded, moreInfoEnterprise, groupEnterprise, btnEnterprise);
                }
                isIndividualCardExpanded = !isIndividualCardExpanded;
                updateCardState(isIndividualCardExpanded, moreInfoIndividual, groupIndividual, btnIndividual);
            }
        });

        enterpriseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isIndividualCardExpanded) {
                    isIndividualCardExpanded = false;
                    updateCardState(isIndividualCardExpanded, moreInfoIndividual, groupIndividual, btnIndividual);
                }
                isEnterpriseCardExpanded = !isEnterpriseCardExpanded;
                updateCardState(isEnterpriseCardExpanded, moreInfoEnterprise, groupEnterprise, btnEnterprise);
            }
        });
    }

    private void updateCardState(boolean isExpanded, TextView moreInfoTextView, Group group, Button button) {
        if (isExpanded) {
            moreInfoTextView.setText(getResources().getString(R.string.less_information));
            expandCard(group, button, moreInfoTextView);
        } else {
            moreInfoTextView.setText(getResources().getString(R.string.more_information));
            collapseCard(group, button, moreInfoTextView);
        }
    }

    private void expandCard(Group group, Button button, TextView moreInfoTxt) {
        //TransitionManager.beginDelayedTransition((ViewGroup) group.getParent(), new AutoTransition());
        group.setVisibility(View.VISIBLE);
        btnText = (String) button.getText();
        //button.setVisibility(View.GONE);
        button.setText(getString(R.string.select_course_card_expand));
        moreInfoTxt.setVisibility(View.GONE);

        // Top title collapse
        collTopTitleCard();

        // Bottom Tool bar hide(Slide Down)
        hideToolBarCard();
    }

    private void collapseCard(Group group, Button button, TextView moreInfoTxt) {
        //TransitionManager.beginDelayedTransition((ViewGroup) group.getParent(), new AutoTransition());
        group.setVisibility(View.GONE);
        button.setVisibility(View.VISIBLE);
        moreInfoTxt.setVisibility(View.VISIBLE);
        button.setText(btnText);

        // Top title expand
        expandTopTitleCard();

        // Bottom Tool bring back hide(Slide Up)
        bringBackToolBarCard();
    }

    private void hideToolBarCard(){

        // Animation
        Transition slideUp = new Slide();
        int durationMillis = 400;
        slideUp.setDuration(durationMillis);
        TransitionManager.beginDelayedTransition(toolBarCard, slideUp);

        //TransitionManager.beginDelayedTransition((toolBarCard) , new Slide());
        toolBarCard.setVisibility(View.GONE);
    }

    private void bringBackToolBarCard(){

        // Animation
        Transition slideUp = new Slide();
        int durationMillis = 500;
        slideUp.setDuration(durationMillis);
        TransitionManager.beginDelayedTransition(toolBarCard, slideUp);

        toolBarCard.setVisibility(View.VISIBLE);
    }

    private void collTopTitleCard(){

        mainTitleTop.setVisibility(View.GONE);
        groupTopTitle.setVisibility(View.VISIBLE);
    }

    private void expandTopTitleCard(){

        mainTitleTop.setVisibility(View.VISIBLE);
        groupTopTitle.setVisibility(View.GONE);

    }

    private void helpNoticePopUp(){
        String message = "In our app, we let to register two kinds of employees under following two courses." +
                "<br><br>1. Individual (Stand-Alone) Employees." +
                "<br>2. Enterprise Employees." +
                "<br><br><b>*Who are the Individual employees?</b>" +
                "<br>- Individuals are stand-alone employees who work independently and are not associated with any particular team, company, or enterprise.\n\n" +
                "<br><br><b>*Who are the Enterprise employees?</b>" +
                "<br>- Enterprise is for employees registered under specific companies who work with teams, companies, or enterprises." +
                "<br><br><b>*How to select one course?</b>" +
                "<br>- To select one course from Individual course and Enterprise course, simple click on the button which you are wishing to join." +
                "<br><br><b>*Can I change my course lately?</b>" +
                "<br>- No, you cannot change your course after this screen via this app. To change your course you will need to contact customer support." +
                "<br><br><b>*How to get a Enterprise (Company) code?</b>" +
                "<br>- Since the all the companies are third party companies, to get a joining code you will need to contact the company that you are willing to join or contact our customer support center for more information." +
                "<br><br><b>*How to contact customer support?</b>" +
                "<br>- You can call our customer support by tapping on the call icon from below toolbar.";

        // Convert the message to a Spanned object with HTML formatting
        Spanned formattedMessage = Html.fromHtml(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(DriverTypeSelection.this);
        builder.setTitle("Help.");
        builder.setMessage(formattedMessage);
        Drawable icon = getResources().getDrawable(R.drawable.ic_help_question_mark64);
        builder.setIcon(icon);
        builder.setCancelable(false);

        builder.setPositiveButton("Got it", (DialogInterface.OnClickListener) (dialog, which) -> {
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

    private void callTechnicalSupport() {
        String phoneNumber = "0762082709";
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        }
    }

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
}
package com.techtop.onehyreapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivatePremiumActivity extends AppCompatActivity {

    private BillingClient billingClient;
    boolean isSuccess = false;
    Button btnUpgrade;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_premium);
        billingClient = BillingClient.newBuilder(ActivatePremiumActivity.this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        query_purchase();
        getSupportActionBar().setTitle("Upgrade");

        btnUpgrade = findViewById(R.id.btnUpgradeToPro);

        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivatePremiumActivity.this, Subscription.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> Purchase) {

        }
    };
    private void query_purchase() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        try {
                            billingClient.queryPurchasesAsync(
                                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                                    ((billingResult1, purchaseList) -> {
                                        for (Purchase purchase: purchaseList){
                                            if (purchase!=null && purchase.isAcknowledged()){
                                                isSuccess = true;
                                            }
                                        }
                                    })
                            );
                        }catch (Exception ex){

                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                }
                                catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                                if (isSuccess){
                                    BuildVariant.premium = true;
                                    BuildVariant.removeAds = true;
                                }
                            }
                        });
                    });
                }
            }
        });
    }

    private void popup_alert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivatePremiumActivity.this);
        builder.setMessage("The premium plan has been activated!\nNow you can send the receipt to the customer using Email and SMS");
        //
        builder.setTitle("Purchase Premium");
        //builder.setIcon(android.R.drawable.);
        builder.setCancelable(true);

        builder.setPositiveButton("OK", (DialogInterface.OnClickListener) (dialog, which) -> {

            Intent intent = new Intent(ActivatePremiumActivity.this, DriverMainActivity.class);
            startActivity(intent);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();

        });
        //builder.setNegativeButton("OK", (DialogInterface.OnClickListener) (dialog, which) -> {
        //  finish();
        //});

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}

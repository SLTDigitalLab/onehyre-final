package com.techtop.onehyreapp;
import android.app.Application;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PurchasesUpdatedListener;

public class MyApplication extends Application {

    private BillingClient billingClient;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeBillingClient();
    }

    private void initializeBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .build();
    }

    public BillingClient getBillingClient() {
        return billingClient;
    }

    // You can add other methods and functionality as needed
}
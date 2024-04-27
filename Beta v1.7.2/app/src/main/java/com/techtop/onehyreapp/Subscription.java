package com.techtop.onehyreapp;

import static com.techtop.onehyreapp.DriverMainActivity.SHARED_PREFS;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Subscription extends AppCompatActivity {

    private BillingClient billingClient;
    TextView sts, txtPrice, desc, tvSub;
    ImageView subbed;
    Button btn_sub, btn_mCash;
    String response, des, sku,billingDate,billingOrderID,driverPhoneNumber;
    boolean isSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        getSupportActionBar().hide();

        subbed = findViewById(R.id.ivAlreadySubbed);
        sts = findViewById(R.id.subStatus);
        txtPrice = findViewById(R.id.tvPrice);
        //desc = findViewById(R.id.tv_benifit);
        //tvSub = findViewById(R.id.tv_subid);
        btn_sub = findViewById(R.id.btn_sub);
        btn_mCash = findViewById(R.id.btn_sub_m_cash);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("tel")) {
            driverPhoneNumber = sharedPreferences.getString("tel", "");

        }

        billingClient = BillingClient.newBuilder(Subscription.this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        if (BuildVariant.premium) {
            //sts.setVisibility(View.VISIBLE);
            //sts.setText("Already Subscribed");
            subbed.setVisibility(View.VISIBLE);
            btn_sub.setVisibility(View.GONE);
            btn_mCash.setVisibility(View.GONE);
        } else {
            //sts.setText("Not Subscribed");
            btn_sub.setVisibility(View.VISIBLE);
            btn_mCash.setVisibility(View.VISIBLE);
        }
        getPrice();

        btn_mCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCashPay();
            }
        });
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> Purchase) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && Purchase != null) {
                for (Purchase purchase : Purchase) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                sts.setVisibility(View.VISIBLE);
                sts.setText("Already Subscribed");
                btn_sub.setVisibility(View.GONE);
                btn_mCash.setVisibility(View.GONE);
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                sts.setVisibility(View.VISIBLE);
                btn_mCash.setVisibility(View.VISIBLE);
                sts.setText("Option does not support");
            } else {
                Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void getPrice_click(View view) {
        txtPrice.setText("...");
        getPrice();
    }

    public void btn_sub_click(View view) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                List<QueryProductDetailsParams.Product> productList = List.of(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("month_premium_sub")
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
                        billingClient.launchBillingFlow(Subscription.this, billingFlowParams);
                    }
                });
            }
        });
    }

    void handlePurchase(Purchase purchase) {

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
                isSuccess = true;
                btn_sub.setVisibility(View.GONE);
                btn_mCash.setVisibility(View.GONE);
                subbed.setVisibility(View.VISIBLE);
            } else {
                //sts.setText("Already Subscribed");
                btn_sub.setVisibility(View.GONE);
                btn_mCash.setVisibility(View.GONE);
                subbed.setVisibility(View.VISIBLE);
            }
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            sts.setVisibility(View.VISIBLE);
            sts.setText("Sub Pending");
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            sts.setVisibility(View.VISIBLE);
            sts.setText("Sub UNSPECIFIED_STATE");
        }
    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                sts.setVisibility(View.VISIBLE);
                sts.setText("Subscribed");
                isSuccess = true;
                premiumUserUpdate premiumUserUpdate=new premiumUserUpdate();
                premiumUserUpdate.updateUserPremiumStatus(driverPhoneNumber,"PremiumUser");
                premiumUserUpdate.addRecordToPremiumUsers(driverPhoneNumber,billingOrderID,billingDate);

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

    public void btn_back_click(View view) {
        startActivity(new Intent(Subscription.this, MainActivity.class));
        finish();
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
                                        .setProductId("month_premium_sub")
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

                        txtPrice.setText(response);
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

    private void mCashPay() {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("tel")) {
            String driverPhoneNumber = sharedPreferences.getString("tel", "");


            AlertDialog.Builder builder = new AlertDialog.Builder(Subscription.this);
            builder.setMessage("You sure you about to make a pay with your account " + driverPhoneNumber + ". This may charge on your account around " + response + ". Do you want to continue?");
            builder.setTitle("mCash Pay");
            Drawable icon = getResources().getDrawable(R.drawable.ic_cause_charges64);
            builder.setIcon(icon);
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                BuildVariant.premium = true;
                mCashSuccess();
            });
            builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                BuildVariant.premium = true;
                mCashCancelled();
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
    }

    private void mCashCancelled() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Subscription.this);
        builder.setMessage("Your purchase has cancelled");
        builder.setTitle("Payment cancelled");
        Drawable icon = getResources().getDrawable(R.drawable.ic_error_cross64);
        builder.setIcon(icon);
        builder.setCancelable(false);

        builder.setPositiveButton("Continue", (DialogInterface.OnClickListener) (dialog, which) -> {

        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void mCashSuccess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Subscription.this);
        builder.setMessage("Congratulations! Now you can access Premium features");
        builder.setTitle("Payment successful");
        Drawable icon = getResources().getDrawable(R.drawable.ic_ok_green_tick96);
        builder.setIcon(icon);
        builder.setCancelable(false);

        builder.setPositiveButton("Continue", (DialogInterface.OnClickListener) (dialog, which) -> {
            startActivity(new Intent(Subscription.this, MainActivity.class));
            finish();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
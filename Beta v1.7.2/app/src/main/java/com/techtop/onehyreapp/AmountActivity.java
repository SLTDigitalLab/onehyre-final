package com.techtop.onehyreapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;

public class AmountActivity extends AppCompatActivity {
    Button btn_update, btn_cancle;
    TextView day_sFee, day_twokmFee, day_waitFee, night_sFee, night_twokmFee, night_waitFee, lastUpdatedOn;
    DatabaseReference ref;
    String vehicle_type, tariff_id;
    String dsv, d2v, dwv, nsv, n2v, nwv;
    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount);
        getSupportActionBar().setTitle("Tariff Values");

        btn_update = findViewById(R.id.updateTripDetails_btn);
        //btn_cancle = findViewById(R.id.cancelTripDetails_btn);

        lastUpdatedOn = findViewById(R.id.txtUpdatedTime);

        day_sFee = findViewById(R.id.day_sFee_txt);
        day_twokmFee = findViewById(R.id.d_twokm_fee);
        day_waitFee = findViewById(R.id.d_wait_fee);

        night_sFee = findViewById(R.id.n_starting_fee);
        night_twokmFee = findViewById(R.id.n_twokm_fee);
        night_waitFee = findViewById(R.id.n_waiting_fee);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.contains("day_sFee")) {
            loaddata();
        }
        if (sharedPreferences.contains("vehicle_type")){
            vehicle_type = sharedPreferences.getString("vehicle_type", "");
            //Toast.makeText(this, vehicle_type, Toast.LENGTH_SHORT).show();
        }
        if (vehicle_type.equals("Cars")){
            tariff_id = "tar_cars";
        }
        if (vehicle_type.equals("Vans")){
            tariff_id = "tar_vans";
        }
        if (vehicle_type.equals("Tuks")){
            tariff_id = "tar_tuks";
        }
        if (vehicle_type.equals("Flex")){
            tariff_id = "tar_flex";
        }
        if (vehicle_type.equals("Minivans")){
            tariff_id = "tar_minivans";
        }
        if (vehicle_type.equals("Mini")){
            tariff_id = "tar_mini";
        }


        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AmountActivity.this, "tariff_id = " + tariff_id, Toast.LENGTH_SHORT).show();
                //status messages
                day_sFee.setText("Please Wait...");
                day_twokmFee.setText("Please Wait...");
                day_waitFee.setText("Please Wait...");
                night_sFee.setText("Please Wait...");
                night_twokmFee.setText("Please Wait...");
                night_waitFee.setText("Please Wait...");

                //getting last updated time
                long date = System.currentTimeMillis();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm:ss a");
                String dateString = dateFormat.format(date);
                lastUpdatedOn.setText(dateString);

                ref = FirebaseDatabase.getInstance().getReference().child("TariffDetails");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot vehicleSnapshot : snapshot.getChildren()) {
                                String vehicleTariffId = vehicleSnapshot.child("tariff_id").getValue(String.class);
                                if (vehicleTariffId.equals(tariff_id)) {
                                    dsv = vehicleSnapshot.child("day_s_fee").getValue(String.class);
                                    d2v = vehicleSnapshot.child("day_2km_fee").getValue(String.class);
                                    dwv = vehicleSnapshot.child("day_w_fee").getValue(String.class);
                                    nsv = vehicleSnapshot.child("night_s_fee").getValue(String.class);
                                    n2v = vehicleSnapshot.child("night_2km_fee").getValue(String.class);
                                    nwv = vehicleSnapshot.child("night_w_fee").getValue(String.class);

                                    // Inserting data into TextView
                                    day_sFee.setText("Rs. "+dsv);
                                    day_twokmFee.setText("Rs. "+d2v);
                                    day_waitFee.setText("Rs. "+dwv);
                                    night_sFee.setText("Rs. "+nsv);
                                    night_twokmFee.setText("Rs. "+n2v);
                                    night_waitFee.setText("Rs. "+nwv);

                                    saveData();
                                    Toast.makeText(AmountActivity.this, "Trip details have been updated! ✔️", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AmountActivity.this, MainActivity.class));
                                    finish();

                                    break; // Exit the loop once the matching tariff_id is found
                                }
                            }
                        } else {
                            Toast.makeText(AmountActivity.this, "TripValues node not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle potential errors or cancellation of the query
                        // ...
                    }
                });

                /*if (TextUtils.isEmpty(day_sFee.getText().toString().trim())
                        || TextUtils.isEmpty(day_twokmFee.getText().toString().trim())
                        || TextUtils.isEmpty(day_waitFee.getText().toString().trim())

                        || TextUtils.isEmpty(night_sFee.getText().toString().trim())
                        || TextUtils.isEmpty(night_twokmFee.getText().toString().trim())
                        || TextUtils.isEmpty(night_waitFee.getText().toString().trim())
                ){
                    Toast.makeText(AmountActivity.this, "Please fill all fields! ", Toast.LENGTH_SHORT).show();
                }else {
                    saveData();
                    Toast.makeText(AmountActivity.this, "Trip details has been updated! ✔️", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AmountActivity.this, MainActivity.class));
                }*/
            }
        });
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("lastUpdatedOn", String.valueOf(lastUpdatedOn.getText()));

        editor.putString("day_sFee", dsv);
        editor.putString("day_twokmFee", d2v);
        editor.putString("day_waitFee", dwv);

        editor.putString("night_sFee", nsv);
        editor.putString("night_twokmFee", n2v);
        editor.putString("night_waitFee", nwv);

        editor.apply();
    }

    public void loaddata(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        lastUpdatedOn.setText(sharedPreferences.getString("lastUpdatedOn", ""));

        day_sFee.setText("Rs. " + sharedPreferences.getString("day_sFee",""));
        day_twokmFee.setText("Rs. " + sharedPreferences.getString("day_twokmFee",""));
        day_waitFee.setText("Rs. " + sharedPreferences.getString("day_waitFee",""));

        night_sFee.setText("Rs. " + sharedPreferences.getString("night_sFee",""));
        night_twokmFee.setText("Rs. " + sharedPreferences.getString("night_twokmFee",""));
        night_waitFee.setText("Rs. " + sharedPreferences.getString("night_waitFee",""));

    }

}
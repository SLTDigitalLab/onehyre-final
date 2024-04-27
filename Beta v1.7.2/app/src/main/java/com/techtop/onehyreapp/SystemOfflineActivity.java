package com.techtop.onehyreapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SystemOfflineActivity extends AppCompatActivity {

    String errMessage, currStatus;
    TextView sysOffNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_offline);
        getSupportActionBar().hide();

        sysOffNote = findViewById(R.id.tvSysDownMsg);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("SystemStatus");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SystemStatus systemStatus = dataSnapshot.getValue(SystemStatus.class);
                if (systemStatus != null) {
                    currStatus = systemStatus.getCurrentStatus();
                    Log.d(currStatus, "System down message is: " + currStatus);

                    if (currStatus.equals("offline")) {
                        errMessage();
                    }
                    else if (currStatus.equals("online")){
                        Intent intent = new Intent(SystemOfflineActivity.this, MainActivity.class);
                        startActivity(intent);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        finish();
                    }
                    else {
                        Intent intent = new Intent(SystemOfflineActivity.this, SystemErrorActivity.class);
                        startActivity(intent);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(String.valueOf(error), "Failed to read value.", error.toException());
            }
        });
    }
    @Override
    public void onBackPressed() {
        // Do nothing to block going back
        Toast.makeText(this, "The app is currently unavailable. Try Again Later.", Toast.LENGTH_SHORT).show();
    }
    public void errMessage() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("SystemStatus");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SystemStatus systemStatus = dataSnapshot.getValue(SystemStatus.class);
                if (systemStatus != null) {
                    errMessage = systemStatus.getErrMsg();
                    sysOffNote.setText(errMessage);
                    Log.d(errMessage, "System down message is: " + errMessage);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(String.valueOf(error), "Failed to read value.", error.toException());
            }
        });
    }
}
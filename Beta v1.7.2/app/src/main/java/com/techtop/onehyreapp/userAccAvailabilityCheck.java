package com.techtop.onehyreapp;

import static android.content.Context.MODE_PRIVATE;
import static com.techtop.onehyreapp.MainActivity.SHARED_PREFS;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class userAccAvailabilityCheck {

    Context context;

    protected void checkUserAccAvailability(String phone, Context context) {
        this.context = context;

        Log.d(phone, "checkUserAccAvailability: " + phone);
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        DatabaseReference nodeRef = FirebaseDatabase.getInstance().getReference().child("Employee");
        Query query = nodeRef.orderByChild("phoneNumber").equalTo(phone);

        // Attach a listener to the query to update the matching child node
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Account not found in Firebase
                    //SharedPreferences.Editor editor = sharedPreferences.edit();
                    //editor.clear();
                    //editor.apply();
                    accAvailabilityData.is_acc_available = false;
                    Toast.makeText(context, "no account", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context, "all good", Toast.LENGTH_SHORT).show();
                    accAvailabilityData.is_acc_available = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error, if any
            }
        });
    }
}

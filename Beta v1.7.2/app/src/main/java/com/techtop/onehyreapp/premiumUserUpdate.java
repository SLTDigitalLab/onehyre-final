package com.techtop.onehyreapp;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class premiumUserUpdate {

    protected void updateUserPremiumStatus(String phone, String premiunStatus) {

            DatabaseReference nodeRef = FirebaseDatabase.getInstance().getReference().child("Employee");
            Query query = nodeRef.orderByChild("phoneNumber").equalTo(phone);

            // Attach a listener to the query to update the matching child node
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Loop through the matching child nodes
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        // Update the data in the child node
                        childSnapshot.getRef().child("accountStatus").setValue(premiunStatus);

                        Employee employee = childSnapshot.getValue(Employee.class);
                        //Toast.makeText(DriverMainActivity.this, "Logged in as " + employee.getName(), Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle the error
                }
            });
    }

    public void addRecordToPremiumUsers(String phone, String billingOrderID, String billingDate) {
        DatabaseReference driverActivityRef = FirebaseDatabase.getInstance().getReference().child("PremiumUsers");
        DatabaseReference phoneNumberRef = driverActivityRef.child(phone);

        // creating a map to hold the data
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("billingDate", billingDate);
        activityData.put("billingID", billingOrderID);

        DatabaseReference statusChangeRef = phoneNumberRef.push();

        statusChangeRef.setValue(activityData);
    }


}

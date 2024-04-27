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

public class userStatusUpdater {

    protected void updateUserStatus(String phone, String currentStatus) {

            DatabaseReference nodeRef = FirebaseDatabase.getInstance().getReference().child("Employee");
            Query query = nodeRef.orderByChild("phoneNumber").equalTo(phone);

            // Attach a listener to the query to update the matching child node
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Loop through the matching child nodes
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        // Update the data in the child node
                        childSnapshot.getRef().child("driverStatus").setValue(currentStatus);

                        Employee employee = childSnapshot.getValue(Employee.class);
                        //Toast.makeText(DriverMainActivity.this, "Logged in as " + employee.getName(), Toast.LENGTH_SHORT).show();

                        addRecordToDriverActivity(phone, currentStatus);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle the error
                }
            });
    }

    private void addRecordToDriverActivity(String phone, String currentStatus) {
        DatabaseReference driverActivityRef = FirebaseDatabase.getInstance().getReference().child("EmployeeActivity");
        DatabaseReference phoneNumberRef = driverActivityRef.child(phone);

        String hash = generateHash(phone + currentStatus + ServerValue.TIMESTAMP);

        // creating a map to hold the data
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("status", currentStatus);
        activityData.put("timestamp", ServerValue.TIMESTAMP);
        activityData.put("ent_id", userDataLocalStore.companyId);

        DatabaseReference statusChangeRef = phoneNumberRef.push();

        statusChangeRef.setValue(activityData);
    }

    private String generateHash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

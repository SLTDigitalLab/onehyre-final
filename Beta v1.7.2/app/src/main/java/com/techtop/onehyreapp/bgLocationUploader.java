package com.techtop.onehyreapp;

import android.os.AsyncTask;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class bgLocationUploader extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
            String driverPhoneSP = params[0];
            String location = params[1];

            DatabaseReference nodeRef = FirebaseDatabase.getInstance().getReference().child("Employee");
            Query query = nodeRef.orderByChild("phoneNumber").equalTo(driverPhoneSP);

            // Attach a listener to the query to update the matching child node
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Loop through the matching child nodes
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        // Update the data in the child node
                        childSnapshot.getRef().child("liveLocation").setValue(location);

                        Employee employee = childSnapshot.getValue(Employee.class);
                        //Toast.makeText(bgLocationUploader.this, "Logged in as " + employee.getName(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle the error
                }
            });
        return null;
    }
}
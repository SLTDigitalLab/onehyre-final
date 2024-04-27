package com.techtop.onehyreapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class TabStats extends Fragment {
    Button save;
    EditText driverName;
    final String phoneNumberToMatch = userDataLocalStore.driverPhone;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_stats, container, false);

        driverName = view.findViewById(R.id.driverNameEditText);
        save = view.findViewById(R.id.btnSaveDriverNameEdits);

        driverName.setHint(userDataLocalStore.driverName);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newDriverName = driverName.getText().toString();
                updateDriverProfile(newDriverName);

                Intent intent = new Intent(getActivity(), SplashScreenActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void updateDriverProfile(String name) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Query query = databaseReference.child("Employee").orderByChild("phoneNumber").equalTo(phoneNumberToMatch);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    userSnapshot.getRef().child("name").setValue(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
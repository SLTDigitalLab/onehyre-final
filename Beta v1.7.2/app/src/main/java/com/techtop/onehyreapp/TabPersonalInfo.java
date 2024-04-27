package com.techtop.onehyreapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TabPersonalInfo extends Fragment {

    TextView dPhone, dNic, dVType, dVModel, dVNo;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_personal_info, container, false);

        // Find TextViews by their IDs
        dPhone = view.findViewById(R.id.txtDPhone);
        dNic = view.findViewById(R.id.txtDId);
        dVType = view.findViewById(R.id.txtDVehicleCategory);
        dVModel = view.findViewById(R.id.txtDVehicleModel);
        dVNo = view.findViewById(R.id.txtDVehicleNo);

        // Set text for TextViews
        dPhone.setText(""+userDataLocalStore.driverPhone);
        dNic.setText(""+userDataLocalStore.nic);
        dVNo.setText(""+userDataLocalStore.vehicleNumber);
        dVType.setText(""+userDataLocalStore.vehicleType);
        dVModel.setText(""+userDataLocalStore.vehicleModel);

        return view;
    }
}
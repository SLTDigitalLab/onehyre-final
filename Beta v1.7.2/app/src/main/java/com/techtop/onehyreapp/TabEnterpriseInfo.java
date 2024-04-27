package com.techtop.onehyreapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TabEnterpriseInfo extends Fragment {

    TextView entName, entId, entHotline, entMail;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_enterprise_info, container, false);

        entName = view.findViewById(R.id.txtDCompanyName);
        entId = view.findViewById(R.id.txtDCompanyId);
        entHotline = view.findViewById(R.id.txtDCompanyPhone);
        entMail = view.findViewById(R.id.txtDCompanyMail);

        entName.setText(userDataLocalStore.companyName);
        entId.setText(userDataLocalStore.companyId);
        entMail.setText(userDataLocalStore.companyMail);
        entHotline.setText(userDataLocalStore.companyPhone);

        return view;
    }
}
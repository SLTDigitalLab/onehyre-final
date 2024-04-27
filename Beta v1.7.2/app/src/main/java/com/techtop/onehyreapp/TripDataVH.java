package com.techtop.onehyreapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;

public class TripDataVH extends RecyclerView.ViewHolder
{
    public TextView t_date,trip_date,fare,waiting_time,hire_time,hire_distance,onekm_fee,twokm_fee,start_time,end_time, wait_fee, vNo, cName, cPhone, cMail, tripId;
    public Group tripDetailsGroup;
    public ImageView imageViewDayOrNight;
    public ConstraintLayout fullTripDetails;
    public TripDataVH(@NonNull View itemView) {
        super(itemView);
        tripId = itemView.findViewById(R.id.txtTripId);
        t_date = itemView.findViewById(R.id.date);
        trip_date = itemView.findViewById(R.id.txt_date);
        fare = itemView.findViewById(R.id.tothiretxt);
        waiting_time = itemView.findViewById(R.id.wating_time);

        hire_time = itemView.findViewById(R.id.hire_time);
        hire_distance = itemView.findViewById(R.id.hire_distance);
        onekm_fee= itemView.findViewById(R.id.onekm_fee);
        twokm_fee = itemView.findViewById(R.id.twokm_fee);
        start_time = itemView.findViewById(R.id.start_time);
        end_time = itemView.findViewById(R.id.end_time);
        wait_fee  =itemView.findViewById(R.id.waiting_fee);

        vNo = itemView.findViewById(R.id.txtVehicleNo);
        cName = itemView.findViewById(R.id.clientName);
        cMail = itemView.findViewById(R.id.clientMail);
        cPhone = itemView.findViewById(R.id.clientPhone);

        fullTripDetails = itemView.findViewById(R.id.fullTripDetails);
        imageViewDayOrNight = itemView.findViewById(R.id.imageViewDayOrNight);
    }
}

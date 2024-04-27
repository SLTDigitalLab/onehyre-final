package com.techtop.onehyreapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<TripData> list = new ArrayList<>();
    private int selectedItemPosition = RecyclerView.NO_POSITION;

    public RVAdapter(Context ctx) {
        this.context = ctx;
        textUpdateHandler = new Handler(Looper.getMainLooper());
        startTextUpdateCycle();
    }

    public void setItems(ArrayList<TripData> td) {
        list.addAll(td);
    }

    public void setSelectedItemPosition(int position) {
        selectedItemPosition = position;
        notifyDataSetChanged();
    }

    private Handler textUpdateHandler;
    private boolean showTripId = true;
    private static final long TEXT_UPDATE_INTERVAL = 7500;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.trip_history_layout, parent, false);
        return new TripDataVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        try {

            TripDataVH vh = (TripDataVH) holder;
            TripData td = list.get(position);

            if (showTripId) {
                vh.tripId.setText("Tap here to see more details");
            } else {
                vh.tripId.setText(td.getTripID());
            }

            vh.trip_date.setText(td.getStartTime());

            dayOrNightTripFinder(vh.imageViewDayOrNight, td.getStartTime());

            vh.t_date.setText(td.getDateTxt());
            vh.end_time.setText("End Time: " + td.getStartTime());
            vh.fare.setText("Total Fare: " + td.getTotFare());
            vh.hire_distance.setText("Distance: " + td.getDistance());
            vh.hire_time.setText("Hire Time: " + td.getTotHireTime());
            vh.waiting_time.setText("Waiting Time: " + td.getWaitTime());
            vh.wait_fee.setText("Waiting Fee (For 1min): " + td.getWaitRate());
            vh.start_time.setText("Start Time: " + td.getStartTime());
            vh.onekm_fee.setText("Fee for 1km: " + td.getOneKmRateTxt());
            vh.twokm_fee.setText("Fee for 2km: " + td.getTwoKmRate());

            vh.vNo.setText("Vehicle Number: " + td.getPhoneTxt());
            vh.cName.setText("Client Name: " + td.getClientName());
            vh.cMail.setText("Customer e-mail: " + td.getClientMail());
            vh.cPhone.setText("Customer Telephone: " + td.getClientPhone());

            boolean isExpanded = list.get(position).isCardExpanded();
            vh.fullTripDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            vh.tripId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TripData td = list.get(vh.getAdapterPosition());
                    td.setCardExpanded(!td.isCardExpanded());
                    notifyItemChanged(vh.getAdapterPosition());
                }
            });

        } catch (Exception e) {
            Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void dayOrNightTripFinder(ImageView imageView, String tripStartTime) {
        // make sure the format = hh.mm a
        SimpleDateFormat sdf = new SimpleDateFormat("hh.mm a", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        Date tripStartTimeDate = null;
        try {
            tripStartTimeDate = sdf.parse(tripStartTime);
            if (tripStartTimeDate != null) {
                calendar.setTime(tripStartTimeDate);
                int tripHour = calendar.get(Calendar.HOUR_OF_DAY);

                // Define the daytime range (6:00 AM to 8:00 PM)
                int daytimeStartHour = 6;
                int daytimeEndHour = 20;

                // Check if tripStartTime is within the daytime range
                boolean isDaytime = tripHour >= daytimeStartHour && tripHour < daytimeEndHour;

                if (isDaytime) {
                    imageView.setImageResource(R.drawable.ic_day_sun_cloud96dp);
                } else {
                    imageView.setImageResource(R.drawable.ic_night_moon_cloud96px);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private void startTextUpdateCycle() {
        textUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Toggle between showing "Tap to see more" and the actual tripId
                showTripId = !showTripId;
                notifyDataSetChanged();

                // Schedule the next update
                textUpdateHandler.postDelayed(this, TEXT_UPDATE_INTERVAL);
            }
        }, TEXT_UPDATE_INTERVAL);
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }
}
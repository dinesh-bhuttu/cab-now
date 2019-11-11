package com.example.drivercabnow.Adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;

import com.example.drivercabnow.MapActivities.DriverWelcomeActivity;
import com.example.drivercabnow.Models.Ride;
import com.example.drivercabnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<Ride> dataSet;
    private String custName;
    private static final String TAG="CustomAdapter";
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users, rides, drivers;
    String rider;
    Context context;


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView customerName, customerLocation, distance, paymentType, destination;
        Button acceptRide, rejectRide;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.customerName =  itemView.findViewById(R.id.customerName);
            this.customerLocation =  itemView.findViewById(R.id.customerLocation);
            this.distance =  itemView.findViewById(R.id.distance);
            this.paymentType =  itemView.findViewById(R.id.paymentType);
            this.destination =  itemView.findViewById(R.id.destination);
        }
    }

    public CustomAdapter(ArrayList<Ride> data) {
        this.dataSet = data;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);
        context = view.getContext();
        return new MyViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        rider = dataSet.get(listPosition).getRider();
        users.child(rider).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    i+=1;
                    if(i==4) {
                        custName = dataSnapshot1.getValue(String.class);
                        holder.customerName.setText(custName);
                        holder.customerName.setTextColor(ColorStateList.valueOf(R.color.splashColor));
                        Log.d(TAG,"Customer name issssssssssssssssssssssssssssssss "+custName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String destloc=null;
        String srcloc=null;
        String destlat, destlng;
        destlat = dataSet.get(listPosition).getDestination().getLat();
        destlng = dataSet.get(listPosition).getDestination().getLng();
        Log.d(TAG, destlat+","+destlng);
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(Double.parseDouble(destlat), Double.parseDouble(destlng),1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list != null & list.size() > 0) {
            Address address = list.get(0);
            destloc = address.getAddressLine(0);
        }
        Log.d(TAG, destloc);

        destlat = dataSet.get(listPosition).getSource().getLat();
        destlng = dataSet.get(listPosition).getSource().getLng();
        Log.d(TAG, destlat+","+destlng);
        try {
            list = geocoder.getFromLocation(Double.parseDouble(destlat), Double.parseDouble(destlng),1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list != null & list.size() > 0) {
            Address address = list.get(0);
            srcloc = address.getAddressLine(0);
        }
        Log.d(TAG, srcloc);
        holder.destination.setText(destloc);
        holder.destination.setTextColor(ColorStateList.valueOf(R.color.splashColor));
        holder.customerLocation.setText(srcloc);
        holder.customerLocation.setTextColor(ColorStateList.valueOf(R.color.splashColor));
        holder.paymentType.setText(dataSet.get(listPosition).getPayment());
        holder.paymentType.setTextColor(ColorStateList.valueOf(R.color.splashColor));
        holder.distance.setText(dataSet.get(listPosition).getDistance());
        holder.distance.setTextColor(ColorStateList.valueOf(R.color.splashColor));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

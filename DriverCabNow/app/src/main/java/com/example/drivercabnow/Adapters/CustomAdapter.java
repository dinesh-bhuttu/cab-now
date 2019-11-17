package com.example.drivercabnow.Adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.Toast;
import com.example.drivercabnow.MapActivities.DriverWelcomeActivity;
import com.example.drivercabnow.MapActivities.TravelActivity;
import com.example.drivercabnow.Models.Latlng;
import com.example.drivercabnow.Models.Ride;
import com.example.drivercabnow.Models.User;
import com.example.drivercabnow.R;
import com.example.drivercabnow.RecycleActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private int i=0;
    private ArrayList<Ride> dataSet;
    private String custName;
    private static final String TAG="CustomAdapter";
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users, rides, drivers;
    String rider;
    Context context;
    Latlng driverlatlng;

    Double average_rating;
    String rider_name, rider_phone, rider_email, vehicle;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView customerName, customerLocation, distance, paymentType, destination;
        Button acceptRide, rejectRide;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.acceptRide = itemView.findViewById(R.id.acceptRide);
            this.customerName =  itemView.findViewById(R.id.customerName);
            this.customerLocation =  itemView.findViewById(R.id.customerLocation);
            this.distance =  itemView.findViewById(R.id.distance);
            this.paymentType =  itemView.findViewById(R.id.paymentType);
            this.destination =  itemView.findViewById(R.id.destination);
        }
    }

    public CustomAdapter(Context context1, ArrayList<Ride> data) {
        this.dataSet = data;
        this.context = context1;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        drivers = db.getReference("Drivers");
        drivers.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                        if(dataSnapshot1.getKey().equalsIgnoreCase("source")){
                            driverlatlng = dataSnapshot1.getValue(Latlng.class);
                        }
                        else if(dataSnapshot1.getKey().equalsIgnoreCase("vehicle_no")){
                            vehicle = dataSnapshot1.getValue(String.class);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);
        context = view.getContext();
        return new MyViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        rider = dataSet.get(listPosition).getRider();
        users.child(rider).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()) {
                        String key = dataSnapshot1.getKey();
                        switch (key) {
                            case "average_Rating":
                                i+=1;
                                Toast.makeText(context, "i is "+i, Toast.LENGTH_LONG);
                                average_rating = dataSnapshot1.getValue(Double.class);
                                break;
                            case "rider_email":
                                i+=1;
                                Toast.makeText(context, "i is "+i, Toast.LENGTH_LONG);
                                rider_email = dataSnapshot1.getValue(String.class);
                                break;
                            case "rider_name":
                                i+=1;
                                Toast.makeText(context, "i is "+i, Toast.LENGTH_LONG);
                                rider_name = dataSnapshot1.getValue(String.class);
                                break;
                            case "rider_phone":
                                i+=1;
                                Toast.makeText(context, "i is "+i, Toast.LENGTH_LONG);
                                rider_phone = dataSnapshot1.getValue(String.class);
                                break;
                        }
                    }
                    holder.customerName.setText(rider_name);
                    holder.customerName.setTextColor(ColorStateList.valueOf(R.color.splashColor));
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
        if (list != null && list.size() > 0) {
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
        assert list != null;
        if (list.size() > 0) {
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



        // On click listeners for accept
        holder.acceptRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rides = FirebaseDatabase.getInstance().getReference("Rides");
                rides.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            Ride ride = dataSnapshot1.getValue(Ride.class);
                            Log.e(TAG, "KKKKEEEEYYYYY - " + dataSnapshot1.getKey());
                            Log.d(TAG, "MESSSSAAAGGGEEE - "+i);
                            assert ride != null;
                            if(ride.getRider().equals(rider) && ride.getStatus().equalsIgnoreCase("Searching")){
                                rides.child(dataSnapshot1.getKey()).child("status").setValue("Accepted");
                                rides.child(dataSnapshot1.getKey()).child("driver").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                Intent n = new Intent(context, TravelActivity.class);
                                n.putExtra("UserName",rider_name);
                                n.putExtra("UserPhone", rider_phone);
                                n.putExtra("UserRating", average_rating.toString());
                                n.putExtra("StartLocationLat", ride.getSource().getLat());
                                n.putExtra("StartLocationLng", ride.getSource().getLng());
                                n.putExtra("EndLocationLat", ride.getDestination().getLat());
                                n.putExtra("EndLocationLng", ride.getDestination().getLng());
                                n.putExtra("Price", ride.getPrice());
                                n.putExtra("DriverLocationLat",driverlatlng.getLat());
                                n.putExtra("DriverLocationLng", driverlatlng.getLng());
                                n.putExtra("Vehicle", vehicle);
                                n.putExtra("RideId", dataSnapshot1.getKey());
                                Toast.makeText(context, "PUT ALL EXTRAS", Toast.LENGTH_LONG);
                                context.startActivity(n);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

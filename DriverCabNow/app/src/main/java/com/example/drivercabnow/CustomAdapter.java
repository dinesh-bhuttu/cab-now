package com.example.drivercabnow;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.drivercabnow.Models.Ride;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<Ride> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView customerName, customerLocation, distance, paymentType, destination;
        Button acceptRide, rejectRide;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.customerName =  itemView.findViewById(R.id.customerLocation);
            this.customerLocation =  itemView.findViewById(R.id.customerName);
            this.distance =  itemView.findViewById(R.id.paymentType);
            this.paymentType =  itemView.findViewById(R.id.distance);
            this.destination =  itemView.findViewById(R.id.destination);
        }
    }

    public CustomAdapter(ArrayList<Ride> data) {
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView customerName = holder.customerName;
        TextView customerLocation = holder.customerLocation;
        TextView paymentType = holder.paymentType;
        TextView distance = holder.distance;

        customerName.setText("Rider - Dinesh");
        customerLocation.setText("Location - "+dataSet.get(listPosition).getSource().getLat()+","+dataSet.get(listPosition).getSource().getLng());
        paymentType.setText("Payment method - "+dataSet.get(listPosition).getPayment());
        distance.setText("Distance - "+dataSet.get(listPosition).getDistance());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

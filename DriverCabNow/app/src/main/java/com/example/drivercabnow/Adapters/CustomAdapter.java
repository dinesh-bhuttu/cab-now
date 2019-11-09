package com.example.drivercabnow.Adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.drivercabnow.Models.Ride;
import com.example.drivercabnow.R;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<Ride> dataSet;
    private static final String TAG="CustomAdapter";

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
        TextView destination = holder.destination;

        customerName.setText("Rider - "+dataSet.get(listPosition).getRider());
        destination.setText("Destination - "+dataSet.get(listPosition).getDestination().getLat()+","+dataSet.get(listPosition).getDestination().getLng());
        customerLocation.setText("Location - "+dataSet.get(listPosition).getSource().getLat()+","+dataSet.get(listPosition).getSource().getLng());
        paymentType.setText("Payment method - "+dataSet.get(listPosition).getPayment());
        distance.setText("Distance - "+dataSet.get(listPosition).getDistance());

        Log.d(TAG, "Customer : "+customerName.getText());
        Log.d(TAG, "Customer Location :"+customerLocation.getText());
        Log.d(TAG, "Payment Type : "+ paymentType.getText());
        Log.d(TAG, "Distance in kms :"+distance.getText());
        Log.d(TAG, "Destination :"+destination.getText());    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

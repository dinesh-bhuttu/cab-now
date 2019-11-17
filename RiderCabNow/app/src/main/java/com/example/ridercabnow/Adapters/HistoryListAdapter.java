package com.example.ridercabnow.Adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridercabnow.R;
import com.example.ridercabnow.models.HistoryItem;
import com.example.ridercabnow.models.Latlng;
import com.example.ridercabnow.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.ViewHolder> {

    private ArrayList<HistoryItem> list;
    private Context ctx;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView rideType, ridePrice, driverName, rating, dest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rideType = itemView.findViewById(R.id.taxi);
            ridePrice = itemView.findViewById(R.id.tvHistPrice);
            driverName = itemView.findViewById(R.id.tvHistName);
            rating = itemView.findViewById(R.id.tvHistRating);
            dest = itemView.findViewById(R.id.tvHistDest);
        }
    }

    public HistoryListAdapter(ArrayList<HistoryItem> list, Context ctx) {
        this.list = list;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list_item,
                parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem currentItem = list.get(position);

        holder.rideType.setText(currentItem.getVehicle());
        holder.ridePrice.setText("-" + String.valueOf(currentItem.getPrice()));
        holder.driverName.setText(currentItem.getDriverName());
        holder.rating.setText(String.valueOf(currentItem.getRating_to_driver()));

        //String title = new Constants().getLocationTitle(l.getLat(), l.getLng());
        Latlng l = currentItem.getDestination();
        String title = getAddress(Double.parseDouble(l.getLat()), Double.parseDouble(l.getLng()));
        holder.dest.setText(title);
    }

    private String getAddress(double lat, double lng) {
        Geocoder coder = new Geocoder(ctx, Locale.getDefault());
        List<Address> list = new ArrayList<>();
        Address address = null;

        try {
            list =  coder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        address = list.get(0);

        if (address != null) {
            return address.getAddressLine(0);
        }
        return "No title found";
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

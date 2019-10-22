package com.example.drivercabnow.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.drivercabnow.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RideListAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] rides;
    private Float[] prices;
    private int[] images;

    public RideListAdapter(Context context, String[] rides, int[] images, Float[] prices) {
        super(context, R.layout.list_ride_item, R.id.rideType, rides);
        this.context = context;
        this.rides = rides;
        this.prices = prices;
        this.images = images;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Only for 3 list items, ViewHolder not needed here
        View row = layoutInflater.inflate(R.layout.list_ride_item, parent, false);

        ImageView image = row.findViewById(R.id.rideImage);
        TextView ride = row.findViewById(R.id.rideType);
        TextView price = row.findViewById(R.id.ridePrice);

        image.setImageResource(images[position]);
        ride.setText(rides[position]);
        price.setText(String.valueOf(prices[position]));

        return row;
    }
}

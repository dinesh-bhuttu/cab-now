package com.example.ridercabnow.MapActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.ridercabnow.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ChooseRideActivity extends AppCompatActivity {

    private static final String TAG = "ChooseRideActivity";
    MarkerOptions place1, place2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_ride);

        // Gets source and dest MarkerOptions from previous activity
        getIntentInfo();

        // UI
        // TODO (1) Create slidingPanel for UI [Ride selection]
        // TODO (2) Create custom list layout for slidingPanel ride selection
        //          Custom list should have
        //          -> Ride name
        //          -> Ride picture
        //          -> Ride price estimate [implement pricing policy crap]

        // Pre work
        // TODO (3) init onMapReady, FusedLocationProviderClient and draw marker options
        // TODO (4) Integrate directions API from previous activity to draw PolyLine

        // Requirement
        // TODO (5) onClick of any ride picture from slidingPanel
        //          -> should 'ADD' Ride to DB
        //          -> should show a custom AlertDialog with progressDialog, cancelRide

        // Post Driver app completion integrate this
        // TODO (6) When driver accepts show his location and draw a PolyLine

        // Post ride completion
        // TODO (7) Integrate payment and all that crap...

    }

    private void getIntentInfo() {
        Intent i = getIntent();
        String[] p1 = i.getStringArrayExtra("place1");
        String[] p2 = i.getStringArrayExtra("place2");

        LatLng l1 = new LatLng(Double.parseDouble(p1[0]), Double.parseDouble(p1[1]));
        LatLng l2 = new LatLng(Double.parseDouble(p2[0]), Double.parseDouble(p2[1]));

        place1 = new MarkerOptions().position(l1);
        place2 = new MarkerOptions().position(l2);

        Log.d(TAG, "getIntentInfo: " + l1 + " " + l2);
    }

}

package com.example.ridercabnow.MapActivities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ridercabnow.Adapters.RideListAdapter;
import com.example.ridercabnow.ProfileActivity;
import com.example.ridercabnow.R;
import com.example.ridercabnow.directionhelpers.FetchURL;
import com.example.ridercabnow.directionhelpers.TaskLoadedCallback;
import com.example.ridercabnow.utils.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;


public class ChooseRideActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private static final String TAG = "ChooseRideActivity";
    private MarkerOptions place1, place2;

    // Directions
    private GoogleMap mMap;
    private Polyline currentPolyline;

    // Location
    private FusedLocationProviderClient mFusedLocationClient;
    private static final float DEFAULT_ZOOM = 16f;

    private String[] mRides = new String[] {"Auto", "Micro", "Sedan"};
    private Float[] mPrices = new Float[] {0f,0f,0f};
    private int[] mImages = new int[] {R.drawable.auto, R.drawable.micro, R.drawable.sedan};

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready " + googleMap.toString());

        mMap = googleMap;

        // 2 markers and saved polyline from previous activity
        mMap.addMarker(place1);
        mMap.addMarker(place2);
        if(Constants.savedPolylineOptions != null) {
            Log.d(TAG, "onMapReady: drawing saved polyline" + Constants.savedPolylineOptions);
            mMap.addPolyline(Constants.savedPolylineOptions);
        }
        else {
            drawPolyLine();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place1.getPosition(), DEFAULT_ZOOM));

        mMap.setMyLocationEnabled(true);

    }

    private void drawPolyLine() {
        // Get directions
        new FetchURL(ChooseRideActivity.this)
                .execute(getUrl(place1.getPosition(),
                        place2.getPosition(),
                        "driving"),
                        "driving");
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_ride);

        // Gets source and dest MarkerOptions from previous activity
        getIntentInfo();


        // PERMISSIONS
        // TODO (1) Take location and storage permission in login activity
        // TODO (2) Change Rating bar in profile to be READONLY !

        // UI
        // DONE (1) Create slidingPanel for UI [Ride selection]
        // DONE (2) Create custom list layout for slidingPanel ride selection
        // DONE         Custom list should have
        //          -> Ride name
        //          -> Ride picture
        // TODO     -> Ride price estimate [implement pricing policy crap]

        // Pre work
        // DONE (3) init onMapReady, FusedLocationProviderClient and draw marker options
        // DONE (4) Integrate directions API draw PolyLine from place1 to place2

        // Requirement
        // TODO (5) onClick of any ride picture from slidingPanel
        //          -> should 'ADD' Ride to DB
        //          -> should show a custom AlertDialog with progressDialog, cancelRide

        // Post Driver app completion integrate this
        // TODO (6) When driver accepts show his location and draw a PolyLine

        // Post ride completion
        // TODO (7) Integrate payment and all that crap...

        // Get toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Directions");
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            Log.d(TAG, "onCreate: " + "Not null");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            // back button pressed
            Log.d(TAG, "onCreate: " + "Back pressed");
            finish();
        });

        // init map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        // umano slidingupPanel
        ListView listView = findViewById(R.id.listView);
        RideListAdapter adapter = new RideListAdapter(this, mRides, mImages, mPrices);
        listView.setAdapter(adapter);

        // Default value of all prices is 0f
        // After pricing policy, estimate new prices and change mPrices[]
        // call notifyDataSetChanged() on adapter to update prices in list
        // testing update values
        mPrices[0] = 1f;
        mPrices[1] = 2f;
        mPrices[2] = 3f;
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            switch(i) {
                case 0:
                    // TODO Book auto
                    Toast.makeText(ChooseRideActivity.this, "Auto", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    // TODO Book micro
                    Toast.makeText(ChooseRideActivity.this, "Micro", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    // TODO Book sedan
                    Toast.makeText(ChooseRideActivity.this, "Sedan", Toast.LENGTH_SHORT).show();
                    break;
            }
        });


    }

    private void getLastLocation() {
        Task<Location> task = mFusedLocationClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if(location != null)
            {
                // Get Map fragment here
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapNearBy);

                if (mapFragment != null) {
                    mapFragment.getMapAsync(ChooseRideActivity.this);

                }
            }
            else
            {
                Toast.makeText(ChooseRideActivity.this, "Location not found", Toast.LENGTH_LONG).show();
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_options_rider, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menuProfile:
                Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this , ProfileActivity.class));
                return true;

            case R.id.menuHistory:
                // TODO show history activity out of app
                Toast.makeText(this, "History selected", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.menuLogout:
                // TODO 1) logout out of the app
                // TODO 2) [optional] delete stored shared pref variables for new login info
                Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + Constants.GOOGLE_MAPS_API_KEY;
    }


}

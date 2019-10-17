package com.example.ridercabnow.MapActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.ridercabnow.ProfileActivity;
import com.example.ridercabnow.R;
import com.example.ridercabnow.utils.Constants;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.example.ridercabnow.directionhelpers.FetchURL;
import com.example.ridercabnow.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

public class WelcomeActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    // Directions
    private GoogleMap mMap;
    private MarkerOptions place1, place2;
    Button getDirection;
    private Polyline currentPolyline;

    // Places
    private static final String TAG = "MainActivity";
    private final String API_KEY = Constants.GOOGLE_MAPS_API_KEY;
    private PlacesClient placesClient;

    // Location
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    private static final float DEFAULT_ZOOM = 16f;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready " + googleMap.toString());

        mMap = googleMap;

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        mMap.setMyLocationEnabled(true);

        //mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "onCreate: Started");

        // Get toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get current location and move camera to it
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        init_places();

        // init PLACES_API fragment
        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.autocompleteFragment);

        assert autocompleteSupportFragment != null;
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.NAME,Place.Field.LAT_LNG));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // LatLng of suggested Place by API
                LatLng latLng = place.getLatLng();
                if (latLng != null) {

                    // Remove old place1 and place2 marker if they exist
                    mMap.clear();

                    // Using onclick LatLng to create destination marker and move camera
                    place2 = new MarkerOptions().position(latLng).title("Destination");
                    mMap.addMarker(place2);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                    // Using currentLocation create a marker
                    place1 = new MarkerOptions().position(new LatLng(currentLocation.getLatitude(),
                            currentLocation.getLongitude()))
                            .title("Current Location");
                    mMap.addMarker(place1);

                    // Get directions
                    getDirection = findViewById(R.id.btnGetDirection);
                    getDirection.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new FetchURL(WelcomeActivity.this)
                                    .execute(getUrl(place1.getPosition(),
                                            place2.getPosition(), "driving"), "driving");
                        }
                    });
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_LONG).show();
            }
        });


    }

    private void getLastLocation() {
        Task<Location> task = mFusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null)
                {
                    Log.d(TAG, "onSuccess: Current location at " + "Latitude : " + location.getLatitude() + " Longitude : " + location.getLongitude());
                    currentLocation = location;

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapNearBy);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(WelcomeActivity.this);
                    }
                }
                else
                {
                    Toast.makeText(WelcomeActivity.this, "Location not found", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void init_places() {
        if(!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }

        placesClient = Places.createClient(this);
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
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
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + Constants.GOOGLE_MAPS_API_KEY;
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
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
                // TODO intent to profile activity
                Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this , ProfileActivity.class));
                return true;

            case R.id.menuHistory:
                // TODO show history activity out of app
                Toast.makeText(this, "History selected", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.menuLogout:
                // TODO 1) logout out of the app
                // TODO 2) delete stored shared pref variables for new login info
                Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
package com.example.ridercabnow.MapActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridercabnow.ProfileActivity;
import com.example.ridercabnow.R;
import com.example.ridercabnow.RiderAuth.MainActivity;
import com.example.ridercabnow.directionhelpers.FetchURL;
import com.example.ridercabnow.directionhelpers.TaskLoadedCallback;
import com.example.ridercabnow.models.Driver;
import com.example.ridercabnow.models.Latlng;
import com.example.ridercabnow.models.Ride;
import com.example.ridercabnow.utils.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TravelActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private static final String TAG = "TravelActivity";
    private MarkerOptions place1, place2;

    // Directions
    private GoogleMap mMap;
    private Polyline currentPolyline;
    private Double driverLat, driverLng;

    // Location
    private FusedLocationProviderClient mFusedLocationClient;

    // Firebase
    String rid = "";
    String driverId = "";

    // widgets
    TextView rideType, rating, driverName, vehicleNo;
    Button btnCancelRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        getIntentInfo();

        // Get toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Directions");
        setSupportActionBar(toolbar);

        // init map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        // fill out driver details section & get driver location in global vars
        fillDriverDetails();

        // TODO listen to status change for "Arrived"

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready " + googleMap.toString());

        mMap = googleMap;

        // 2 markers and saved polyline from previous activity
        mMap.addMarker(place1).setTitle("Pickup point");
        mMap.addMarker(place2).setTitle("Destination");
        if(Constants.savedPolylineOptions != null) {
            Log.d(TAG, "onMapReady: drawing saved polyline" + Constants.savedPolylineOptions);
            mMap.addPolyline(Constants.savedPolylineOptions);
        }
        else {
            drawPolyLine();
        }

        CameraUpdate c2 = CameraUpdateFactory.newLatLngZoom(new LatLng(driverLat, driverLng),
                15.7f);
        mMap.animateCamera(c2);
        mMap.setMyLocationEnabled(true);

        // mark driver
        Log.d(TAG, "onMapReady: driver loc ->> " + driverLat + " " + driverLng);
        if(driverLat != null && driverLng != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(driverLat, driverLng)))
                    .setTitle(driverName.getText().toString());
            // draw line
            drawPolyLineToDriver();
        }

    }

    private void drawPolyLineToDriver() {
        // Get directions to driver
        new FetchURL(TravelActivity.this)
                .execute(getUrl(place1.getPosition(),
                        new LatLng(driverLat, driverLng),
                        "driving"),
                        "driving");
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
                    mapFragment.getMapAsync(TravelActivity.this);

                }
            }
            else
            {
                Toast.makeText(TravelActivity.this, "Location not found", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void drawPolyLine() {
        // Get directions
        new FetchURL(TravelActivity.this)
                .execute(getUrl(place1.getPosition(),
                        place2.getPosition(),
                        "driving"),
                        "driving");
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
                startActivity(new Intent(this , ProfileActivity.class));
                return true;

            case R.id.menuHistory:
                // TODO show history activity out of app
                Toast.makeText(this, "History selected", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.menuLogout:
                Intent logoutintent = new Intent(this, MainActivity.class);
                SharedPreferences sharedPreferences;
                sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("email", "");
                editor.putString("password","");
                editor.apply();
                Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
                startActivity(logoutintent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
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

    private void getIntentInfo() {
        Intent i = getIntent();
        String[] p1 = i.getStringArrayExtra("place1");
        String[] p2 = i.getStringArrayExtra("place2");
        rid = i.getStringExtra("rid");
        driverId = i.getStringExtra("dId");

        LatLng l1 = new LatLng(Double.parseDouble(p1[0]), Double.parseDouble(p1[1]));
        LatLng l2 = new LatLng(Double.parseDouble(p2[0]), Double.parseDouble(p2[1]));

        place1 = new MarkerOptions().position(l1);
        place2 = new MarkerOptions().position(l2);

        Log.d(TAG, "getIntentInfo: " + l1 + " " + l2);
    }


    private void fillDriverDetails() {
        rideType = findViewById(R.id.tvRideType);
        rating = findViewById(R.id.tvRating);
        driverName = findViewById(R.id.tvDriverName);
        vehicleNo = findViewById(R.id.tvVehicleNo);
        btnCancelRide = findViewById(R.id.btnCancelRide);

        DatabaseReference driverRef = FirebaseDatabase.getInstance()
                .getReference("Drivers")
                .child(driverId);
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: snapshot ->> " + dataSnapshot.getValue());

                Driver driver = dataSnapshot.getValue(Driver.class);
                if(driver != null) {
                    Toast.makeText(TravelActivity.this,
                            driver.getDriver_name() + " has accepted", Toast.LENGTH_LONG).show();

                    // driver details
                    rideType.setText(driver.getCab_type());
                    rating.setText(String.valueOf(driver.getAverage_rating()));
                    driverName.setText(driver.getDriver_name());
                    vehicleNo.setText(driver.getVehicle_no());

                    // driver global location vars
                    Latlng l = driver.getSource();
                    driverLat = Double.parseDouble(l.getLat());
                    driverLng = Double.parseDouble(l.getLng());
                    Log.d(TAG, "onDataChange: Driver values ->> " + driverLat + " " + driverLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Error : " + databaseError.getMessage());
                Toast.makeText(TravelActivity.this, databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}

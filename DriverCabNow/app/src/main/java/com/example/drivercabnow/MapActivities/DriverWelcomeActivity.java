package com.example.drivercabnow.MapActivities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drivercabnow.Models.Latlng;
import com.example.drivercabnow.Models.Ride;
import com.example.drivercabnow.ProfileActivity;
import com.example.drivercabnow.R;
import com.example.drivercabnow.MainActivity;
import com.example.drivercabnow.Models.Driver;
import com.example.drivercabnow.RecycleActivity;
import com.example.drivercabnow.SplashActivity;
import com.example.drivercabnow.Utils.Constants;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.example.drivercabnow.DirectionHelpers.FetchURL;
import com.example.drivercabnow.DirectionHelpers.TaskLoadedCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DriverWelcomeActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    // Directions
    private GoogleMap mMap;
    public static int type = 0;
    private int count = 0;
    private int times = 0;
    public static LatLng myLocation;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Polyline currentPolyline;

    private String type1;

    // Places
    private static final String TAG = "WelcomeActivity";
    private final String API_KEY = Constants.GOOGLE_MAPS_API_KEY;
    private PlacesClient placesClient;


    // Location
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    private static final float DEFAULT_ZOOM = 16f;
    private int i = 0;


    // Intent values
    public static String[] p1, p2;

    // Firebase
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference rides;
    DatabaseReference drivers;

    private ValueEventListener x;

    DatabaseReference driverid;

    int drawable = -1;

    private AlertDialog waitingDialog;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready " + googleMap.toString());

        // Get current driver and his drawable

        String currentDriverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "CCCUUUERRREEENNTTT  UUUIIIDDD - " + currentDriverID);


        x = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This callback will fire even if the node doesn't exist, so now check for existence
                if (dataSnapshot.exists()) {
                    Log.e(TAG, "ONDATACHANGE CALLLLLLLLEEEEEEEEDDDDDD");
                    i = 1;
                    type1 = dataSnapshot.getValue(String.class);
                    switch (type1) {
                        case "Auto":
                            type = 1;
                            drawable = R.drawable.auto2;
                            break;
                        case "Sedan":
                            type = 2;
                            drawable = R.drawable.sedan;
                            break;
                        case "Micro":
                            type = 3;
                            drawable = R.drawable.micro;
                            break;
                    }
                    mMap = googleMap;

                    mMap.clear();
                    waitingDialog.dismiss();
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    myLocation = latLng;
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                    Log.e(TAG, "LLLLOOOOCCCAAATTTIIIOOONNN - " + latLng.latitude + "," + latLng.longitude);
                    //mMap.setMyLocationEnabled(true);
                    mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(
                            createCustomMarker(DriverWelcomeActivity.this, drawable, "")))).
                            setTitle("Me");


                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    //the include method will calculate the min and max bound.
                    builder.include(latLng);

                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                    //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                    //mMap.setPadding(10, 10, 10, 10);
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    mMap.animateCamera(cu);
                    Log.d(TAG, "MIN ZOOM LEVEL - " + mMap.getMinZoomLevel());
                    Log.d(TAG, "MAX ZOOM LEVEL - " + mMap.getMaxZoomLevel());

                    drivers.child(currentDriverID).child("cab_type").removeEventListener(x);

                } else {
                    //System.out.println("The node does not exist.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        };
        drivers.child(currentDriverID).child("cab_type").addListenerForSingleValueEvent(x);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_home);

        Log.e(TAG, "ONCREATE ONGOINGGGGGG");
        checkLocationPermission();
        Log.d(TAG, "onCreate: Started");


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Directions");
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        rides = db.getReference("Rides");
        drivers = db.getReference("Drivers");

        // Get current location and move camera to it
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        waitingDialog = new SpotsDialog.Builder().setContext(DriverWelcomeActivity.this).build();
        waitingDialog.setTitle("Looking for customers nearby...");
        waitingDialog.show();

        checkRides();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void checkRides() {
        rides.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("Driver Count ", "" + dataSnapshot.getChildrenCount());
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Ride ride = dataSnapshot1.getValue(Ride.class);
                    assert ride != null;
                    if (ride.getStatus().equalsIgnoreCase("Searching") && ride.getVehicle().equalsIgnoreCase(type1)) {
                        if (count == 0) {
                            count = 1;
                            // TODO - Display splash screen and listview - with user, source and destination details
                            Log.d(TAG, "Starting splashactivity");
                            startActivity(new Intent(DriverWelcomeActivity.this, SplashActivity.class));
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }

    private void getLastLocation() {
        Task<Location> task = mFusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d(TAG, "onSuccess: Current location at " + "Latitude : " + location.getLatitude() + " Longitude : " + location.getLongitude());
                    Latlng x = new Latlng(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
                    drivers.child(FirebaseAuth.getInstance().getUid()).child("source").setValue(x);
                    Toast.makeText(getBaseContext(), "UPDATED DRIVER LOCATION", Toast.LENGTH_LONG);
                    currentLocation = location;

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(DriverWelcomeActivity.this);
                    }
                } else {
                    Toast.makeText(DriverWelcomeActivity.this, "Location not found", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void init_places() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }

        placesClient = Places.createClient(this);
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
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + Constants.GOOGLE_MAPS_API_KEY;
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();

        // save this polyline for next activity
        Constants.savedPolylineOptions = (PolylineOptions) values[0];
        currentPolyline = mMap.addPolyline(Constants.savedPolylineOptions);
    }

    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker, null);

        CircleImageView markerImage = marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);
        TextView txt_name = marker.findViewById(R.id.name);
        txt_name.setText(_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

    public void checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                new AlertDialog.Builder(this)
//                        .setTitle(R.string.title_location_permission)
//                        .setMessage(R.string.text_location_permission)
//                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                //Prompt the user once explanation has been shown
//                                ActivityCompat.requestPermissions(DriverWelcomeActivity.this,
//                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                        MY_PERMISSIONS_REQUEST_LOCATION);
//                            }
//                        })
//                        .setNegativeButton(R.string.notok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.dismiss();
//                            }
//                        })
//                        .create()
//                        .show();
//
//
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        MY_PERMISSIONS_REQUEST_LOCATION);
//            }
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // location-related task you need to do.
//                    if (ContextCompat.checkSelfPermission(this,
//                            Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED) {
//                        Log.d(TAG, "Permissions Granted!!!!!");
//                        Intent i = new Intent(DriverWelcomeActivity.this, DriverWelcomeActivity.class);
//                        finish();
//                        overridePendingTransition(0, 0);
//                        startActivity(i);
//                        overridePendingTransition(0, 0);
//                    }
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//
//                }
//                return;
//            }
//
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_options_driver, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuProfile:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;

            case R.id.menuHistory:
                // TODO show history activity out of app
                Toast.makeText(this, "History selected", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menuNotifications:
                Toast.makeText(this, "Notifications selected", Toast.LENGTH_SHORT).show();
                Intent notifIntent = new Intent(this, RecycleActivity.class);
                startActivity(notifIntent);
                return true;


            case R.id.menuLogout:
                Intent logoutintent = new Intent(this, MainActivity.class);
                SharedPreferences sharedPreferences;
                sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("email", "");
                editor.putString("password", "");
                editor.apply();
                Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
                startActivity(logoutintent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
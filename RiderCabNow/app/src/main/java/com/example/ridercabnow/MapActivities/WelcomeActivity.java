package com.example.ridercabnow.MapActivities;

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


import com.example.ridercabnow.ProfileActivity;
import com.example.ridercabnow.R;
import com.example.ridercabnow.RiderAuth.MainActivity;
import com.example.ridercabnow.models.Driver;
import com.example.ridercabnow.utils.Constants;
import com.example.ridercabnow.utils.PaymentDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class WelcomeActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    // Directions
    private GoogleMap mMap;
    private MarkerOptions place1, place2;
    Button getDirection;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Polyline currentPolyline;

    // Places
    private static final String TAG = "WelcomeActivity";
    private final String API_KEY = Constants.GOOGLE_MAPS_API_KEY;
    private PlacesClient placesClient;


    // Location
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    private static final float DEFAULT_ZOOM = 16f;
    private ArrayList<Driver> driverList = new ArrayList<>();

    // Markers
    private ArrayList<Marker> driverMarkers;


    // Intent values
    public static String[] p1, p2;

    // Firebase
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    DatabaseReference drivers;


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready " + googleMap.toString());

        mMap = googleMap;

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        mMap.setMyLocationEnabled(true);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // Get all valid drivers, adds their markers on map
        ArrayList<Driver> alldrivers = getAllDriverLocations(5);

        for(Driver driver:alldrivers){
            LatLng latLng1 = new LatLng(Double.parseDouble(driver.getSource().getLat()), Double.parseDouble(driver.getSource().getLng()));
            builder.include(latLng1);
        }
        //the include method will calculate the min and max bound.
        builder.include(latLng);

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.30); // offset from edges of the map 30% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        mMap.moveCamera(cu);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.5f));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        checkLocationPermission();
        Log.d(TAG, "onCreate: Started");

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        drivers = db.getReference("Drivers");

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

                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                    // Using currentLocation create a marker
                    place1 = new MarkerOptions().position(new LatLng(currentLocation.getLatitude(),
                            currentLocation.getLongitude()))
                            .title("Current Location");
                    mMap.addMarker(place1);


                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    // Get all valid drivers, adds their markers on map
                    ArrayList<Driver> alldrivers = getAllDriverLocations(5);

                    for(Driver driver:alldrivers){
                        LatLng latLng1 = new LatLng(Double.parseDouble(driver.getSource().getLat()), Double.parseDouble(driver.getSource().getLng()));
                        builder.include(latLng1);
                    }
                    //the include method will calculate the min and max bound.
                    builder.include(place1.getPosition());
                    builder.include(place2.getPosition());

                    LatLngBounds bounds = builder.build();

                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = getResources().getDisplayMetrics().heightPixels;
                    int padding = (int) (width * 0.30); // offset from edges of the map 30% of screen

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                    mMap.animateCamera(cu);

                    getDirection.setBackgroundColor(Color.BLACK);

                    // Get directions
                    new FetchURL(WelcomeActivity.this)
                            .execute(getUrl(place1.getPosition(),
                                    place2.getPosition(),
                                    "driving"),
                                    "driving");

                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_LONG).show();
            }
        });

        getDirection = findViewById(R.id.btnGetDirection);
        getDirection.setOnClickListener(view -> {
            if(place2 == null) {
                Toast.makeText(this, "Choose your destination", Toast.LENGTH_LONG).show();
            }
            else {
                Log.d(TAG, "onCreate: " + place1.getPosition() + place2.getPosition());

                // save static place values
                WelcomeActivity.p1 = new String[] {
                        String.valueOf(place1.getPosition().latitude),
                        String.valueOf(place1.getPosition().longitude)
                };

                WelcomeActivity.p2 = new String[] {
                        String.valueOf(place2.getPosition().latitude),
                        String.valueOf(place2.getPosition().longitude)
                };

                // select payment and move to ChooseRideActivity
                new PaymentDialog().show(getSupportFragmentManager(), "PaymentSelection");
            }
        });

    }

    private ArrayList<Driver> getAllDriverLocations(float distanceRadius){
        // Iterate through list of drivers and mark on map
        final LatLng[] src = new LatLng[1];
        drivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float[] answer = {0};
                int drawable = -1;
                Log.e("Driver Count " ,"" + dataSnapshot.getChildrenCount());
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Driver driver = dataSnapshot1.getValue(Driver.class);
                    Log.d(TAG, "DRIVERRRRRRR ISSSSSSSSS "+driver.getDriver_email());
                    src[0] = new LatLng(Double.parseDouble(driver.getSource().getLat()), Double.parseDouble(driver.getSource().getLng()));
                    Log.d(TAG, "Got driver - email:"+driver.getDriver_email());
                    if(src[0].latitude==0) {
                        Log.d(TAG, "LatLng set to 0 so cant do anything");
                        continue;
                    }
                    if(driver.getCab_status()=="NA" || driver.getCab_status()=="Transit") {
                        Log.d(TAG, "Driver scheduled or unavailable");
                        continue;
                    }

                    // Dist between current location of user and driver
                    Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), src[0].latitude, src[0].longitude, answer);
                    if(answer[0]>distanceRadius*1000) {
                        Log.d(TAG, "Distance=" + answer[0] + " - Failed");
                        //continue;
                    }
                    switch(driver.getCab_type()){
                        case "Auto":
                            drawable = R.drawable.auto;
                            break;
                        case "Sedan":
                            drawable = R.drawable.sedan;
                            break;
                        case "Micro":
                            drawable = R.drawable.micro;
                            break;
                    }
                    Log.d(TAG, "Driver is of drawable "+drawable);
                    Log.d(TAG, "USER LOCATION IS "+src[0].latitude + src[0].longitude);
                    driverList.add(driver);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(src[0].latitude, src[0].longitude)).
                            icon(BitmapDescriptorFactory.fromBitmap(
                                    createCustomMarker(WelcomeActivity.this,drawable,driver.getDriver_name()))))
                            .setTitle("CabNow Inc - "+answer[0]+"m");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
        return driverList;
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
                // DONE 1) logout out of the app
                // DONE 2) [optional] delete stored shared pref variables for new login info -- DONE
                Intent logoutintent = new Intent(this, MainActivity.class);
                SharedPreferences sharedPreferences;
                sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("email", "");
                editor.putString("password","");
                editor.apply();
                Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
                startActivity(logoutintent);

            default:
                return super.onOptionsItemSelected(item);
        }
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(WelcomeActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .setNegativeButton(R.string.notok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Permissions Granted!!!!!");
                        Intent i = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(i);
                        overridePendingTransition(0, 0);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

}
package com.example.ridercabnow.MapActivities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ridercabnow.Adapters.RideListAdapter;
import com.example.ridercabnow.HistoryActivity;
import com.example.ridercabnow.ProfileActivity;
import com.example.ridercabnow.R;
import com.example.ridercabnow.RiderAuth.MainActivity;
import com.example.ridercabnow.directionhelpers.FetchURL;
import com.example.ridercabnow.directionhelpers.TaskLoadedCallback;
import com.example.ridercabnow.models.Latlng;
import com.example.ridercabnow.models.Ride;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ChooseRideActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private static final String TAG = "ChooseRideActivity";
    private MarkerOptions place1, place2;

    // Directions
    private GoogleMap mMap;
    private Polyline currentPolyline;

    // Location
    private FusedLocationProviderClient mFusedLocationClient;
    private static final float DEFAULT_ZOOM = 16f;

    // umano ListView params
    private String[] mRides = new String[] {"Auto", "Micro", "Sedan"};
    private Float[] mPrices = new Float[] {0f,0f,0f};
    private int[] mImages = new int[] {R.drawable.auto, R.drawable.micro1, R.drawable.sedan2};

    // AlertDialog after selecting one of the ride types
    AlertDialog.Builder dialogBuilder;

    // Firebase
    String rid = "";
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    // Payment method obtained from PaymentDialog in WelcomeActivity
    // If not set for some reason, Default is UPI
    public static String payment = "UPI";

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
        // TODO (1) Take location and storage permission in login activity itself

        // Payment method
        // DONE ASK for payment type in AlertDialog [upi or cash] in WelcomeActivity

        // UI
        // DONE (1) Create slidingPanel for UI [Ride selection]
        // DONE (2) Create custom list layout for slidingPanel ride selection
        // DONE         Custom list should have
        //          -> Ride name
        //          -> Ride picture
        // TODO (3) -> [implement pricing policy crap] in calcPrice()
        // TODO     -> Complete calcDistance() and calcPrice() methods

        // Pre work
        // DONE (1) init onMapReady, FusedLocationProviderClient and draw marker options
        // DONE (2) Integrate directions API draw PolyLine from place1 to place2

        // Requirement
        // DONE (1) onClick of any ride picture from slidingPanel
        // DONE     -> should 'ADD' Ride to DB
        // TODO     -> [OPTIONAL] setView() to AlertDialog which has ProgressBar

        // LATER !!!
        // Post Driver app completion integrate this
        // TODO (1) When driver accepts
        //          -> change listView to show static emergency contact or sos and notifyAdapter
        //          -> show driver location and draw a PolyLine

        // Post ride completion
        // TODO (2) Make a button in listView to simulate ride completion for rider
        // TODO (3) Integrate payment and all that crap...

        // init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Rides");

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
        // mPrices[] is updated with proper prices after calcPrice()
        calcPrice();
        adapter.notifyDataSetChanged();

        // onClick builds Ride object and adds to Rides
        listView.setOnItemClickListener((adapterView, view, i, l) -> {

            switch(i) {
                case 0:
                    showAlert();
                    bookRide("auto", i);
                    break;
                case 1:
                    showAlert();
                    bookRide("micro", i);
                    break;
                case 2:
                    showAlert();
                    bookRide("sedan", i);
                    break;
            }
        });

    }

    private void bookRide(String type, int index) {

        Latlng p1 = new Latlng(String.valueOf(place1.getPosition().latitude),
                String.valueOf(place1.getPosition().longitude));
        Latlng p2 = new Latlng(String.valueOf(place2.getPosition().latitude),
                String.valueOf(place2.getPosition().longitude));

        String price = String.valueOf(mPrices[index]);
        String distance = String.valueOf(calcDistance());
        // TODO get payment from AlertDialog, before showing booking AlertDialog
        String payment = ChooseRideActivity.payment;
        //String payment = "UPI";

        // create Ride object for firebase based on the type of ride selected
        Ride ride = null;
        switch (type) {
            case "auto": {
                Toast.makeText(this, "Booking an auto ...", Toast.LENGTH_SHORT).show();

                ride = new Ride(p1, p2, "looking", price, distance, payment,
                        "", firebaseAuth.getUid(), "auto");
                break;
            }
            case "micro": {
                Toast.makeText(this, "Booking a micro ...", Toast.LENGTH_SHORT).show();
                ride = new Ride(p1, p2, "looking", price, distance, payment,
                        "", firebaseAuth.getUid(), "micro");
                break;
            }
            case "sedan": {
                Toast.makeText(this, "Booking a sedan ...", Toast.LENGTH_SHORT).show();
                ride = new Ride(p1, p2, "looking", price, distance, payment,
                        "", firebaseAuth.getUid(), "sedan");
                break;
            }
        }

        if(ride != null) {
            Log.d(TAG, "bookRide: for uid -> " + ride.getRider());
        }

        // create global rid and use it to save ride object
        rid = databaseReference.push().getKey();
        if(rid != null) {
            databaseReference.child(rid).setValue(ride);
        }
    }

    private void showAlert() {

        dialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Please wait")
                .setMessage("Looking for rides ...")
                .setCancelable(false)
                .setNegativeButton("cancel", (dialogInterface, i) -> {
                    databaseReference.child(rid).child("status").setValue("cancelled");
                    dialogInterface.dismiss();
                    Toast.makeText(ChooseRideActivity.this, "Ride cancelled", Toast.LENGTH_SHORT).show();
                });

        dialogBuilder.show();
    }

    private void calcPrice() {
        // TODO implement pricing policy here for different rides
        //  change values of Float[] mPrices index:0->auto, 1->micro, 2->sedan
        int AUTO_BASE = 20, AUTO_KM = 10;
        int MICRO_BASE = 50, MICRO_KM = 25;
        int SEDAN_BASE = 100, SEDAN_KM = 50;

        float distance = calcDistance();

        // base applies for < 1km, for every km after base
        mPrices[0] = 30f;
        mPrices[1] = 100f;
        mPrices[2] = 300f;
    }

    private float calcDistance() {
        // TODO somehow calculate distance and return float of that
        return 3.3f;
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
                startActivity(new Intent(this , ProfileActivity.class));
                return true;

            case R.id.menuHistory:
                Toast.makeText(this, "Showing your History", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                return true;

            case R.id.menuLogout:
                // TODO [optional] delete stored shared pref variables for new login info

                Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
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

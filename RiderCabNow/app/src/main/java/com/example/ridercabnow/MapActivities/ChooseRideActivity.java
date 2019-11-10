package com.example.ridercabnow.MapActivities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ridercabnow.Adapters.RideListAdapter;
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
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.sql.Date;
import java.sql.Time;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class ChooseRideActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private static final String TAG = "ChooseRideActivity";
    private MarkerOptions place1, place2;

    // Directions
    private GoogleMap mMap;
    private Polyline currentPolyline;

    // Location
    private FusedLocationProviderClient mFusedLocationClient;

    // umano ListView params
    private String[] mRides = new String[] {"Auto", "Micro", "Sedan"};
    private Float[] mPrices = new Float[] {0f,0f,0f};
    private int[] mImages = new int[] {R.drawable.auto, R.drawable.micro, R.drawable.sedan};

    // AlertDialog after selecting one of the ride types
    AlertDialog alert;
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
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //the include method will calculate the min and max bound.
        builder.include(place1.getPosition());
        builder.include(place2.getPosition());

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        mMap.moveCamera(cu);
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
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_choose_ride);

        // Gets source and dest MarkerOptions from previous activity
        getIntentInfo();


        // After Driver app completion integrate this
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

        String payment = ChooseRideActivity.payment;

        // create Ride object for firebase based on the type of ride selected
        Ride ride = null;
        switch (type) {
            case "auto": {
                Toast.makeText(this, "Booking an auto ...", Toast.LENGTH_SHORT).show();

                ride = new Ride(p1, p2, "Searching", price, distance, payment,
                        "", firebaseAuth.getUid(), "auto");
                break;
            }
            case "micro": {
                Toast.makeText(this, "Booking a micro ...", Toast.LENGTH_SHORT).show();

                ride = new Ride(p1, p2, "Searching", price, distance, payment,
                        "", firebaseAuth.getUid(), "micro");
                break;
            }
            case "sedan": {
                Toast.makeText(this, "Booking a sedan ...", Toast.LENGTH_SHORT).show();

                ride = new Ride(p1, p2, "Searching", price, distance, payment,
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
            DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference("Rides")
                    .child(rid).child("driver");

            rideRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: snapshot -> " + dataSnapshot.getValue());

                    String driverId = (String) dataSnapshot.getValue();

                    if(driverId != null && driverId.equals("")) {
                        // still not accepted here
                        Toast.makeText(ChooseRideActivity.this, "Waiting for drivers ...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.d(TAG, "onDataChange: driverId ->> " + driverId);

                        // change Ride status value
                        databaseReference.child(rid).child("status").setValue("Accepted");

                        // create intent values
                        String[] p1 = new String[] {
                                String.valueOf(place1.getPosition().latitude),
                                String.valueOf(place1.getPosition().longitude)
                        };
                        String[] p2 = new String[] {
                                String.valueOf(place2.getPosition().latitude),
                                String.valueOf(place2.getPosition().longitude)
                        };

                        alert.dismiss();
                        Intent intent = new Intent(getApplicationContext(), TravelActivity.class);
                        intent.putExtra("place1", p1);
                        intent.putExtra("place2", p2);
                        intent.putExtra("rid", rid);
                        intent.putExtra("dId", driverId);
                        startActivity(intent);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void showAlert() {

        dialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Please wait")
                .setMessage("Looking for rides ...")
                .setCancelable(false)
                .setNegativeButton("cancel ride", (dialogInterface, i) -> {
                    databaseReference.child(rid).child("status").setValue("cancelled");
                    dialogInterface.dismiss();
                    Toast.makeText(ChooseRideActivity.this, "Ride cancelled", Toast.LENGTH_SHORT).show();
                });

        alert = dialogBuilder.show();
    }

    private void calcPrice() {
        mPrices[0] = 30f;
        mPrices[1] = 100f;
        mPrices[2] = 300f;

        float BASE_AUTO = 30f, PER_AUTO_KM = 5f;
        float BASE_MICRO = 40f, PER_MICRO_KM = 10f;
        float BASE_SEDAN = 60f, PER_SEDAN_KM = 20f;
        float SURGE_RATE = 1.3f;

        // init to base default
        float total_auto = BASE_AUTO,
                total_micro = BASE_MICRO,
                total_sedan = BASE_SEDAN;

        float distance = calcDistance();

        if(distance > 1f) {
            // add base + (remaining dist * perKM price)
            total_auto += (distance - 1) * PER_AUTO_KM;
            total_micro += (distance - 1) * PER_MICRO_KM;
            total_sedan += (distance - 1) * PER_SEDAN_KM;
        }

        if(isSurgeTime()) {
            // 30% bonus
            total_auto *= SURGE_RATE;
            total_micro *= SURGE_RATE;
            total_sedan *= SURGE_RATE;
        }

        // here we have actual total price .2f format
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        mPrices[0] = Float.parseFloat(df.format(total_auto));
        mPrices[1] = Float.parseFloat(df.format(total_micro));
        mPrices[2] = Float.parseFloat(df.format(total_sedan));

    }

    private float calcDistance() {
        LatLng l1 = new LatLng(place1.getPosition().latitude, place1.getPosition().longitude);
        LatLng l2 = new LatLng(place2.getPosition().latitude, place2.getPosition().longitude);


        float distance = (float) SphericalUtil.computeDistanceBetween(l1, l2) / 1000;
        Log.d(TAG, "calcDistance: " + distance);
        // in KMs
        return distance;
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

    private boolean isSurgeTime() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH");
            LocalDateTime now = LocalDateTime.now();
            Log.d(TAG, "isSurgeTime: " + formatter.format(now));

            String t = null;

            t = formatter.format(now);

            // 8 to 11
            if (t.equals("8") || t.equals("9") || t.equals("10") || t.equals("11")) {
                return true;
            }

            // 5 to 8
            if (t.equals("17") || t.equals("18") || t.equals("19") || t.equals("20")) {
                return true;
            }

            // no surge :(
            return false;
        }

        return false;
    }


}

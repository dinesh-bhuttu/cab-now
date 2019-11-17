package com.example.drivercabnow.MapActivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.drivercabnow.DirectionHelpers.FetchURL;
import com.example.drivercabnow.DirectionHelpers.TaskLoadedCallback;
import com.example.drivercabnow.MainActivity;
import com.example.drivercabnow.Models.Driver;
import com.example.drivercabnow.Models.Latlng;
import com.example.drivercabnow.Models.Ride;
import com.example.drivercabnow.Models.User;
import com.example.drivercabnow.ProfileActivity;
import com.example.drivercabnow.R;
import com.example.drivercabnow.RecycleActivity;
import com.example.drivercabnow.Utils.Constants;
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

import java.util.ArrayList;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;


/* TODO - get user location (source from rides), destination from rides, driver current location
 */
public class TravelActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        TaskLoadedCallback,
        View.OnClickListener {

    private int drawable = 1;
    private int type1 = DriverWelcomeActivity.type;
    private String rid;
    private static final String TAG = "TravelActivity";
    private MarkerOptions place1, place2, place3;

    // Directions
    private GoogleMap mMap;
    private Polyline currentPolyline;

    // Location
    private FusedLocationProviderClient mFusedLocationClient;

    // Firebase

    String ridername, riderphone;
    Double riderrating, price, startlat, startlng, destlat, destlng;


    Double driverLat, driverLng;
    // widgets
    TextView rideType, rating, riderName, vehicleNo, lastFour, ridePrice, enjoyRide;
    Button btnChangeStatus;
    CardView callRider;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference drivers;
    DatabaseReference rides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        drivers = db.getReference("Drivers");
        rides = db.getReference("Rides");


        Intent intent = getIntent();
        ridername = intent.getStringExtra("UserName");
        riderphone = intent.getStringExtra("UserPhone");
        Log.d(TAG, ridername+riderphone);
        riderrating = Double.parseDouble(intent.getStringExtra("UserRating"));
        startlat = Double.parseDouble(intent.getStringExtra("StartLocationLat"));
        startlng = Double.parseDouble(intent.getStringExtra("StartLocationLng"));

        destlat = Double.parseDouble(intent.getStringExtra("EndLocationLat"));
        destlng = Double.parseDouble(intent.getStringExtra("EndLocationLng"));
        price = Double.parseDouble(intent.getStringExtra("Price"));

        Log.d(TAG, ridername + riderphone);
        Toast.makeText(TravelActivity.this, ridername + " has been notified", Toast.LENGTH_LONG).show();
        initWidgets();
        rating.setText(String.valueOf(riderrating));
        riderName.setText(ridername);
        ridePrice.setText(price.toString());

        LatLng l1 = new LatLng(startlat, startlng);
        LatLng l2 = new LatLng(destlat, destlng);

        place1 = new MarkerOptions().position(l1);
        place2 = new MarkerOptions().position(l2);

        driverLat = Double.parseDouble(intent.getStringExtra("DriverLocationLat"));
        driverLng = Double.parseDouble(intent.getStringExtra("DriverLocationLng"));
        rid = intent.getStringExtra("RideId");

        LatLng curr = new LatLng(driverLat,driverLng);
        place3 = new MarkerOptions().position(curr);
        Log.d(TAG, "getIntentInfo: " + l1 + " " + l2);

        // get vehicle number after splitting
        String[] p = getVehicleNo(intent.getStringExtra("Vehicle"));


        vehicleNo.setText(p[0]);
        lastFour.setText(p[1]);

        Log.d(TAG, "GGGOOOTTT DDDEEETTTAAIILLLSSS");

        // Get toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Ride Info");
        setSupportActionBar(toolbar);

        // init map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(TravelActivity.this);
        getLastLocation();

        // ride is active here, display enjoy message and remove cancel button
        btnChangeStatus.setVisibility(View.VISIBLE);
        enjoyRide.setVisibility(View.GONE);
        // Note - This activity is called only after ride status is set to "Accepted"
        // Driver details (me)
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready " + googleMap.toString());

//
//        mMap.clear();
//        mMap.addMarker(currentlocation);
//        mMap.addMarker(place1).setTitle("Pickup point");
//        mMap.addMarker(place2).setTitle("Destination");
//        drawPolyLine();
//        CameraUpdate moveToPickup = CameraUpdateFactory.newLatLngZoom(
//                new LatLng(place1.getPosition().latitude,
//                        place1.getPosition().longitude),
//                15.6f
//        );
//        mMap.moveCamera(moveToPickup);
//
//        // draw line from source to dest
//        if (Constants.savedPolylineOptions != null) {
//            Log.d(TAG, "onMapReady: drawing saved polyline" + Constants.savedPolylineOptions);
//            mMap.addPolyline(Constants.savedPolylineOptions);
//        } else {
//            drawPolyLine();
//        }


        mMap = googleMap;

        // 2 markers and saved polyline from previous activity
        mMap.addMarker(place2).setTitle("Destination");
        switch (type1) {
            case 1:
                drawable = R.drawable.auto2;
                break;
            case 2:
                drawable = R.drawable.sedan;
                break;
            case 3:
                drawable = R.drawable.micro;
                break;
        }
        mMap.addMarker(new MarkerOptions().position(new LatLng(driverLat, driverLng)).icon(BitmapDescriptorFactory.fromBitmap(
                createCustomMarker(TravelActivity.this, drawable,"")))).
                setTitle("Me");

        Log.d(TAG, "LLLOOOOCCCAAATTTIIIIOOOONNNNSSSSSS");
        Log.d(TAG, startlat+","+startlng);
        Log.d(TAG, destlat+","+destlng);
        Log.d(TAG, driverLat.toString()+","+driverLng.toString());


        int padding = 100; // offset from edges of the map in pixels
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(place1.getPosition());
        builder.include(place2.getPosition());
        builder.include(place3.getPosition());

        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        //CameraUpdate c2 = CameraUpdateFactory.newLatLngZoom(new LatLng(startlat, startlng),15.7f);
        mMap.animateCamera(cu);
        mMap.setMyLocationEnabled(true);

        // mark rider
        Log.d(TAG, "onMapReady: driver loc ->> " + startlat + " " + startlng);
        if (startlat != null && startlng != null) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(startlat, startlng)).icon(BitmapDescriptorFactory.fromBitmap(
                    createCustomMarker(TravelActivity.this, R.drawable.user3,"")))).
                    setTitle(ridername);
            // draw line
            drawPolylineFromSourceToDestination(place3, place1);
            //drawPolylineFromSourceToDestination(place1, place2);
        }

        // add listener

        btnChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                place3 = place1;
                mMap.clear();


                mMap.addMarker(new MarkerOptions().position(place3.getPosition()).icon(BitmapDescriptorFactory.fromBitmap(
                        createCustomMarker(TravelActivity.this, drawable,"")))).
                        setTitle("Me + "+ridername);

                mMap.addMarker(place2).setTitle("Destination");
                drawPolylineFromSourceToDestination(place3, place2);


                int padding = 130; // offset from edges of the map in pixels
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(place2.getPosition());
                builder.include(place3.getPosition());

                LatLngBounds bounds = builder.build();

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                //CameraUpdate c2 = CameraUpdateFactory.newLatLngZoom(new LatLng(startlat, startlng),15.7f);
                mMap.animateCamera(cu);
                mMap.setMyLocationEnabled(true);

                rides.child(rid).child("status").setValue("Active");
                btnChangeStatus.setBackgroundColor(getColor(R.color.resetbuttoncolor));
                btnChangeStatus.setText("DESTINATION REACHED");
                btnChangeStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(place2.getPosition()).icon(BitmapDescriptorFactory.fromBitmap(
                                createCustomMarker(TravelActivity.this, drawable,"")))).
                                setTitle("Me + "+ridername+" = Destination");
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(place2.getPosition()));
                        rides.child(rid).child("status").setValue("Arrived");

                        rides.child(rid).child("status").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String s = dataSnapshot.getValue(String.class);
                                if(s.equalsIgnoreCase("finished")){
                                    Toast.makeText(TravelActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();

                                    LayoutInflater inflater = TravelActivity.this.getLayoutInflater();
                                    View ratingView = inflater.inflate(R.layout.alert_rate_rider, null);

                                    //final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(get).build();
                                    //waitingDialog.setTitle("Loading...");
                                    //waitingDialog.show();


                                    AlertDialog ratingDialog;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(TravelActivity.this)
                                            .setTitle("Rate your Rider")
                                            .setCancelable(false)
                                            .setView(ratingView);

                                    TextView ratingDriverName = ratingView.findViewById(R.id.riderName);
                                    ratingDriverName.setText(riderName.getText().toString());
                                    RatingBar ratingBar = ratingView.findViewById(R.id.ratingBar);
                                    Button giveRating = ratingView.findViewById(R.id.giveRating);

                                    ratingDialog = builder.create();
                                    ratingDialog.show();

                                    giveRating.setOnClickListener(view -> {
                                        float currentRating = ratingBar.getRating();
                                        if(currentRating > 0f) {
                                            ratingDialog.dismiss();
                                            rateRider(currentRating);
                                        }
                                        else {
                                            Toast.makeText(TravelActivity.this, "Please give a rating", Toast.LENGTH_SHORT).show();
                                        }
                                    });


                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        });

    }

    private void drawPolylineFromSourceToDestination(MarkerOptions placea, MarkerOptions placeb){
        new FetchURL(TravelActivity.this)
                .execute(getUrl(placea.getPosition(), placeb.getPosition(),
                        "driving"),
                        "driving");
    }



    private void getLastLocation() {
        Task<Location> task = mFusedLocationClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                // Get Map fragment here
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapNearBy);

                if (mapFragment != null) {
                    mapFragment.getMapAsync(TravelActivity.this);

                }
            } else {
                Toast.makeText(TravelActivity.this, "Location not found", Toast.LENGTH_LONG).show();
            }
        });
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


    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker, null);

        CircleImageView markerImage = marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);

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

    private void rateRider(float rating) {

        // TODO change avg formula somehow -> we need number of rides by driver
        double avg = (rating + riderrating) / 2;

        Task<Void> task = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(rid)
                .child("average_rating")
                .setValue(avg);

        task.addOnSuccessListener(aVoid -> {
            Log.d(TAG, "rateRider: TASK -> inside onSuccessListener");
            if (task.isSuccessful()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Ride completed")
                        .setMessage("Thank you for using our services  - CabNow Inc.")
                        .setCancelable(true)
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            //addRideToDriverHistory(rating);
                        });
                builder.create().show();
            }
        });
    }

//    private void addRideToUserHistory(float ratingGiven) {
//
//        HistoryItem ride = new HistoryItem(
//                Float.parseFloat(ridePrice.getText().toString()),
//                new Latlng(String.valueOf(place2.getPosition().latitude),
//                        String.valueOf(place2.getPosition().longitude)),
//                driverName.getText().toString(),
//                ChooseRideActivity.payment,
//                ratingGiven,
//                rideType.getText().toString()
//        );
//
//        String uid = FirebaseAuth.getInstance().getUid();
//        if(uid != null)
//        {
//            // Task to add ride to user history list
//            Task<Void> task = FirebaseDatabase.getInstance()
//                    .getReference("Users")
//                    .child(uid)
//                    .child("history")
//                    .child(rid)
//                    .setValue(ride);
//
//            task.addOnSuccessListener(aVoid -> {
//                if(task.isSuccessful()) {
//                    Toast.makeText(TravelActivity.this,
//                            "Added this ride to your history",
//                            Toast.LENGTH_SHORT).show();
//
//                    // task to remove current rid from Rides
//                    Task<Void> task1 = FirebaseDatabase.getInstance()
//                            .getReference("Rides")
//                            .child(rid)
//                            .removeValue();
//
//                    task1.addOnSuccessListener(aVoid1 -> {
//                        if(task.isSuccessful()) {
//                            // clear map, finish() and move to WelcomeActivity
//                            mMap.clear();
//                            startActivity(new Intent(this, WelcomeActivity.class));
//                            finish();
//                        }
//                    });
//                }
//            });
//        }
//
//    }

    private void initWidgets() {
        rideType = findViewById(R.id.tvRideType);
        ridePrice = findViewById(R.id.tvPrice);
        rating = findViewById(R.id.tvRating);
        riderName = findViewById(R.id.tvDriverName);
        vehicleNo = findViewById(R.id.tvVehicleNo);
        lastFour = findViewById(R.id.tvLastFour);
        enjoyRide = findViewById(R.id.tvEnjoyRide);
        btnChangeStatus = findViewById(R.id.btnChangeStatus);
        callRider = findViewById(R.id.ivCallDriver);
        callRider.setOnClickListener(this);
    }

    private String[] getVehicleNo(String vehicleNo) {
        int n = vehicleNo.length();
        String[] p = new String[]{vehicleNo.substring(0, n - 4),
                vehicleNo.substring(n - 4, n)};

        Log.d(TAG, "getVehicleNo: vehicle -> " + p[0] + " " + p[1]);
        return p;
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ivCallDriver) {
            // call driver here
            Toast.makeText(this, "Calling driver", Toast.LENGTH_SHORT).show();
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.fromParts("tel", "+91" + riderphone, null));
            startActivity(callIntent);
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Back button is disabled", Toast.LENGTH_SHORT).show();
    }

    // pulling History values
//    String uid = FirebaseAuth.getInstance().getUid();
//    List<HistoryItem> list = new ArrayList<>();
//    if(uid != null) {
//        Log.d(TAG, "onCreate: uid -> " + uid);
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference("Users")
//                .child(uid)
//                .child("history");
//
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.d(TAG, "onDataChange: Snapshot ->> " + dataSnapshot.getValue());
//                for (DataSnapshot ds: dataSnapshot.getChildren()) {
//                    HistoryItem item =  ds.getValue(HistoryItem.class);
//                    list.add(item);
//                }
//                Log.d(TAG, "onDataChange: List -> " + list);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d(TAG, "onCancelled: ERROR : " + databaseError.getMessage());
//            }
//        });
//    }

}

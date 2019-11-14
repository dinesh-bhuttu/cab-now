package com.example.ridercabnow.MapActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridercabnow.ProfileActivity;
import com.example.ridercabnow.R;
import com.example.ridercabnow.RiderAuth.MainActivity;
import com.example.ridercabnow.directionhelpers.FetchURL;
import com.example.ridercabnow.directionhelpers.TaskLoadedCallback;
import com.example.ridercabnow.models.Driver;
import com.example.ridercabnow.models.HistoryItem;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class TravelActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        TaskLoadedCallback,
        View.OnClickListener {

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
    String driverPhone = "";
    String driverUPI = "";
    String billPrice = "";
    Float driverRating = 0f;

    // UPI
    AlertDialog paymentAlert;
    final int UPI_PAYMENT = 0;

    // widgets
    TextView rideType, rating, driverName, vehicleNo, lastFour, ridePrice, enjoyRide;
    Button btnCancelRide;
    CardView callDriver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        getIntentInfo();

        // Get toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Ride Info");
        setSupportActionBar(toolbar);

        // init map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        // fill out driver details section & get driver location in global vars
        fillDriverDetails();

        DatabaseReference activeRef = FirebaseDatabase.getInstance()
                .getReference("Rides").child(rid).child("status");
        activeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // check when status becomes Active
                String status = (String) dataSnapshot.getValue();

                Log.d(TAG, "onDataChange: status reference -> " + dataSnapshot.getValue());
                if(status != null && status.equalsIgnoreCase("active"))
                {
                    // ride is active here, display enjoy message and remove cancel button
                    btnCancelRide.setVisibility(View.GONE);
                    enjoyRide.setVisibility(View.VISIBLE);
                    enjoyRide.setBackgroundColor(Color.BLACK);

                    // remove driver to user polyline and move to pickup
                    mMap.clear();
                    mMap.addMarker(place1).setTitle("Pickup point");
                    mMap.addMarker(place2).setTitle("Destination");
                    drawPolyLine();
                    CameraUpdate moveToPickup = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(place1.getPosition().latitude,
                                    place1.getPosition().longitude),
                            15.6f
                    );
                    mMap.moveCamera(moveToPickup);

                    // draw line from source to dest
                    if(Constants.savedPolylineOptions != null) {
                        Log.d(TAG, "onMapReady: drawing saved polyline" + Constants.savedPolylineOptions);
                        mMap.addPolyline(Constants.savedPolylineOptions);
                    }
                    else {
                        drawPolyLine();
                    }
                }

                else if(status != null && status.equalsIgnoreCase("arrived"))
                {
                    CameraUpdate cDest = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(place2.getPosition().latitude,
                                    place2.getPosition().longitude),
                            15.6f
                    );
                    mMap.moveCamera(cDest);
                    Log.d(TAG, "onDataChange: Destination reached");
                    if(ChooseRideActivity.payment.equalsIgnoreCase("upi"))
                    {
                        // Use PaymentUtil here
                        Log.d(TAG, "onDataChange: Payment : " + ChooseRideActivity.payment);

                        LayoutInflater inflater = TravelActivity.this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.alert_payment_layout, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(TravelActivity.this)
                                .setTitle("Payment")
                                .setView(dialogView)
                                .setCancelable(false);

                        Button gpay = dialogView.findViewById(R.id.btnPay);
                        TextView price = dialogView.findViewById(R.id.alertPrice);
                        TextView driverUpi = dialogView.findViewById(R.id.alertDriverUPI);
                        price.setText(billPrice);
                        driverUpi.setText(driverUPI);

                        paymentAlert = builder.create();
                        paymentAlert.show();

                        gpay.setOnClickListener(view -> {

                            payUsingUpi(billPrice, driverUPI,
                                    driverName.getText().toString());

                            paymentAlert.dismiss();
                        });

                    }
                    else {
                        Log.d(TAG, "onDataChange: Payment : " + ChooseRideActivity.payment);
                        // TODO handle cash payment way

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        

    }

    // GPAY helpers
    void payUsingUpi(String amount, String upiId, String name) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", "")
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();


        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(TravelActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT) {
            if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                if (data != null) {
                    String trxt = data.getStringExtra("response");
                    Log.d("UPI", "onActivityResult: " + trxt);
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(trxt);
                    upiPaymentDataOperation(dataList);
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
            } else {
                Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                ArrayList<String> dataList = new ArrayList<>();
                dataList.add("nothing");
                upiPaymentDataOperation(dataList);
            }
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(TravelActivity.this)) {
            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String[] response = str.split("&");
            for (String s : response) {
                String[] equalStr = s.split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                } else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                Toast.makeText(TravelActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: "+approvalRefNo);

                paymentAlert.dismiss();

                LayoutInflater inflater = TravelActivity.this.getLayoutInflater();
                View ratingView = inflater.inflate(R.layout.alert_rate_driver, null);

                AlertDialog ratingDialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Rate your driver")
                        .setCancelable(false)
                        .setView(ratingView);

                TextView ratingDriverName = ratingView.findViewById(R.id.driverName);
                ratingDriverName.setText(driverName.getText().toString());
                RatingBar ratingBar = ratingView.findViewById(R.id.ratingBar);
                Button giveRating = ratingView.findViewById(R.id.giveRating);

                ratingDialog = builder.create();
                ratingDialog.show();

                giveRating.setOnClickListener(view -> {
                    float currentRating = ratingBar.getRating();
                    if(currentRating > 0f) {
                        ratingDialog.dismiss();
                        rateDriver(currentRating);
                    }
                    else {
                        Toast.makeText(this, "Please give a rating", Toast.LENGTH_SHORT).show();
                    }
                });


            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(TravelActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                paymentAlert.show();
            }
            else {
                Toast.makeText(TravelActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                paymentAlert.show();
            }
        }
        else {
            Toast.makeText(TravelActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable();
        }
        return false;
    }
    // GPAY helpers end



    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready " + googleMap.toString());

        mMap = googleMap;

        // 2 markers and saved polyline from previous activity
        mMap.addMarker(place1).setTitle("Pickup point");
        mMap.addMarker(place2).setTitle("Destination");

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
        billPrice = i.getStringExtra("bill");

        LatLng l1 = new LatLng(Double.parseDouble(p1[0]), Double.parseDouble(p1[1]));
        LatLng l2 = new LatLng(Double.parseDouble(p2[0]), Double.parseDouble(p2[1]));

        place1 = new MarkerOptions().position(l1);
        place2 = new MarkerOptions().position(l2);

        Log.d(TAG, "getIntentInfo: " + l1 + " " + l2);
    }


    private void fillDriverDetails() {

        initWidgets();

        DatabaseReference driverRef = FirebaseDatabase.getInstance()
                .getReference("Drivers")
                .child(driverId);
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: driverId reference ->> " + dataSnapshot.getValue());

                Driver driver = dataSnapshot.getValue(Driver.class);
                if(driver != null) {
                    Toast.makeText(TravelActivity.this,
                            driver.getDriver_name() + " has accepted", Toast.LENGTH_LONG).show();

                    // driver details
                    driverPhone = driver.getDriver_phone();
                    rideType.setText(driver.getCab_type());
                    driverRating = driver.getAverage_rating();
                    rating.setText(String.valueOf(driverRating));
                    driverName.setText(driver.getDriver_name());
                    // get vehicle number after splitting
                    String[] p = getVechicleNo(driver.getVehicle_no());
                    vehicleNo.setText(p[0]);
                    lastFour.setText(p[1]);

                    // driver global location vars
                    driverUPI = driver.getUPI_Id();
                    Latlng l = driver.getSource();
                    driverLat = Double.parseDouble(l.getLat());
                    driverLng = Double.parseDouble(l.getLng());
                    Log.d(TAG, "onDataChange: Driver values ->> " + driverLat + " " + driverLng);

                    // single value event for price in ride
                    DatabaseReference rideRef = FirebaseDatabase.getInstance()
                            .getReference("Rides").child(rid);
                    rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // grab price
                            Ride ride = dataSnapshot.getValue(Ride.class);
                            if(ride != null) {
                                ridePrice.setText(ride.getPrice());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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

    private void rateDriver(float rating) {

        // TODO change avg formula somehow -> we need number of rides by driver
        float avg = (rating + driverRating) / 2;

        Task<Void> task = FirebaseDatabase.getInstance()
                .getReference("Drivers")
                .child(driverId)
                .child("average_rating")
                .setValue(avg);

        task.addOnSuccessListener(aVoid -> {
            Log.d(TAG, "rateDriver: TASK -> inside onSuccessListener");
            if(task.isSuccessful()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Ride completed")
                        .setMessage("Thank you for using our services  - CabNow Inc.")
                        .setCancelable(true)
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            dialogInterface.dismiss();

                            // set status of ride to finished
                            FirebaseDatabase.getInstance()
                                    .getReference("Rides")
                                    .child(rid)
                                    .child("status")
                                    .setValue("finished");

                            addRideToUserHistory(rating);

                        });

                builder.create().show();
            }
        });
    }

    private void addRideToUserHistory(float ratingGiven) {

        HistoryItem ride = new HistoryItem(
                Float.parseFloat(ridePrice.getText().toString()),
                new Latlng(String.valueOf(place2.getPosition().latitude),
                        String.valueOf(place2.getPosition().longitude)),
                driverName.getText().toString(),
                ChooseRideActivity.payment,
                ratingGiven,
                rideType.getText().toString()
        );

        String uid = FirebaseAuth.getInstance().getUid();
        if(uid != null)
        {
            // Task to add ride to user history list
            Task<Void> task = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("history")
                    .child(rid)
                    .setValue(ride);

            task.addOnSuccessListener(aVoid -> {
                if(task.isSuccessful()) {
                    Toast.makeText(TravelActivity.this,
                            "Added this ride to your history",
                            Toast.LENGTH_SHORT).show();

                    // task to remove current rid from Rides
                    Task<Void> task1 = FirebaseDatabase.getInstance()
                            .getReference("Rides")
                            .child(rid)
                            .removeValue();

                    task1.addOnSuccessListener(aVoid1 -> {
                        if(task.isSuccessful()) {
                            // clear map, finish() and move to WelcomeActivity
                            mMap.clear();
                            startActivity(new Intent(this, WelcomeActivity.class));
                            finish();
                        }
                    });
                }
            });
        }

    }

    private void initWidgets() {
        rideType = findViewById(R.id.tvRideType);
        ridePrice = findViewById(R.id.tvPrice);
        rating = findViewById(R.id.tvRating);
        driverName = findViewById(R.id.tvDriverName);
        vehicleNo = findViewById(R.id.tvVehicleNo);
        lastFour = findViewById(R.id.tvLastFour);
        enjoyRide = findViewById(R.id.tvEnjoyRide);
        btnCancelRide = findViewById(R.id.btnCancelRide);
        callDriver = findViewById(R.id.ivCallDriver);
        callDriver.setOnClickListener(this);
    }

    private String[] getVechicleNo(String vehicleNo) {
        int n = vehicleNo.length();
        String[] p = new String[] {vehicleNo.substring(0, n-4),
                vehicleNo.substring(n-4, n)};

        Log.d(TAG, "getVechicleNo: vehicle -> " + p[0] + " " + p[1]);
        return p;
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ivCallDriver) {
            // call driver here
            Toast.makeText(this, "Calling driver", Toast.LENGTH_SHORT).show();
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.fromParts("tel", "+91" + driverPhone, null));
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

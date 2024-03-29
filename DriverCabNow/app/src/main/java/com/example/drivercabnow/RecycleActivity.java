package com.example.drivercabnow;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.drivercabnow.Adapters.CustomAdapter;
import com.example.drivercabnow.MapActivities.DriverWelcomeActivity;
import com.example.drivercabnow.Models.Driver;
import com.example.drivercabnow.Models.Ride;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RecycleActivity extends AppCompatActivity {
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<Ride> data;

    Driver currentDriver;
    private ValueEventListener ridesValueListener;
    private static final String TAG = "RecycleActivity";
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users, rides, drivers;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.another);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        rides = db.getReference("Rides");
        drivers = db.getReference("Drivers");

        data = new ArrayList<>();
        adapter = new CustomAdapter(RecycleActivity.this, data);


        String currentDriverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        drivers.child(currentDriverID) // Create a reference to the child node directly
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This callback will fire even if the node doesn't exist, so now check for existence
                        if (dataSnapshot.exists()) {
                            currentDriver = dataSnapshot.getValue(Driver.class);
                            //System.out.println("The node exists.");
                        } else {
                            //System.out.println("The node does not exist.");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });

        getAllRideRequests(5);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void getAllRideRequests(float distanceRadius){
        // Iterate through list of drivers and mark on map
        final LatLng[] src = new LatLng[1];

        ridesValueListener = rides.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float[] answer = {0};
                Log.d(TAG, "GOING THROUGH RIDE DATABASE");
                Log.d(TAG, "Current location driver : "+DriverWelcomeActivity.myLocation.latitude+","+DriverWelcomeActivity.myLocation.longitude);
                Log.e("Driver Count " ,"" + dataSnapshot.getChildrenCount());
                data.clear();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Ride ride = dataSnapshot1.getValue(Ride.class);
                    if(ride.getStatus().equalsIgnoreCase("Searching") && ride.getVehicle().equalsIgnoreCase(currentDriver.getCab_type())) {
                        src[0] = new LatLng(Double.parseDouble(ride.getSource().getLat()), Double.parseDouble(ride.getSource().getLng()));
                        Log.d(TAG, "Got rider - email:"+ride.getRider());
                        String riderReference = ride.getRider();
                        Log.d(TAG, "Rider location "+src[0].latitude+","+src[0].longitude);
                        // Dist between current location of user and driver
                        Location.distanceBetween(DriverWelcomeActivity.myLocation.latitude, DriverWelcomeActivity.myLocation.longitude, src[0].latitude, src[0].longitude, answer);
                        Log.d(TAG, "Distance :"+answer[0]);
                        Log.d(TAG, "ADDDEEEEDDD RIIIDDDDEEEEE");
                        data.add(ride);
                        adapter.notifyDataSetChanged();
                    }
                }
                Log.d(TAG, "Size of data : "+data.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }
}

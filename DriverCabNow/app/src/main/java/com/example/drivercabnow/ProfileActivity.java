package com.example.drivercabnow;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.drivercabnow.Models.Driver;
import com.example.drivercabnow.Models.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ProfileActivity";
    TextView driverName, driverEmail, driverPhone, driverupiid;
    RatingBar driverRating;

    // Firebase variables
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_profile);

        // init firebase
        uid = FirebaseAuth.getInstance().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Drivers").child(uid);

        // init toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
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

        // init widgets
        initViews();

        // Get info from firebase db into User class and fill the TextViews
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Driver profile " + dataSnapshot.getValue());
                Driver driver = dataSnapshot.getValue(Driver.class);
                if(driver != null) {
                    fillText(driver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // update values here
        driverName.setOnClickListener(this);
        driverEmail.setOnClickListener(this);
        driverPhone.setOnClickListener(this);
        driverupiid.setOnClickListener(this);
    }

    private void fillText(Driver p) {
        driverName.setText(p.getDriver_name());
        driverEmail.setText(p.getDriver_email());
        driverPhone.setText(p.getDriver_phone());
        driverupiid.setText(p.getUPI_Id());
        driverRating.setRating(Float.parseFloat(p.getAverage_rating().toString()));
        driverRating.setIsIndicator(true);
    }

    private void initViews() {
        findViewById(R.id.tvFullname);
        findViewById(R.id.tvPhone);
        findViewById(R.id.tvEmail);
        findViewById(R.id.tvRating);
        findViewById(R.id.tvupiid);

        driverEmail = findViewById(R.id.tvDriveremail);
        driverName = findViewById(R.id.tvDrivername);
        driverRating = findViewById(R.id.ratingDriver);
        driverPhone = findViewById(R.id.tvDriverphone);
        driverupiid = findViewById(R.id.tvDriverupiid);
    }

    @Override
    public void onClick(View view) {
        // TODO inflate one custom AlertDialog layout with EditText
        // TODO handle all click events in a switch for [name, email, phone]

    }
}

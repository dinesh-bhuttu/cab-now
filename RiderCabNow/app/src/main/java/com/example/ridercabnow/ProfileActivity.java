package com.example.ridercabnow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.ridercabnow.MapActivities.WelcomeActivity;
import com.example.ridercabnow.models.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    TextView riderName, riderEmail, riderPhone;
    RatingBar riderRating;

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
        databaseReference = firebaseDatabase.getReference("Users").child(uid);
        Log.d(TAG, "onCreate: Database reference " + databaseReference);

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
                Log.d(TAG, "onDataChange: user profile " + dataSnapshot.getValue());
                Profile profile = dataSnapshot.getValue(Profile.class);
                if(profile != null) {
                    fillText(profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void fillText(Profile p) {
        riderName.setText(p.getRider_name());
        riderEmail.setText(p.getRider_email());
        riderPhone.setText(p.getRider_phone());
        riderRating.setRating(p.getAverage_Rating());
        riderRating.setIsIndicator(true);
    }

    private void initViews() {
        findViewById(R.id.tvFullname);
        findViewById(R.id.tvPhone);
        findViewById(R.id.tvEmail);
        findViewById(R.id.tvRating);

        riderEmail = findViewById(R.id.tvRideremail);
        riderName = findViewById(R.id.tvRidername);
        riderRating = findViewById(R.id.ratingRider);
        riderPhone = findViewById(R.id.tvRiderphone);
    }

}

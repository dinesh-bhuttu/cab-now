package com.example.ridercabnow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.ridercabnow.MapActivities.WelcomeActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    TextView riderName, riderEmail, riderPassword, riderPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });

        // init widgets
        initViews();

        // TODO get info from firebase db into User class and fill the TextViews


    }

    private void initViews() {
        findViewById(R.id.tvFullname);
        findViewById(R.id.tvPhone);
        findViewById(R.id.tvEmail);
        findViewById(R.id.tvPassword);

        riderEmail = findViewById(R.id.tvRideremail);
        riderName = findViewById(R.id.tvRidername);
        riderPassword = findViewById(R.id.tvRiderPassword);
        riderPhone = findViewById(R.id.tvRiderphone);
    }

}

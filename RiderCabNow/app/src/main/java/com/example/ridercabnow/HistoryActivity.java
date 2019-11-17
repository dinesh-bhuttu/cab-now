package com.example.ridercabnow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ridercabnow.Adapters.HistoryListAdapter;
import com.example.ridercabnow.RiderAuth.MainActivity;
import com.example.ridercabnow.models.HistoryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.type.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager manager;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Get toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Your Rides");
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


        // LIST
        ArrayList<HistoryItem> list = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        manager = new LinearLayoutManager(this);
        adapter = new HistoryListAdapter(list, HistoryActivity.this);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        progressBar = findViewById(R.id.progressBarHist);

        // set listener
        String uid = FirebaseAuth.getInstance().getUid();
        if(uid != null)
        {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("history");

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    progressBar.setVisibility(View.VISIBLE);

                    Log.d(TAG, "onDataChange: snapshot ->> " + dataSnapshot.getValue());

                    if(dataSnapshot.getValue() != null) {
                        // history branch exists
                        list.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            HistoryItem item = ds.getValue(HistoryItem.class);
                            list.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    else {
                        Toast.makeText(HistoryActivity.this, "You have no history",
                                Toast.LENGTH_LONG).show();
                    }

                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                }
            });
        }

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
                //Toast.makeText(this, "History selected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HistoryActivity.class));

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



}

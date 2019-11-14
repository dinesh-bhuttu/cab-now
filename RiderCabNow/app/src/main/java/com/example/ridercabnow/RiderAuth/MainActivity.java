package com.example.ridercabnow.RiderAuth;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ridercabnow.MapActivities.ChooseRideActivity;
import com.example.ridercabnow.MapActivities.TravelActivity;
import com.example.ridercabnow.MapActivities.WelcomeActivity;
import com.example.ridercabnow.R;
import com.example.ridercabnow.models.HistoryItem;
import com.example.ridercabnow.models.Latlng;
import com.example.ridercabnow.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button btnSignin, btnRegister;
    RelativeLayout rootLayout;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    DatabaseReference drivers;

    // shared pref variables
    String riderEmail = "", riderPassword = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_main);

        // Init firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        drivers = db.getReference("Drivers");


        //Init view
        btnRegister = findViewById(R.id.btnRegister);
        btnSignin = findViewById(R.id.btnSignin);
        rootLayout = findViewById(R.id.rootLayout);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });

        // get SharedPreference credentials into riderEmail and pass global variables
        getSharedPrefVars();
        if(doCredsExist()) {
            Log.d(TAG, "onCreate: " + riderEmail + " " + riderPassword);
            btnSignin.performClick();
        }

    }


    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register, null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);

        //Set button
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();


                // Check validation
                if(TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtPassword.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtPhone.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter Phone number", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtName.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter Full Name", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(edtPassword.getText().toString().length()<8){
                    Snackbar.make(rootLayout,"Password Length must be 8 or more", Snackbar.LENGTH_SHORT);
                    return;
                }

                final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
                waitingDialog.setTitle("Loading...");
                waitingDialog.show();

                // Register new user
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                // Save user to db
                                waitingDialog.dismiss();
                                User user = new User(edtEmail.getText().toString(),
                                        edtName.getText().toString(),
                                        edtPhone.getText().toString());


                                // Use email as key and password as value
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(aVoid -> Snackbar.make(rootLayout, "Registered Successfully !!", Snackbar.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Snackbar.make(rootLayout, "Registration failed. Please try again.\n"+e.getMessage(), Snackbar.LENGTH_SHORT).show());
                            }
                        })
                        .addOnFailureListener(e -> Snackbar.make(rootLayout, "Registration failed. Please try again.\n"+e.getMessage(), Snackbar.LENGTH_SHORT).show());

            }
        });
        dialog.setNegativeButton("CANCEL", (dialog1, which) -> dialog1.dismiss());
        dialog.show();
    }

    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to Sign in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login, null);
        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);

        dialog.setView(login_layout);

        // Set button for forgot password
        dialog.setNeutralButton("Forgot Password?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btnSignin.setEnabled(true);
                final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
                waitingDialog.setTitle("Loading...");
                waitingDialog.show();

                startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));

                finish();
            }
        });

        //Set button for login
        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Set disable sign in if processing
                btnSignin.setEnabled(true);

                String email = edtEmail.getText().toString();
                // Check validation
                if (TextUtils.isEmpty(email)) {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtPassword.getText().toString().length() < 8) {
                    Snackbar.make(rootLayout, "Password Length must be 8 or more", Snackbar.LENGTH_SHORT);
                    return;
                }

                final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
                waitingDialog.setTitle("Loading...");
                waitingDialog.show();



                final String[] user_email = new String[1];
                final int[] userFound = {0};
                // Check if Driver or not
                users.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Log.e("Count " ,""+snapshot.getChildrenCount());
                        for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                            User user = postSnapshot.getValue(User.class);
                            user_email[0] = user.getRider_email();
                            if(user_email[0].equals(email)){
                                userFound[0] =1;
                                break;
                            }
                        }
                        if(userFound[0]==0){
                            waitingDialog.dismiss();
                            Snackbar.make(rootLayout, "Email not registered as Rider :(",Snackbar.LENGTH_SHORT ).show();
                        }
                        else if(userFound[0]==1) {
                            auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            waitingDialog.dismiss();

                                            // Write shared prefs on successful login
                                            writeSharedPrefs(edtEmail.getText().toString(),
                                                    edtPassword.getText().toString());

                                            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            waitingDialog.dismiss();
                                            Snackbar.make(rootLayout, "Failed with message : " + e.getMessage(), Snackbar.LENGTH_SHORT).show();

                                            btnSignin.setEnabled(true);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("The read failed: " ,databaseError.getMessage());
                    }
                });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

        // check for global variables
        if(doCredsExist()) {
            edtEmail.setText(riderEmail);
            edtPassword.setText(riderPassword);
        }
    }


    private void writeSharedPrefs(String email, String password) {
        Log.d(TAG, "addToSharedPref: " + email + " " + password);
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }

    private void getSharedPrefVars() {
        riderEmail = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                .getString("email", "");
        riderPassword = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                .getString("password", "");
        Log.d(TAG, "Shared pref variables : " + riderEmail + " " + riderPassword);
    }

    private boolean doCredsExist() {
        return !riderEmail.equals("") && !riderPassword.equals("");
    }

}

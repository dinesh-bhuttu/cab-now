package com.example.drivercabnow;

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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.example.drivercabnow.DriverAuth.ResetPasswordActivity;
import com.example.drivercabnow.MapActivities.DriverWelcomeActivity;
import com.example.drivercabnow.Models.Driver;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button btnDriverLogin, btnDriverRegister;
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
        btnDriverLogin = findViewById(R.id.driverSignin);
        btnDriverRegister = findViewById(R.id.driverRegister);
        rootLayout = findViewById(R.id.rootLayout);

        btnDriverRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDriverRegisterDialog();
            }
        });
        btnDriverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDriverLoginDialog();
            }
        });

        // get SharedPreference credentials into riderEmail and pass global variables
        getSharedPrefVars();
        if(doCredsExist()) {
            Log.d(TAG, "onCreate: " + riderEmail + " " + riderPassword);
            btnDriverLogin.performClick();
        }
    }


    private void showDriverRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        dialog.setTitle("REGISTER");
        //dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.driver_layout_register, null);

        // Initialize all the input fields
        // Driver_email, Driver_name, Driver_phone, UPI_Id, Vehicle_no, Current_Location, License_no, Cab_type, Cab_status;

        // Do not need to store password
        final MaterialEditText edtDriver_email = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtDriver_name= register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtDriver_phone = register_layout.findViewById(R.id.edtPhone);
        final MaterialEditText edtUPI_id = register_layout.findViewById(R.id.edtUPIid);
        final MaterialEditText edtVehicle_no = register_layout.findViewById(R.id.edtVehicleNo);
        final MaterialEditText edtLicense_no = register_layout.findViewById(R.id.edtDriverLicenseID);

        RadioGroup RadioCab_types = register_layout.findViewById(R.id.cabtypes);
        int id1 = R.id.sedan;
        int id2 = R.id.micro;
        int id3 = R.id.auto;
        dialog.setView(register_layout);

        //Set button
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // Check validation
                if(TextUtils.isEmpty(edtDriver_email.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtLicense_no.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter valid License ID", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtUPI_id.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter valid UPI payment ID", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtVehicle_no.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter correct Vehicle Number", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtPassword.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtDriver_phone.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter Phone number", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtDriver_name.getText().toString())){
                    Snackbar.make(rootLayout, "Please enter Full Name", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(edtPassword.getText().toString().length()<8){
                    Snackbar.make(rootLayout,"Password Length must be 8 or more", Snackbar.LENGTH_SHORT);
                    return;
                }

                int selected = RadioCab_types.getCheckedRadioButtonId();
                String text;
                if(selected==id1){
                    text = "Sedan";
                }
                else if(selected==id2){
                    text = "Micro";
                }
                else
                    text="Auto";

                final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
                waitingDialog.setTitle("Loading...");
                waitingDialog.show();
                // Register new user
                //System.out.println(edtDriver_email.getText().toString());
                //System.out.println(edtUPI_id.getText().toString());
                auth.createUserWithEmailAndPassword(edtDriver_email.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingDialog.dismiss();
                                // Save user to db
                                Driver newDriver = new Driver(edtDriver_email.getText().toString(),
                                        edtDriver_name.getText().toString(),
                                        edtDriver_phone.getText().toString(),
                                        edtVehicle_no.getText().toString(),
                                        edtLicense_no.getText().toString(),
                                        text,
                                        edtUPI_id.getText().toString());

                                System.out.println(edtDriver_email.getText().toString());

                                // Use email as key and password as value
                                drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(newDriver)
                                        .addOnSuccessListener(aVoid -> Snackbar.make(rootLayout,
                                                edtDriver_email.getText().toString()+" - Registered Successfully !!",
                                                Snackbar.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Snackbar.make(rootLayout,
                                                "Registration failed. Please try again.\n"+e.getMessage(),
                                                Snackbar.LENGTH_SHORT).show());
                            }
                        })
                        .addOnFailureListener(e -> Snackbar.make(rootLayout,
                                "Registration failed. Please try again.\n"+e.getMessage(),
                                Snackbar.LENGTH_SHORT).show());

            }
        });
        dialog.setNegativeButton("CANCEL", (dialog1, which) -> dialog1.dismiss());
        dialog.show();
    }


    private void showDriverLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to Sign in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.driver_layout_login, null);
        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);

        dialog.setView(login_layout);

        // Set button for forgot password
        dialog.setNeutralButton("Forgot Password?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btnDriverLogin.setEnabled(true);
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
                btnDriverLogin.setEnabled(true);

                // Check validation
                String email = edtEmail.getText().toString();
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


                final String[] driver_email = new String[1];
                final int[] driverFound = {0};

                // Check if Driver or not
                drivers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Log.e("Count " ,""+snapshot.getChildrenCount());
                        for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                            Driver driver = postSnapshot.getValue(Driver.class);
                            driver_email[0] = driver.getDriver_email();
                            if(driver_email[0].equals(email)){
                                driverFound[0] =1;
                                break;
                            }
                        }
                        if(driverFound[0]==0){
                            waitingDialog.dismiss();
                            Snackbar.make(rootLayout, "Email not registered as Driver :(",Snackbar.LENGTH_SHORT ).show();
                        }
                        if(driverFound[0]==1) {
                            Log.d("The driver was found","The driver was found");
                            // Login
                            auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            waitingDialog.dismiss();

                                            // Write shared preferences
                                            writeSharedPrefs(edtEmail.getText().toString(),
                                                    edtPassword.getText().toString());
                                            startActivity(new Intent(MainActivity.this, DriverWelcomeActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            waitingDialog.dismiss();
                                            Snackbar.make(rootLayout, "Failed with message : " + e.getMessage(), Snackbar.LENGTH_SHORT).show();

                                            btnDriverLogin.setEnabled(true);
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

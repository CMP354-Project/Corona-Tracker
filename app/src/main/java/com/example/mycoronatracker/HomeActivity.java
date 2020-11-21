package com.example.mycoronatracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class HomeActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private ToggleButton coronaToggleButton;
    private ImageButton infoButton;
    FirebaseFirestore db;

    public static final int updateInterval  = 1000 * 60 * 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(updateInterval)
                .setFastestInterval(updateInterval);

        coronaToggleButton = (ToggleButton) findViewById(R.id.coronaToggleButton);
        infoButton = (ImageButton) findViewById(R.id.infoButton);

        coronaToggleButton.setOnCheckedChangeListener(this);
        infoButton.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();
        googleApiClient.connect();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Toast.makeText(this, "You have corona lol", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123)
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onConnected(new Bundle());
                Log.d("PERMISSION GRANTED", "");
            }
    }

    @Override
    public void onClick(View v) {
        String link = "https://www.cdc.gov";
        Uri uri = Uri.parse(link);
        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(websiteIntent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(
                            googleApiClient, locationRequest, this::onLocationChanged);
        }
        catch (SecurityException s){
            Log.d("Error","Not able to run location services...");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        HashMap<String,String> newlocation = new HashMap<>();
        newlocation.put("user", getIntent().getStringExtra("email"));
        newlocation.put("location", String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude()));
        db.collection("users")
                .add(newlocation)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("User entered", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("User entry failed", "Error adding document", e);
                    }
                });
    }
}
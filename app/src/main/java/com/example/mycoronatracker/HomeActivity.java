package com.example.mycoronatracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.pm.PackageManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class HomeActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private ToggleButton coronaToggleButton;
    private ImageButton infoButton;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        coronaToggleButton = (ToggleButton) findViewById(R.id.coronaToggleButton);
        infoButton = (ImageButton) findViewById(R.id.infoButton);

        coronaToggleButton.setOnCheckedChangeListener(this);
        infoButton.setOnClickListener(this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            Toast.makeText(this, "You have corona lol",Toast.LENGTH_SHORT).show();

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Intent intent = getIntent();
            Map<String, Object> user = new HashMap<>();
            user.put("user", intent.getStringExtra("email"));
            user.put("location", "Lovelace");
            user.put("born", 1815);
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

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
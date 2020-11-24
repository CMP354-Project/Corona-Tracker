package com.example.mycoronatracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.mycoronatracker.HomeActivity.visitedLocations;

public class MyLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    FirebaseFirestore db;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private String email = "";
    public static final int updateInterval  = 1000 * 60;


    @Override
    public void onCreate() {
        Log.d("News reader", "Service created");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(updateInterval)
                .setFastestInterval(updateInterval);
        db = FirebaseFirestore.getInstance();
        googleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("News reader", "Service started");
        email = intent.getStringExtra("email");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d("News reader", "Service destroyed");
        googleApiClient.disconnect();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123)
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onConnected(new Bundle());
                Log.d("PERMISSION GRANTED", "");
            }
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
        // stackoverflow https://stackoverflow.com/questions/8654990/how-can-i-get-current-date-in-android
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyy H:mm", Locale.getDefault());
        String formattedDate = df.format(c);
        HashMap<String, Object> newLocation = new HashMap<>();
        newLocation.put("latitude",location.getLatitude());
        newLocation.put("longitude",location.getLongitude());
        visitedLocations.add(newLocation);
        HashMap<String,Object> ussr = new HashMap<>();
        ussr.put("user", email);
        ussr.put("location", visitedLocations);
        ussr.put("date", formattedDate);
        try {
            CollectionReference usersRef = db.collection("users");
            db.collection("users")
                    .add(ussr)
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
        catch (Exception e) {

        }
    }
}
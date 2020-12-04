
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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import static com.example.mycoronatracker.LoginActivity.mAuth;

public class MyLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private FirebaseFirestore db;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    public static final int updateInterval = 1000 * 60 * 30; // every 30 seconds testing, 30 mins once testing is done.


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
        try {
            db.collection("users").document(mAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            visitedLocations = (List<HashMap<String, Object>>) document.get("location");
                        }
                    }
                }
            });

        } catch (Exception e) {
            Log.d("EXCEPTION", e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("News reader", "Service started");
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
        } catch (SecurityException s) {
            Log.d("Error", "Not able to run location services...");
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
        newLocation.put("latitude", location.getLatitude());
        newLocation.put("longitude", location.getLongitude());
        try {
            db.collection("users").document(mAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            visitedLocations = (List<HashMap<String, Object>>) document.get("location");
                            if (visitedLocations.size() >= 672) {
                                visitedLocations.remove(0);
                            }
                            visitedLocations.add(newLocation);
                            db.collection("users").document(mAuth.getCurrentUser().getEmail()).update("location", visitedLocations);
                            db.collection("users").document(mAuth.getCurrentUser().getEmail()).update("date", formattedDate);

                        } else {
                            mAuth.signOut();
                        }
                    } else {
                        mAuth.signOut();
                    }
                }
            });

        } catch (Exception e) {
            Log.d("EXCEPTION", e.getMessage());
        }
    }


}
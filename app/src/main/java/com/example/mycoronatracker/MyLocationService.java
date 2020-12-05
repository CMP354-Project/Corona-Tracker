
package com.example.mycoronatracker;

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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.mycoronatracker.HomeActivity.visitedLocations;
import static com.example.mycoronatracker.LoginActivity.mAuth;

public class MyLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private FirebaseFirestore db;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    public static final int updateInterval = 1000 * 30; // every 30 seconds for testing and demo, 30 minutes once testing is done.


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
                .setInterval(updateInterval) // set interval for getting current location
                .setFastestInterval(updateInterval);

        db = FirebaseFirestore.getInstance();
        googleApiClient.connect();

        // This code causes error in reading locations if not present
        try {
            db.collection("users").document(mAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            visitedLocations = (List<HashMap<String, Object>>) document.get("location"); // initialize visited locations with the array from Firestore
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

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) { // request permission function for getting locations if not allowed on the phone
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
                            googleApiClient, locationRequest, this::onLocationChanged); // request location updates periodically
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
                            if (visitedLocations.size() >= 672) { // 672 because that's the amount locations stored in two weeks
                                visitedLocations.remove(0); // remove oldest location
                            }
                            visitedLocations.add(newLocation); // add the new location
                            db.collection("users").document(mAuth.getCurrentUser().getEmail()).update("location", visitedLocations); // update location array
                            db.collection("users").document(mAuth.getCurrentUser().getEmail()).update("date", formattedDate); // update date and time of last location record

                        } else {
                            mAuth.signOut(); // sign out if document does not exist
                        }
                    } else {
                        mAuth.signOut(); // sign out if task did not execute successfully
                    }
                }
            });

        } catch (Exception e) {
            Log.d("EXCEPTION", e.getMessage());
        }
    }


}
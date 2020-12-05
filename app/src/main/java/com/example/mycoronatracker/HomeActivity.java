package com.example.mycoronatracker;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.example.mycoronatracker.LoginActivity.mAuth;

public class HomeActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private String infectionStatus = "Not Infected";
    private ToggleButton coronaToggleButton;
    private TextView emailDisplayTV;
    private TextView infectionStatusTV;
    private ImageButton infoButton;
    private FirebaseFirestore db;
    private List<HashMap<String, Object>> userLocations;
    public static List<HashMap<String, Object>> visitedLocations = new ArrayList<>(); // to store the locations the user visited in the last two weeks



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dev);

        coronaToggleButton = (ToggleButton) findViewById(R.id.coronaToggleButton_final); // Button to tell app you have been infected or if you have recovered
        infoButton = (ImageButton) findViewById(R.id.infoButton_final); // button to open CDC website

        emailDisplayTV=(TextView)findViewById(R.id.nameDisplayTV); // displays user
        infectionStatusTV=(TextView)findViewById(R.id.infectionStatusTV);
        coronaToggleButton.setOnCheckedChangeListener(this);
        infoButton.setOnClickListener(this);



        db = FirebaseFirestore.getInstance();

        Intent locationService = new Intent(this, MyLocationService.class);
        getApplicationContext().startService(locationService);

        Intent notificationService = new Intent(this, MyNotificationService.class);
        getApplicationContext().startService(notificationService);


        emailDisplayTV.setText(mAuth.getCurrentUser().getEmail());
        infectionStatusTV.setText(infectionStatus);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(
                R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut(); // signs user out
                finish(); // ends home activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Toast.makeText(this, "You have corona lol", Toast.LENGTH_SHORT).show();
            changeInfectionStatus(true); // changes infection status in database
            infectionStatus = "Infected";
            infectionStatusTV.setText(infectionStatus);
            checkLocations();
            coronaToggleButton.setBackgroundColor(Color.parseColor("#FF0000")); // changes colour of Toggle Button
        } else {
            changeInfectionStatus(false);
            infectionStatus = "Not Infected";
            infectionStatusTV.setText(infectionStatus);
            coronaToggleButton.setBackgroundColor(Color.parseColor("#2196F3"));
        }
    }

    public void changeInfectionStatus(boolean infected) { // changes infection status in database
        try {
            db.collection("users").document(mAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            db.collection("users").document(mAuth.getCurrentUser().getEmail()).update("infected", infected);

                        } else {
                            mAuth.signOut(); // sign out because the user is not properly signed in
                        }
                    } else {
                        mAuth.signOut(); // sign out because the user is not properly signed in
                    }
                }
            });

        } catch (Exception e) {
            Log.d("EXCEPTION", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.infoButton_final: // button to launch CDC website
                String link = "https://www.cdc.gov";
                Uri uri = Uri.parse(link);
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(websiteIntent);
                break;
        }
    }


    private class checkUser extends AsyncTask<String, Void, Boolean> { // background task to check if other users has been in contact with current user
        String userToTest;
        boolean hasCorona = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            userLocations = null; // reassign userLocations to null
            userToTest = strings[0];
            try {
                getLocations();
            }
            catch (Exception e) {
                Log.e("error retrieving", e.getMessage());
            }
            while (userLocations == null) {}
            Iterator i = visitedLocations.iterator();
            Iterator j = userLocations.iterator();
            HashMap<String, Object> currentLocation;
            HashMap<String, Object> locationToCompare;
            while (i.hasNext() && !hasCorona) { // iterates over current user
                currentLocation = (HashMap<String, Object>) i.next();
                while (j.hasNext() && !hasCorona) { // iterates over user to compare
                    locationToCompare = (HashMap<String, Object>) j.next();
                    float lat1 = Float.parseFloat(String.valueOf(currentLocation.get("latitude")));
                    float lng1 = Float.parseFloat(String.valueOf(currentLocation.get("longitude")));
                    float lat2 = Float.parseFloat(String.valueOf(locationToCompare.get("latitude")));
                    float lng2 = Float.parseFloat(String.valueOf(locationToCompare.get("longitude")));
                    float difference = distance(lat1, lng1, lat2, lng2);
                    if (difference <= 0.002f) { // two meters
                        db.collection("users").document(userToTest).update("notify", true); // update notify value in database to true
                    }
                }
            }
            return null;
        }

        private void getLocations() { // gets locations of the user we need to check on
            db.collection("users").document(userToTest).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    userLocations = (List<HashMap<String, Object>>) documentSnapshot.get("location");
                }
            });
        }
    }


    private void checkLocations() { // Gets all users that we need to compare
        try {
            db.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String userToTest = document.get("user").toString();
                                    if (!mAuth.getCurrentUser().getEmail().equals(userToTest)) {
                                        new checkUser().execute(userToTest);
                                    }
                                }
                            } else {
                                Log.d("ERROR", "Error getting users: ", task.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    // https://stackoverflow.com/questions/18170131/comparing-two-locations-using-their-longitude-and-latitude
    private float distance(float lat1, float lng1, float lat2, float lng2) { // returns distance between two locations in Kilometers

        float earthRadius = 6371;

        float dLat = (float) Math.toRadians(lat2 - lat1);
        float dLng = (float) Math.toRadians(lng2 - lng1);

        float sindLat = (float) Math.sin(dLat / 2);
        float sindLng = (float) Math.sin(dLng / 2);

        float a = (float) (Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)));

        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));

        float dist = earthRadius * c;

        return dist; // output distance, in KILOMETERS
    }
}
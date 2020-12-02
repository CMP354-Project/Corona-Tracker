package com.example.mycoronatracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.mycoronatracker.LoginActivity.mAuth;

public class HomeActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {


    private ToggleButton coronaToggleButton;
    private ImageButton infoButton;
    private Button mapButton;
    private FirebaseFirestore db;

    public static List<HashMap<String, Object>> visitedLocations = new ArrayList<>();


    public static final int updateInterval = 1000 * 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        coronaToggleButton = (ToggleButton) findViewById(R.id.coronaToggleButton);
        infoButton = (ImageButton) findViewById(R.id.infoButton);
        mapButton = (Button) findViewById(R.id.mapButton);

        coronaToggleButton.setOnCheckedChangeListener(this);
        infoButton.setOnClickListener(this);

        mapButton.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();

        Intent service = new Intent(this, MyLocationService.class);
        service.putExtra("email", getIntent().getStringExtra("email"));
        getApplicationContext().startService(service);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Toast.makeText(this, "You have corona lol", Toast.LENGTH_SHORT).show();
            changeInfectionStatus(true);
            checkLocations();
        } else {
            changeInfectionStatus(false);
        }
    }

    public void changeInfectionStatus(boolean infected) {
        try {
            db.collection("users").document(mAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            db.collection("users").document(mAuth.getCurrentUser().getEmail()).update("infected", infected);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.infoButton:
                String link = "https://www.cdc.gov";
                Uri uri = Uri.parse(link);
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(websiteIntent);
                break;
            case R.id.mapButton:
                Intent locMap = new Intent(this, GoogleMapActivity.class);
                startActivity(locMap);
                break;
        }
    }

    private class checkUser extends AsyncTask<String, Void, Boolean> {
        String userToTest;
        List<HashMap<String, Object>> userLocations = new ArrayList<>();

        @Override
        protected Boolean doInBackground(String... strings) {
            userToTest = strings[0];
            getUserLocations(userToTest);

            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }

        private void getUserLocations(String userToTest) {

            try {
                db.collection("users").document(userToTest).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Log.d("document exists", document.getId());
                            if (document.exists()) {
                                userLocations = (List<HashMap<String, Object>>) document.get("location");
                                Log.d("locations exists", String.valueOf(userLocations.size()));
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Log.d("ERROR", e.getMessage());
            }
        }
    }


    private void checkLocations() {
        try {
            db.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String userToTest = document.get("user").toString();
                                    if (!mAuth.getCurrentUser().getEmail().equals(userToTest))
                                        new checkUser().execute(userToTest);
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
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = earthRadius * c;

        return dist; // output distance, in KILOMETERS
    }


    private void sendNotification() {

    }
}
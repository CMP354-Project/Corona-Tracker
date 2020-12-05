package com.example.mycoronatracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.example.mycoronatracker.LoginActivity.mAuth;

public class HomeActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private String infectionStatus = "";
    private boolean infected;
    private ToggleButton coronaToggleButton;
    private TextView nameDisplayTV;
    private TextView infectionStatusTV;
    private ImageButton infoButton;
    private FirebaseFirestore db;
    private List<HashMap<String, Object>> userLocations;
    public static List<HashMap<String, Object>> visitedLocations = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dev);

        coronaToggleButton = (ToggleButton) findViewById(R.id.coronaToggleButton_final);
        infoButton = (ImageButton) findViewById(R.id.infoButton_final);

        nameDisplayTV=(TextView)findViewById(R.id.nameDisplayTV);
        infectionStatusTV=(TextView)findViewById(R.id.infectionStatusTV);
        coronaToggleButton.setOnCheckedChangeListener(this);
        infoButton.setOnClickListener(this);



        db = FirebaseFirestore.getInstance();

        Intent locationService = new Intent(this, MyLocationService.class);
        getApplicationContext().startService(locationService);

        Intent notificationService = new Intent(this, MyNotificationService.class);
        getApplicationContext().startService(notificationService);



        db.collection("users").document(mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                infected = (boolean) documentSnapshot.get("infected");
            }
        });

        if (infected) {
            infectionStatus = "Infected";
        }
        else {
            infectionStatus = "Not Infected";
        }
        nameDisplayTV.setText(mAuth.getCurrentUser().getEmail());
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
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Toast.makeText(this, "You have corona lol", Toast.LENGTH_SHORT).show();
            changeInfectionStatus(true);
            infectionStatus = "Infected";
            infectionStatusTV.setText(infectionStatus);
            checkLocations();
            coronaToggleButton.setBackgroundColor(Color.parseColor("#FF0000"));
        } else {
            changeInfectionStatus(false);
            infectionStatus = "Not Infected";
            infectionStatusTV.setText(infectionStatus);
            coronaToggleButton.setBackgroundColor(Color.parseColor("#2196F3"));
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
            case R.id.infoButton_final:
                String link = "https://www.cdc.gov";
                Uri uri = Uri.parse(link);
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(websiteIntent);
                break;
        }
    }


    private class checkUser extends AsyncTask<String, Void, Boolean> {
        String userToTest;
        boolean hasCorona = false;
        boolean userFound = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            userLocations = null;
            Log.e("here", "entered background function/task");
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
            currentLocation = (HashMap<String, Object>) i.next();
            Log.e("latitude", currentLocation.get("latitude").toString());
            HashMap<String, Object> locationToCompare;
            int count = 0;
            while (i.hasNext() && !hasCorona) {
                Log.e("here", "entered while loop");
                currentLocation = (HashMap<String, Object>) i.next();
                while (j.hasNext() && !hasCorona) {
                    locationToCompare = (HashMap<String, Object>) j.next();
                    float lat1 = Float.parseFloat(String.valueOf(currentLocation.get("latitude")));
                    float lng1 = Float.parseFloat(String.valueOf(currentLocation.get("longitude")));
                    float lat2 = Float.parseFloat(String.valueOf(locationToCompare.get("latitude")));
                    float lng2 = Float.parseFloat(String.valueOf(locationToCompare.get("longitude")));
                    float difference = distance(lat1, lng1, lat2, lng2);
                    Log.e("difference", String.valueOf(difference));
                    count++;
                    if (difference <= 0.002f) {
                        db.collection("users").document(userToTest).update("notify", true);
                    }
                }
            }
            Log.e("count", String.valueOf(count));
            return null;
        }

        private void getLocations() {
            db.collection("users").document(userToTest).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.e("accountToTest:", userToTest.toString());
                    userLocations = (List<HashMap<String, Object>>) documentSnapshot.get("location");
                    Log.e("gettingLocations", "Execution is now in getLocations()");
                }
            });
        }
    }


    private void checkLocations() {
        Log.e("here", "entered function");
        try {
            db.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.e("here", "entered task");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String userToTest = document.get("user").toString();
                                    Log.e("myAccount", mAuth.getCurrentUser().getEmail().toString());
                                    Log.e("userAccount", userToTest);
                                    if (!mAuth.getCurrentUser().getEmail().equals(userToTest)) {
                                        Log.e("here", "entered for loop");
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
    private float distance(float lat1, float lng1, float lat2, float lng2) {

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
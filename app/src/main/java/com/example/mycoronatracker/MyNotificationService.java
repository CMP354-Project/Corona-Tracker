package com.example.mycoronatracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.mycoronatracker.LoginActivity.mAuth;

public class MyNotificationService extends Service {

    private String name = "";
    private FirebaseFirestore db;
    private int NOTIFICATION_ID = 1;
    private Timer timer;
    private boolean isInfected = false;

    @Override
    public void onCreate() {
        startTimer();
        db = FirebaseFirestore.getInstance();
    }

    private void startTimer() {
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Log.e("stopping", "Service stopped");
                    stopTimer();
                    return;
                }
                db.collection("users").document(mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        isInfected = (boolean) documentSnapshot.get("notify");
                        if(isInfected)
                            sendNotification();
                        isInfected = false; //resets flag after notification has been sent
                        db.collection("users").document(mAuth.getCurrentUser().getEmail()).update("notify", isInfected);
                    }
                });
            }
        };

        timer = new Timer(true);
        int delay = 1000;
        int interval = 1000 * 20;
        timer.schedule(task, delay, interval);
    }

    private void stopTimer() {
        timer.cancel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification() {
        db.collection("users").document(mAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Log.d("document exists", document.getId());
                    if (document.exists()) {
                        name = document.get("name").toString();
                    }
                }
            }
        });
        int icon = R.drawable.ic_launcher_foreground;
        CharSequence tickerText = "Possible chance of COVID-19 infection!";
        CharSequence contentTitle = "RED ALERT!";
        CharSequence contentText = "Please update your infection status in the application and get tested as soon as possible";

        NotificationChannel notificationChannel =
                new NotificationChannel("myChannelID", "My Notifications", NotificationManager.IMPORTANCE_HIGH);

        NotificationManager manager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);


        Notification notification = new NotificationCompat
                .Builder(this, "myChannelID")
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setAutoCancel(true)
                .setChannelId("myChannelID")
                .build();

        manager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
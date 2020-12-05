package com.example.mycoronatracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private void startTimer() { // starts timer task
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
                            sendNotification(); // sends notification to user if he needs to be notified
                        isInfected = false; //resets flag after notification has been sent
                        db.collection("users").document(mAuth.getCurrentUser().getEmail()).update("notify", isInfected);
                    }
                });
            }
        };

        timer = new Timer(true);
        int delay = 1000; // checks immediately when app is launched
        int interval = 1000 * 20; // 20 seconds for testing and demo purposes, one hour if is in production
        timer.schedule(task, delay, interval);
    }

    private void stopTimer() {
        timer.cancel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification() { // sends notification to user
        int icon = R.drawable.ic_baseline_bubble_chart_24;
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
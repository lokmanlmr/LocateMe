package com.example.locateme.model;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.locateme.R;
import com.example.locateme.model.localdatabase.PhoneNumbers;
import com.example.locateme.view.MainActivity;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SendSmsAndLocation extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Repository repository;
    private ArrayList<PhoneNumbers> sendToNumbers = new ArrayList<>();
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        Log.v("tag", "service working");
        repository = new Repository(getApplication());
        LiveData<List<PhoneNumbers>> liveData = repository.getSendToNumbers();
        liveData.observeForever(new Observer<List<PhoneNumbers>>() {
            @Override
            public void onChanged(List<PhoneNumbers> phoneNumbers) {
                sendToNumbers.clear();
                sendToNumbers.addAll(phoneNumbers);

            }
        });

        createNotificationChannel();

        Notification notification = getNotification();

        ServiceCompat.startForeground(this, 10, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                // Execute your code here
                getLocation(new LocationCallback() {
                    @Override
                    public void onLocationReceived(Location location) {
                        // Handle the location here
                        if (location != null) {
                            for (PhoneNumbers number : sendToNumbers) {

                                sendSMS("" + number.getPhone_number(),
                                        preferences.getString("sentMsg","Google Maps Link: ") +"https://www.google.com/maps?q="+ location.getLatitude() + "," + location.getLongitude());

                            }
                        } else {
                            Log.v("tag", "My location is null");
                        }
                    }
                });
                // Schedule the runnable to run again after 15 minutes
                handler.postDelayed(this, preferences.getLong("period",15*60*1000));
                Log.v("ddd",""+preferences.getLong("period",15*60*1000));
                // 15 minutes * 60 seconds * 1000 milliseconds
            }

        };
        handler.post(runnable);


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        Log.v("tag", "service stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("MissingPermission")
    private void getLocation(LocationCallback callback) {

        CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(5000)
                .setMaxUpdateAgeMillis(2000).
                build();
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationProviderClient.getCurrentLocation(currentLocationRequest,
                cancellationTokenSource.getToken()).addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    callback.onLocationReceived(location);

                } else {
                    Log.v("tag", "problem");
                    callback.onLocationReceived(null);
                }
            }
        });

    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to send SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public Notification getNotification() {
        Intent intent1 = new Intent(this, MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, intent1, PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setColor(getColor(R.color.primaryColor3))
                .setOngoing(true)  //persistent notification!
                .setChannelId(CHANNEL_ID)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("Tracking Service")   //Title message top row.
                .setContentText("Service is running")  //message when looking at the notification, second row
                .build();  //finally build and return a Notification.
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ForegroundServiceChannel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription("Channel for Foreground Service");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    interface LocationCallback {
        void onLocationReceived(Location location);
    }


}

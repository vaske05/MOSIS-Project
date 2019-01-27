package com.mosisproject.mosisproject.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mosisproject.mosisproject.R;
import com.mosisproject.mosisproject.model.User;
import com.mosisproject.mosisproject.model.UserLocation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.os.IBinder;
import android.content.Intent;
import android.Manifest;
import android.location.Location;
import android.content.pm.PackageManager;
import android.app.Service;
import android.util.Log;
import android.widget.Toast;

public class TrackingService extends Service {

    private DatabaseReference databaseReference;
    private LocationCallback locationCallback;
    private LocationRequest request;
    private FusedLocationProviderClient locationClient;
    private NotificationManager notificationManager;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    private static final String TAG = TrackingService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
        requestLocationUpdates();
        Log.i(TAG, "Service onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationClient.removeLocationUpdates(locationCallback);
        unregisterReceiver(stopReceiver);
        Log.i(TAG, "Service onDestroyed");
    }

    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

// Create the persistent notification//
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_enabled_notif))
                .setAutoCancel(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.tracking_enabled);
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "User tracking", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            //notificationChannel.enableVibration(true);
            //notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert notificationManager != null;
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        assert notificationManager != null;
        notificationManager.notify(0 /* Request Code */,builder.build());
    }


    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

//Unregister the BroadcastReceiver when the notification is tapped//

            unregisterReceiver(stopReceiver);

//Stop the Service//

            stopSelf();
        }
    };



    //Initiate the request to track the device's location//

    private void requestLocationUpdates() {
        request = new LocationRequest();

//Specify how often your app should request the device’s location//
        request.setInterval(10000);

//Get the most accurate location data available//
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

//If the app currently has access to the location permission...//
        if (permission == PackageManager.PERMISSION_GRANTED) {
//...then request location updates//
            locationClient.requestLocationUpdates(request,locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

//Get a reference to the database, so your app can perform read and write operations//

                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
                    final Location location = locationResult.getLastLocation();
                    if (location != null) {
                        //Save the location data to the database
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final User user = dataSnapshot.getValue(User.class);
                                UserLocation userLocation = new UserLocation();

                                userLocation.setLatitude(location.getLatitude());
                                userLocation.setLongitude(location.getLongitude());

                                user.setUserLocation(userLocation);
                                databaseReference.setValue(user);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("Save Location Failed",databaseError.getMessage());
                            }
                        });
                    }
                }
            }, null);
        }
    }
}
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
import com.mosisproject.mosisproject.model.User;
import com.mosisproject.mosisproject.model.UserLocation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.os.IBinder;
import android.content.Intent;
import android.Manifest;
import android.location.Location;
import android.content.pm.PackageManager;
import android.app.Service;
import android.util.Log;

public class TrackingService extends Service {

    private DatabaseReference databaseReference;

    private static final String TAG = TrackingService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        requestLocationUpdates();
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
        LocationRequest request = new LocationRequest();

//Specify how often your app should request the device’s location//
        request.setInterval(10000);

//Get the most accurate location data available//
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

//If the app currently has access to the location permission...//
        if (permission == PackageManager.PERMISSION_GRANTED) {
//...then request location updates//
            client.requestLocationUpdates(request, new LocationCallback() {
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
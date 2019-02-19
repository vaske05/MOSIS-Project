package com.mosisproject.mosisproject.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mosisproject.mosisproject.R;
import com.mosisproject.mosisproject.filter.FilterHelper;
import com.mosisproject.mosisproject.model.Event;
import com.mosisproject.mosisproject.model.Friend;
import com.mosisproject.mosisproject.model.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FriendsLocationService {

    private MapboxMap mapboxMap;
    private Context context;

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;

    private List<Friend> userIdList = new ArrayList<>();
    private List<User> friendsList = new ArrayList<>();
    private List<Event> eventList = new ArrayList<>();

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();

    public FriendsLocationService(MapboxMap mapboxMap, Context context) {
        this.mapboxMap = mapboxMap;
        this.context = context;
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        user = firebaseAuth.getCurrentUser();
    }

    public void loadFriends() {
        firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();
                userIdList.clear();
                final User userRecord = dataSnapshot.child("Users").child(user.getUid()).getValue(User.class);
                eventList = userRecord.getEventList();
                userIdList = userRecord.getFriendsList();
                if (userIdList == null) return;
                userIdList.remove(0);
                for(int i = 0; i < userIdList.size(); i++) {
                    User friend = dataSnapshot.child("Users").child(userIdList.get(i).getFriendId()).getValue(User.class);
                    if (friend != null) {
                        addFriendToList(friend);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void loadEvents() {


    }

    public boolean DeleteEvent(Marker marker, String userId) {

        String id = new StringBuilder()
                .append(marker.getTitle())
                .append(marker.getPosition().getLongitude())
                .append(marker.getPosition().getLatitude())
                .toString();

        for (Event event: eventList
             ) {
            if (event.getEventId().equals(id))
            {
                eventList.remove(event);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("Users").child(userId).child("eventList").setValue(eventList);
                marker.remove();
                return true;
            }
        }
        return false;
    }




    private void addFriendToList(final User friend) {
        File localFile = null;
        try {
            localFile = File.createTempFile(friend.getId(), ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File localFileFinal = localFile;

        StorageReference sRef = storageReference.child("profile_images/" + friend.getId() + ".jpg");
        sRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bitmap = BitmapFactory.decodeFile(localFileFinal.getAbsolutePath());
                bitmap = bitmap.createScaledBitmap(bitmap, 70, 70, true);
                Icon icon = IconFactory.getInstance(context).fromBitmap(bitmap);
                friend.setMarkerIcon(icon);
                friendsList.add(friend);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ERROR", "Error when loading icon" + e.getMessage());
            }
        });
    }

    public void showLocationMarkers(){
        for(int i = 0; i < friendsList.size(); i++) {
            mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(friendsList.get(i).getUserLocation().getLatitude(), friendsList.get(i).getUserLocation().getLongitude()))
                    //.title(fullName + " Lat:" + lat.toString() + "Lng:" + lng.toString())
                    .icon(friendsList.get(i).getMarkerIcon())
                    .setTitle(friendsList.get(i).getName() + " " + friendsList.get(i).getSurname())
                    .setSnippet("Email:" + friendsList.get(i).getEmail() + "\n" + "Phone:" + friendsList.get(i).getPhone())
            );
        }
    }

    public void showEventsMarkers() {
        FilterHelper.getInstance().setEvents((ArrayList<Event>) eventList);
        ArrayList<Event> newList = FilterHelper.getInstance().FilterEvents();
        for(int i = 0; i < newList.size(); i++) {

            Icon icon = null;
            IconFactory iconFactory = IconFactory.getInstance(context);

            if (newList.get(i).getLocationType() == Event.LocationType.TAVERN) {
                icon = iconFactory.fromResource(R.drawable.green_marker);

            }
            else if (newList.get(i).getLocationType() == Event.LocationType.COFFEE_SHOP)
            {
                icon = iconFactory.fromResource(R.drawable.blue_marker);

            }

            String attenders = "";
            List<String> friendNames = newList.get(i).getFriendNames();
            for (int i1 = 0; i1 < friendNames.size() - 1; i1++) {
                String friendName = friendNames.get(i1);
                attenders += friendName + "\n";
            }

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(newList.get(i).getLatitude(), newList.get(i).longitude))
                    .setTitle(newList.get(i).getTitle())
                    .setSnippet("Description: " + newList.get(i).description + "\n" +
                            "Attenders: \n" + attenders);

            if (icon != null) markerOptions.icon(icon);

            mapboxMap.addMarker(markerOptions);
        }
    }

    //To stop timer
    public void stopTimer(){
        if(timer != null){
            timer.cancel();
            timer.purge();
        }
    }

    //To start timer
    public void startTimer(){
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run(){
                        clearMarkers();
                        loadFriends();
                        showLocationMarkers();
                    }
                });
            }
        };
        timer.schedule(timerTask, 2000, 2000);
    }

    public void clearMarkers() {
        List<Marker> markers = mapboxMap.getMarkers();
        for(int i = 0; i < markers.size(); i++) {
            markers.get(i).remove();
        }
    }

    public MapboxMap getMapboxMap() {
        return mapboxMap;
    }

    public void setMapboxMap(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }
}

package com.mosisproject.mosisproject.fragment;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mosisproject.mosisproject.R;
import com.mosisproject.mosisproject.adapter.EventFriendsAdapter;
import com.mosisproject.mosisproject.adapter.FriendsAdapter;
import com.mosisproject.mosisproject.model.Event;
import com.mosisproject.mosisproject.model.Friend;
import com.mosisproject.mosisproject.model.User;

import java.util.ArrayList;
import java.util.List;

public class AddEventFragment extends Fragment implements PermissionsListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    Location currentLocation;

    private PermissionsManager permissionsManager;

    private String userId;
    private ListView listViewEventFriends;
    private List<Friend> userIdList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();
    List<User> eventUsers;
    private EventFriendsAdapter eventFriendsAdapter;
    private User currentUser;

    private EditText placeNameText;
    private EditText descriptionText;

    public Button testBtn;
    private Boolean areFieldsValid;

    private ProgressBar spinner;

    private FusedLocationProviderClient mFusedLocationClient;
    MapboxMap mapboxMap;

    public AddEventFragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_event, container, false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        initCurrentLocation();


        getActivity().setTitle(R.string.navigation_event);

        //saveEventBtn = view.findViewById(R.id.buttonSaveEvent);
        placeNameText = view.findViewById(R.id.editTextPlaceName);
        descriptionText = view.findViewById(R.id.editTextDesc);

        testBtn = (Button) view.findViewById(R.id.saveEventBtn);

        listViewEventFriends = view.findViewById(R.id.eventFriendsList);
        spinner = (ProgressBar) view.findViewById(R.id.spinnerEvent);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        initCurrentUser();


        areFieldsValid = false;



        initButton();

        loadFriends(container);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //friendsAdapter.notifyDataSetChanged();
    }

    public void loadFriends(final ViewGroup container) {
        firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                spinner.setVisibility(View.VISIBLE);
                final User user = dataSnapshot.child("Users").child(userId).getValue(User.class);
                userIdList = new ArrayList<>(user.getFriendsList());
                userIdList.remove(0);
                for(int i = 0; i < userIdList.size(); i++) {
                    User friend = dataSnapshot.child("Users").child(userIdList.get(i).getFriendId()).getValue(User.class);
                    if (friend != null) {
                        userList.add(friend);
                    }
                }
                eventFriendsAdapter = new EventFriendsAdapter(container.getContext(), userList, getFragmentManager());
                listViewEventFriends.setAdapter(eventFriendsAdapter);
                eventFriendsAdapter.notifyDataSetChanged();
                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("GET USER ID LIST ERROR:", databaseError.getMessage());
                spinner.setVisibility(View.GONE);
            }
        });
    }

    private void verification() {
        if(TextUtils.isEmpty(placeNameText.getText())) {
            placeNameText.setError("Please enter place name.");
            placeNameText.requestFocus();
            areFieldsValid = false;
            return;
        }
        if(TextUtils.isEmpty(descriptionText.getText())) {
            descriptionText.setError("Please enter description.");
            descriptionText.requestFocus();
            areFieldsValid = false;
            return;
        }
        areFieldsValid = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionsManager != null) {
            permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /*@SuppressWarnings( {"MissingPermission"} )
    private Event setEventLocation(Event event) {
        if(PermissionsManager.areLocationPermissionsGranted(getContext())) {
            Location location = mapboxMap.getLocationComponent().getLastKnownLocation();
            event.setLatitude(location.getLatitude());
            event.setLongitude(location.getLongitude());
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
        return event;
    }*/

    private void initButton() {
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verification();
                if(areFieldsValid) {
                    eventUsers = new ArrayList<>(eventFriendsAdapter.getEventUsers());
                    Event event = new Event();
                    if(eventUsers.isEmpty()) {
                        Toast.makeText(getContext(), "Please select at least one friend", Toast.LENGTH_LONG).show();
                    } else {
                        eventUsers.add(currentUser);
                        event.setPlaceName(placeNameText.getText().toString());
                        event.setDescription(descriptionText.getText().toString());
                        event.setLatitude(currentLocation.getLatitude());
                        event.setLongitude(currentLocation.getLongitude());
                        //event.setAttendersList(eventUsers);
                        storeEvent(event);
                    }


                }
            }
        });
    }

    private void initCurrentUser() {
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void storeEvent(final Event event) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
       for(int i = 0; i < eventUsers.size(); i++) {
           eventUsers.get(i).addEvent(event);
           eventUsers.get(i).addPoints();
           databaseReference.child("Users").child(eventUsers.get(i).getId()).setValue(eventUsers.get(i));
       }
    }



    @SuppressWarnings( {"MissingPermission"} )
    private void initCurrentLocation() {
        if(PermissionsManager.areLocationPermissionsGranted(getContext())) {

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            currentLocation = location;
                        }
                    });
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }
}

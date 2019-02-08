package com.mosisproject.mosisproject.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mosisproject.mosisproject.fragment.AddEventFragment;
import com.mosisproject.mosisproject.fragment.RankingsFragment;
import com.mosisproject.mosisproject.model.User;
import com.mosisproject.mosisproject.module.GlideApp;
import com.mosisproject.mosisproject.R;
import com.mosisproject.mosisproject.fragment.AddFriendFragment;
import com.mosisproject.mosisproject.fragment.FriendsFragment;
import com.mosisproject.mosisproject.service.FriendsLocationService;
import com.mosisproject.mosisproject.service.TrackingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, PermissionsListener {

    private static final int PERMISSIONS_REQUEST = 100;

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseUser user;
    private SupportMapFragment mapFragment;
    public MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private MapboxMapOptions options;
    private FloatingActionButton mapSettingsBtn;
    private FloatingActionButton addObject;
    private SwitchCompat drawerSwitch;
    private SwitchCompat showFriendsLocationSwitch;
    private SwitchCompat realTimeLocationSwitch;
    private SwitchCompat showEventsLocationSwitch;
    private Dialog dialog;

    private FirebaseDatabase firebaseDatabase;
    private boolean switchChecked1;
    private boolean switchChecked2;
    private boolean switchChecked3;
    private FriendsLocationService friendsLocationService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        user = firebaseAuth.getCurrentUser();
        switchChecked1 = false;
        switchChecked2 = false;
        switchChecked3 = false;

        subscribeToTopics(user.getUid());

        Mapbox.getInstance(this, getString(R.string.access_token));

        if (savedInstanceState == null) {
            // Create fragment
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            LatLng patagonia = new LatLng(-52.6885, -70.1395);
            // Build mapboxMap
            options = new MapboxMapOptions();
            options.camera(new CameraPosition.Builder()
                    .target(patagonia)
                    .zoom(9)
                    .build());

            // Create map fragment
            mapFragment = SupportMapFragment.newInstance(options);

            // Add map fragment to parent container
            transaction.add(R.id.fragment_container, mapFragment, "com.mapbox.map");
            transaction.commit();
        } else {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        updateNavigationProfile(navigationView);

        friendsLocationService = new FriendsLocationService(mapboxMap, this);
        friendsLocationService.loadFriends();
        friendsLocationService.loadEvents();

        setupButtons(navigationView);

        InitLocationService();
        openFriendsFragment();
    }

    private void setupButtons(NavigationView navigationView) {
        //Tracking switch listener
        drawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.nav_tracking)
                .getActionView().findViewById(R.id.drawer_switch);
        drawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    startTrackerService();
                } else {
                    stopTrackingService();
                }
            }
        });
        //Map settings button listener
        mapSettingsBtn = (FloatingActionButton) findViewById(R.id.map_settings);
        mapSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(view.getContext());
                dialog.setContentView(R.layout.map_settings_dialog);
                dialog.setTitle("Map preferences");
                dialog.show();

                showFriendsLocationSwitch = dialog.findViewById(R.id.showFriendsLocation);
                realTimeLocationSwitch = dialog.findViewById(R.id.realTimeLocation);
                showEventsLocationSwitch = dialog.findViewById(R.id.eventsLocation);

                realTimeLocationSwitch.setClickable(switchChecked1);
                showFriendsLocationSwitch.setClickable(!switchChecked2);


                showFriendsLocationSwitch.setChecked(switchChecked1);
                realTimeLocationSwitch.setChecked(switchChecked2);
                showEventsLocationSwitch.setChecked(switchChecked3);


                showFriendsLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            switchChecked1 = isChecked;
                            startFriendsLocationService();
                            realTimeLocationSwitch.setClickable(true);
                        } else {
                            switchChecked1 = isChecked;
                            stopFriendsLocationService();
                            realTimeLocationSwitch.setClickable(false);
                        }

                    }
                });
                realTimeLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            switchChecked2 = isChecked;
                            friendsLocationService.startTimer();
                            showFriendsLocationSwitch.setClickable(false);
                        } else {
                            switchChecked2 = isChecked;
                            friendsLocationService.stopTimer();
                            showFriendsLocationSwitch.setClickable(true);
                        }
                    }
                });
                showEventsLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            friendsLocationService.loadFriends();
                            switchChecked3 = isChecked;
                            friendsLocationService.showEventsMarkers();
                        } else {
                            switchChecked3 = isChecked;
                            friendsLocationService.clearMarkers();
                        }
                    }
                });
            }
        });

        addObject = (FloatingActionButton) findViewById(R.id.add_object);
    }

    private void InitLocationService() {
        //Check whether GPS tracking is enabled//
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            finish();
        }
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        //If the location permission has been granted, then start the TrackerService//
        if (permission == PackageManager.PERMISSION_GRANTED) {
            //startTrackerService();
        } else {
        //If the app doesn’t currently have access to the user’s location, then request access//
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        //Show/hide floating button. Show only on map fragment.
        if(fragment.getTag()== "com.mapbox.map") {
            mapSettingsBtn.show();
            addObject.hide();
        } else {
            mapSettingsBtn.hide();
            addObject.hide();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.friends_list) {
            openFriendsFragment();

        } else if (id == R.id.add_friend) {
            openAddFriendFragment();

        } else if (id == R.id.ranking) {
            openRankingsFragment();

        } else if (id == R.id.nav_manage) {
            openMapFragment();
        } else if (id == R.id.nav_logout) {
            userLogout();
        } else if(id == R.id.add_event) {
            openAddEventFragment();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void userLogout() {
        stopTrackingService();
        firebaseAuth.signOut();
        finish(); // Closing activity
        startLoginActivity();
        Toast.makeText(MainActivity.this, "Log out successfully.", Toast.LENGTH_LONG).show();
    }

    private void updateNavigationProfile(NavigationView navigationView) {

        TextView fullNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.fullNameTextView);
        TextView emailTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.emailTextView);
        final ImageView imageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.navProfilePicture);

        //Update user full name and email
        fullNameTextView.setText(user.getDisplayName());
        emailTextView.setText(user.getEmail());

        // Update user photo
        // Moze i ovako da se prikaze slika
        /*
        //Update user photo
        File localFile = null;
        try {
            localFile = File.createTempFile(user.getUid(), ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File localFileFinal = localFile;

        StorageReference sRef = storageReference.child("profile_images/" + user.getUid() + ".jpg");

        sRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bitmap = BitmapFactory.decodeFile(localFileFinal.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
        */
        //Ovako je lakse i jednostavnije
        StorageReference sRef = storageReference.child("profile_images/" + user.getUid() + ".jpg");
        GlideApp.with(getApplicationContext())
                .load(sRef)
                .transform(new RoundedCorners(15))
                .into(imageView);
    }

    private void openAddEventFragment() {
        AddEventFragment addEventFragment = new AddEventFragment();
        //addEventFragment.setMapboxMap(mapboxMap);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, addEventFragment, "fragment_addEvent").commit();
    }

    private void openFriendsFragment() {
        FriendsFragment friendsFragment = new FriendsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, friendsFragment, "fragment_friends").commit();
        String tag = friendsFragment.getTag();
        Log.w("TAG:", tag);
    }

    private void openAddFriendFragment() {
        AddFriendFragment addFriendFragment = new AddFriendFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, addFriendFragment, "fragment_addFriend").commit();
    }

    private void openRankingsFragment() {
        RankingsFragment rankingsFragment = new RankingsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, rankingsFragment, "fragment_rankings").commit();
    }

    private void openMapFragment() {
        mapFragment = SupportMapFragment.newInstance(options);
        setTitle(R.string.navigation_map);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mapFragment, "com.mapbox.map").commit();

        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapFragment.onSaveInstanceState(outState);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Enable permissions", Toast.LENGTH_LONG).show();
    }
    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionsManager != null) {
            permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent();
            startTrackerService();
        } else {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startTrackerService() {
        startService(new Intent(this, TrackingService.class));
        //Notify the user that tracking has been enabled//
        Toast.makeText(this, "GPS tracking enabled", Toast.LENGTH_SHORT).show();
    }

    private void stopTrackingService() {
        stopService(new Intent(this, TrackingService.class));
        Toast.makeText(this, "GPS tracking disabled", Toast.LENGTH_SHORT).show();
    }

    private void startFriendsLocationService() {
        friendsLocationService.loadFriends();
        friendsLocationService.showLocationMarkers();
    }

    private void stopFriendsLocationService() {
        friendsLocationService.clearMarkers();
    }

    public void GetId()
    {

    }

    @Override
    public void onMapReady(@NonNull MapboxMap _mapboxMap) {
        MainActivity.this.mapboxMap = _mapboxMap;
        friendsLocationService.setMapboxMap(mapboxMap);
        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng point) {
                //mapboxMap.addMarker(new MarkerOptions().position(point).title(point.toString()));
                return true;
            }
        });


        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent();
            }
        });
    }
        @SuppressWarnings( {"MissingPermission"})
        private void enableLocationComponent() {
            // Check if permissions are enabled and if not request
            if (PermissionsManager.areLocationPermissionsGranted(this)) {
                // Get an instance of the component
                LocationComponent locationComponent = mapboxMap.getLocationComponent();
                // Activate
                locationComponent.activateLocationComponent(this, mapboxMap.getStyle());
                // Enable to make component visible
                locationComponent.setLocationComponentEnabled(true);
                // Set the component's camera mode
                locationComponent.setCameraMode(CameraMode.TRACKING);
                // Set the component's render mode
                locationComponent.setRenderMode(RenderMode.COMPASS);
            } else {
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(this);
            }
        }

    private void subscribeToTopics(String id) {
        FirebaseMessaging.getInstance().subscribeToTopic("radiusNotification-" + id);
        //Toast.makeText(getApplicationContext(), "Subscribed to eventConfirmationFor-" + mail, Toast.LENGTH_LONG).show();
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onStart() {
        super.onStart();
        mapFragment.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapFragment.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapFragment.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapFragment.onDestroy();
    }
}

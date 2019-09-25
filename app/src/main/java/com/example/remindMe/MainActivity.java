package com.example.remindMe;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.MenuItem;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.management.ManagementException;
import com.auth0.android.management.UsersAPIClient;
import com.auth0.android.result.UserProfile;
import com.example.remindMe.Locations.LocationService;
import com.example.remindMe.Locations.UserLocation;
import com.example.remindMe.Notifications.NotificationFragment;
import com.example.remindMe.Users.User;
import com.example.remindMe.Users.UserClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.GeoPoint;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String TAG = "MainActivity";
    public static User curUser;
    public static String userId;
    public static UserLocation mUserLocation;
    public AuthenticationAPIClient authenticationAPIClient;
    public UsersAPIClient usersClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadFragment(new WelcomeFragment()); //loads the first fragment
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        handlingBottomNavigationView();

        if(curUser == null){
            User user = new User();
            // restarts a singleton of the user that is authenticated
            ((UserClient)getApplicationContext()).setUser(user);
            curUser = ((UserClient)getApplicationContext()).getUser();
        }

        if (mUserLocation == null){
            mUserLocation = new UserLocation();
        }
        // Integration ot Auth0
        Auth0 auth0 = new Auth0(this);
        auth0.setOIDCConformant(true);

        String accessToken = getIntent().getStringExtra(LoginActivity.EXTRA_ACCESS_TOKEN);
        usersClient = new UsersAPIClient(auth0, accessToken);
        authenticationAPIClient = new AuthenticationAPIClient(auth0);
        getProfile(accessToken);
    }

    /**
     * this func sets and initialize the bottom navigation view
     */
    private void handlingBottomNavigationView(){
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setSelectedItemId(R.id.navigation_remind);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    /** implementation of the listener to the bottom navigation menu */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_remind:
                    fragment = new WelcomeFragment();
                    break;
                case R.id.navigation_profile:
                    fragment = new ProfileFragment();
                    break;
                case R.id.navigation_notifications:
                    fragment = new NotificationFragment();
                    break;
            }
            return loadFragment(fragment);
        }
    };


    /**
     * when the app is running, starts the user-locating process
     */
    @Override
    protected void onResume() {
        super.onResume();
        isMapsEnabled();
        if (mLocationPermissionGranted) {
            StartUserLocating();
        } else {
            getLocationPermission();
        }
    }

    /**
     * this method is switching fragments.
     * @param fragment - a Fragment typed object
     * @return true if switched successfully.
     */
    public boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * this method builds a message that guides the user turn on their GPS.
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "don't let access to location services");
            }
        }).create();
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * this method determines whether the current application the user is using has GPS enabled on
     * the device.
     * @return boolean value accordingly
     */
    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps(); // if the GPS isn't enabled, calls this method + returns false
            return false;
        }
        return true; // if the GPS is enabled returns true
    }

    /**
     * when the user grants gps locations permission, changes the boolean mLocationPermissionGranted
     * to true and start the user locating process, otherwise asks for user permission
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    StartUserLocating();
                } else {
                    getLocationPermission(); // ask user for explicit location permission
                }
            }
        }
    }

    /**
     * Request location permission, so that we can get the location of the device. The result of
     * the permission request is handled by a callback, onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        //checking if location permission has bean granted in the past
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            // a function that symbolizes using the application as intended. below - holder for future function.
            StartUserLocating();
        } else {
            //asking for permission to access fine location
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * runs after the user has accepted/denied access to GPS (for the case that is wasn't granted
     * in the past.
     * @param requestCode the code for the wanted access request
     * @param permissions
     * @param grantResults an array that keeps all prier permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 // some results exist, check if they granted permission
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    /**
     * this method deals with the actual user locating, saves the location to a geoPoint and updates
     * the userLocation object and database.
     */
    public void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            Log.d(TAG, "onComplete: latitude: " + location.getLatitude());
                            Log.d(TAG, "onComplete: longitude: " + location.getLongitude());

                            mUserLocation.setGeoPoint(geoPoint);
                            mUserLocation.setTimestamp(null);
                            ServerApi.getInstance().saveUserLocation(mUserLocation);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * this method enables the whole user locating process.
     */
    private void StartUserLocating(){
        mUserLocation.setUser(curUser);
        getDeviceLocation();
        startLocationService();
    }

    /**
     * checks that the user has location services enabled
     * @return boolean value accordingly
     */
    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    /**
     * stats the location service that gets the current location of the user while the app is running
     */
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                MainActivity.this.startForegroundService(serviceIntent); // dealing w. different APIs
                Log.d(TAG, "startLocationService: location now service running.");

            }else{
                startService(serviceIntent);
                Log.d(TAG, "startLocationService: location now service running.");
            }
        }
    }

    /**
     * after the user is authenticated vie Auth0, retrieves user info and inserts it into a static
     * user object (solution to problem with migration of user data from Auth0 to Firebase)
     * @param accessToken access token to give to the authentication api client object
     */
    private void getProfile(String accessToken) {
        authenticationAPIClient.userInfo(accessToken)
                .start(new BaseCallback<UserProfile, AuthenticationException>() {
                    @Override
                    public void onSuccess(UserProfile userinfo) {
                        usersClient.getProfile(userinfo.getId())
                                .start(new BaseCallback<UserProfile, ManagementException>() {
                                    @Override
                                    public void onSuccess(UserProfile profile) {
                                        //userId = profile.getId();
                                        userId = "MfpY2xH87aV84GzFluqv";//todo:change!
                                        curUser.setName(profile.getName());
                                        curUser.setEmail(profile.getEmail());
                                        curUser.setId(userId);
                                        ServerApi.getInstance().updateUser(curUser);

                                    }

                                    @Override
                                    public void onFailure(ManagementException error) {
                                        Log.d(TAG, "fail to get user profile - ManagementException");
                                    }
                                });
                    }
                    @Override
                    public void onFailure(AuthenticationException error) {
                        Log.d(TAG, "fail to get user profile - AuthenticationException"
                                + error.getDescription());
                    }
                });
    }



}


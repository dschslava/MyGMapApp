package com.example.stephenyang.mygmapapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.location.LocationManager;
import android.location.LocationListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.location.LocationProvider.AVAILABLE;
import static android.location.LocationProvider.OUT_OF_SERVICE;
import static android.location.LocationProvider.TEMPORARILY_UNAVAILABLE;
import static com.example.stephenyang.mygmapapp.R.id.editSearch;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Geocoder geocoder;
    private List<Address> myList;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private boolean tracking = true;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private static final float MY_LOC_ZOOM_FACTOR = 17;
    private Location myLocation;
    private LatLng userLocation;
    private LatLng poi;
    private LatLng closest;
   // private int colour;
    EditText locationSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationSearch = (EditText) findViewById(editSearch);
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    /*
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near eh.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


@Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in birthplace and move the camera
        final LatLng birth = new LatLng(41.805609, -87.920693);
        mMap.addMarker(new MarkerOptions().position(birth).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(birth));
        try {
            mMap.setMyLocationEnabled(false);

        } catch (SecurityException s) {
            Log.d("MyGMap", "no location permissions");
        }

    }

    public void changeMapView(View v) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        Log.d("MyGMap", "changing map view");
    }

    public void getLocation(View v) {
        if (tracking) {
            try{
                tracking = false;
                mMap.setMyLocationEnabled(true);
                Log.d("MyGMap", "location tracking");
                try {
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    //get gps and network status
                    isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    if (isGPSEnabled) {
                        Log.d("MyGMap", "getlocation() GPS enabled");
                    }
                    if (isNetworkEnabled) {
                        Log.d("MyGMap", "getlocation() Network enabled");
                    }
                    if (!isGPSEnabled && !isNetworkEnabled) {
                        Log.d("MyGMap", "enable your location stuff mate there are nO PROVIDERS");
                    } else {
                        this.canGetLocation = true;

                        if (isGPSEnabled) {
                            Log.d("MyGMap", "getlocation() GPS enabled, requesting loc updates");
                            try {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                        MIN_TIME_BW_UPDATES,
                                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);

                                Log.d("MyGMap", "getLocation() gps location update successful");
                                Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);
                            } catch (SecurityException s) {
                                Log.d("MyGMap", "se getlocation gps");
                            }
                        }

                        if (isNetworkEnabled) {
                            Log.d("MyGMap", "getlocation() Network enabled, requesting loc updates");
                            try {
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                        MIN_TIME_BW_UPDATES,
                                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                                Log.d("MyGMap", "getLocation() network location update successful");
                                Toast.makeText(this, "Using network for location", Toast.LENGTH_SHORT);
                            } catch (SecurityException s) {
                                Log.d("MyGMap", "se getlocation net");
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.d("MyGMap", "Exception caught in getLocation()");
                    e.printStackTrace();
                }
            }
            catch (SecurityException s){

            }

        }
        else{

            try {
                tracking = true;
                mMap.setMyLocationEnabled(false);
                locationManager.removeUpdates(locationListenerGPS);
                locationManager.removeUpdates(locationListenerNetwork);
                locationManager = null;
                Log.d("MyGMap", "location not tracking (off)");


            }
            catch (SecurityException s){

            }
        }

    }

    android.location.LocationListener locationListenerGPS =
            new android.location.LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    //output in log d that gps works
                        //colour = Color.RED;
                        dropMarker(location, Color.RED);
                    Log.d("MyGMap", "red marker");
                    //remove network location updates. (see api)
                    try {
                        locationManager.removeUpdates(locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    }
                    catch (SecurityException s){

                    }

                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                    //same comments as above
                    Log.d("MyGMap", "gps status changed");
                    //switch statement to check status input parameter
                    switch (status) {
                        case AVAILABLE:
                            Log.d("MyGMap", "GPS available");
                            try {
                              //  colour = Color.RED;
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                        MIN_TIME_BW_UPDATES,
                                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);

                                Log.d("MyGMap", "getLocation() gps location update successful");
                            } catch (SecurityException s) {
                                Log.d("MyGMap", "se getlocation gps");
                            }
                            break;
                        case OUT_OF_SERVICE:
                            Log.d("MyGMap", "GPS out of service");
                            try {
                             //   colour = Color.BLUE;
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                        MIN_TIME_BW_UPDATES,
                                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                                Log.d("MyGMap", "getLocation() network location update successful");
                            } catch (SecurityException s) {
                                Log.d("MyGMap", "se getlocation net");
                            }
                            break;
                        case TEMPORARILY_UNAVAILABLE:
                            Log.d("MyGMap", "GPS temporarily unavailable");
                            try {
                            //    colour = Color.BLUE;
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                        MIN_TIME_BW_UPDATES,
                                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                                Log.d("MyGMap", "getLocation() network location update successful");
                            } catch (SecurityException s) {
                                Log.d("MyGMap", "se getlocation net");
                            }
                            break;
                    }

                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

    android.location.LocationListener locationListenerNetwork =
            new android.location.LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    //output in log d that gps works
                  //  colour = Color.BLUE;

                    dropMarker(location, Color.BLUE);
                    Log.d("MyGMap", "blue marker");
                    //locationManager.removeUpdates(locationListenerGPS);
                    //relaunch network provider request (requestLocationUpdates)
                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    catch (SecurityException s){

                    }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            //same comments as above
            Log.d("MyGMap", "network status changed");
            //switch to gps
            switch(status){
                case AVAILABLE: Log.d("MyGMap", "Network available");
                    try {
                        //colour = Color.RED;
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                        Log.d("MyGMap", "getLocation() network location update successful");
                    }
                    catch (SecurityException s){
                        Log.d("MyGMap", "se getlocation network");
                    }
                    break;
                case OUT_OF_SERVICE: Log.d("MyGMap", "Network out of service");
                    try {
                        //colour = Color.RED;
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);

                        Log.d("MyGMap", "getLocation() gps location update successful");
                    }
                    catch (SecurityException s){
                        Log.d("MyGMap", "se getlocation gps");
                    }
                    break;
                case TEMPORARILY_UNAVAILABLE: Log.d("MyGMap", "Network temporarily out of service");
                    try {
                        //colour = Color.RED;

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);

                        Log.d("MyGMap", "getLocation() gps location update successful");
                    }
                    catch (SecurityException s){
                        Log.d("MyGMap", "se getlocation gps");
                    }
                    break;
            }

        }

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };
    public void dropMarker(Location location, int colour){
        myLocation = location;
        if(myLocation == null){
            //Display a message vua log.d or toast
        }else{
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            Circle circle = mMap.addCircle(new CircleOptions().center(userLocation).
                    radius(3).strokeColor(colour).strokeWidth(1).fillColor(colour));
            CameraUpdate update = CameraUpdateFactory
                    .newLatLngZoom(userLocation,MY_LOC_ZOOM_FACTOR);
            mMap.animateCamera(update);

        }
    }

    public void searchLocation(View v) throws IOException {
        locationSearch = (EditText) findViewById(editSearch);
        //String address = "Canyon Crest Academy, San Diego, CA";
        //send search query to gmaps api

       // Geocoder.geocode();
        //https://maps.googleapis.com/maps/api/place/textsearch/
        // json?query=123+main+street&key=AIzaSyCZROy6KjapgRHe5a7tggu86MWmUBeDUAk
        //will return lat long
        //drop marker
        //move camera
        if (!locationSearch.getText().toString().isEmpty()) {
            mMap.clear();
            try {
                mMap.setMyLocationEnabled(true);
                Log.d("MyGMap", "search setlocationenabled true");

            } catch (SecurityException s) {

            }

            geocoder = new Geocoder(this, Locale.US);
            try{
                Log.d("MyGMap", "myLocation add just entering try");

               // myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (myLocation == null){
                    myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                Log.d("MyGMap", "myLocation added");

            }
            catch (SecurityException s){

            }

            if (geocoder.isPresent()) {
                Log.d("MyGMap", locationSearch.getText().toString());
                try {
                    Log.d("MyGMap", "geocoder present");
                    myList = geocoder.getFromLocationName(locationSearch.getText().toString(), 5,
                            myLocation.getLatitude() - 0.07246377,
                            myLocation.getLongitude() - 0.09157509,
                            myLocation.getLatitude() + 0.07246377,
                            myLocation.getLongitude() + 0.09157509);

                    Log.d("MyGMap", "geocoder through");
                    if (myList.size() == 0) {
                        return;
                    }
                    for (int i = 0; i < myList.size(); i++) {
                        Log.d("MyGMap", "geocoder for loop");
                        closest = new LatLng(myList.get(0).getLatitude(), myList.get(0).getLatitude());
                        poi = new LatLng(myList.get(i).getLatitude(), myList.get(i).getLongitude());
                        mMap.addMarker(new MarkerOptions().position(poi)
                                .title(locationSearch.getText().toString()));
                    }
                } catch (IOException se) {
                    Log.d("MyGMap", "SE gecodoer");

                }

                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(poi, MY_LOC_ZOOM_FACTOR);
                mMap.animateCamera(update);
            } else {

            }
        }
    }


    public void clearMarkers(View v){

        mMap.clear();
    }

}


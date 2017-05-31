package com.example.stephenyang.mygmapapp;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.location.LocationManager;
import android.location.LocationListener;
import android.widget.Toast;

//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.location.LocationProvider.AVAILABLE;
import static android.location.LocationProvider.OUT_OF_SERVICE;
import static android.location.LocationProvider.TEMPORARILY_UNAVAILABLE;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
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
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
//
//        }
        try {
            mMap.setMyLocationEnabled(true);

        }
        catch (SecurityException s){
            Log.d("MyGMap", "no location permissions");
        }

    }

    public void changeMapView(View v){
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else{
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        Log.d("MyGMap", "changing map view");
    }

    public void getLocation(View v){
        try{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //get gps and network status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
/*
            LocationListener locationListenerNetwork = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                            location.getLongitude())));
                    Log.d("MyGMap", "network location marked");

                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

           LocationListener locationListenerGPS = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                            location.getLongitude())));
                    Log.d("MyGMap", "gps location marked");
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.d("MyGMap", "gps status changed");
                }

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };*/

            if (isGPSEnabled){
                Log.d("MyGMap", "getlocation() GPS enabled");
            }
            if (isNetworkEnabled){
                Log.d("MyGMap", "getlocation() Network enabled");
            }
            if (!isGPSEnabled && !isNetworkEnabled){
                Log.d("MyGMap", "enable your location stuff mate there are nO PROVIDERS");
            } else {
                this.canGetLocation = true;

                if (isNetworkEnabled){
                    Log.d("MyGMap", "getlocation() Network enabled, requesting loc updates");
                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                        Log.d("MyGMap", "getLocation() network location update successful");
                        Toast.makeText(this, "Using network for location", Toast.LENGTH_SHORT);
                    }
                    catch (SecurityException s){
                        Log.d("MyGMap", "se getlocation net");
                    }
                }

                if (isGPSEnabled){
                    Log.d("MyGMap", "getlocation() GPS enabled, requesting loc updates");
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);

                        Log.d("MyGMap", "getLocation() gps location update successful");
                        Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);
                    }
                    catch (SecurityException s){
                        Log.d("MyGMap", "se getlocation gps");
                    }
                }
            }

        }
        catch (Exception e){
            Log.d("MyGMap", "Exception caught in getLocation()");
            e.printStackTrace();
        }
    }
    android.location.LocationListener locationListenerGPS = new android.location.LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            //output in log d that gps works
            dropMarker(location);

            //remove network location updates. (see api)
            locationManager.removeUpdates(locationListenerNetwork);

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            //same comments as above
            Log.d("MyGMap", "gps status changed");
            //switch statement to check status input parameter
            //case LocationProvider.AVAILABLE ->output Log.d, toast
            //case LocationProvider.OUT_OF_SERVICE ->output log d, request updates from network
            //case LocationPovider.TEMPOARILY_UNAVAILABLE -> same ^
            //case default ^
            switch(status){
                case AVAILABLE: Log.d("MyGMap", "GPS available");
                    break;
                case OUT_OF_SERVICE: Log.d("MyGMap", "GPS out of service");
                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                        Log.d("MyGMap", "getLocation() network location update successful");
                    }
                    catch (SecurityException s){
                        Log.d("MyGMap", "se getlocation net");
                    }
                    break;
                case TEMPORARILY_UNAVAILABLE: Log.d("MyGMap", "GPS temporarily unavailable");
                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                        Log.d("MyGMap", "getLocation() network location update successful");
                    }
                    catch (SecurityException s){
                        Log.d("MyGMap", "se getlocation net");
                    }
                    break;
            }

        }

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener(){
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            //output in log d that gps works
            dropMarker(location);

            //relaunch network provider request (requestLocationUpdates)
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                Log.d("MyGMap", "getLocation() network location update successful");
            }
            catch (SecurityException s){
                Log.d("MyGMap", "se getlocation net");
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            //same comments as above
            Log.d("MyGMap", "network status changed");
            //switch to gps
            switch(status){
                case AVAILABLE: Log.d("MyGMap", "Network available");
                    break;
                case OUT_OF_SERVICE: Log.d("MyGMap", "Network out of service");
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);

                        Log.d("MyGMap", "getLocation() gps location update successful");
                    }
                    catch (SecurityException s){
                        Log.d("MyGMap", "se getlocation gps");
                    }
                    break;
                case TEMPORARILY_UNAVAILABLE: Log.d("MyGMap", "Network out of service");
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
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
    public void dropMarker(Location location){
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                location.getLongitude())));
        Log.d("MyGMap", "location marked");
    }
}

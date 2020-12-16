package com.example.locationtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient = null;
    private Geocoder geocoder;
    private LocationCallback locationCallback = null;

    private static final int FINE_LOCATION_REQUEST_IDENTIFIER = 101;

    private TextView coordinatesView;
    private TextView addressView;
    private String longitude;
    private String latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.coordinatesView = (TextView)findViewById(R.id.coordinatesTextView);
        this.addressView = (TextView)findViewById(R.id.adressTextView);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_IDENTIFIER);
        } else {
            Toast.makeText(getApplicationContext(),"Has location permission",Toast.LENGTH_SHORT).show();
            setupLocationTracking();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_REQUEST_IDENTIFIER: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(),"Location permission GRANTED",Toast.LENGTH_SHORT).show();
                    setupLocationTracking();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(),"Location permission DENIED",Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @SuppressLint("MissingPermission")
    private void setupLocationTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = Geocoder.isPresent() ? new Geocoder(this) : null; // Create instance of Geocoder class

        addressView.setText("Finding your current location...");
        // Request to find current location
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(getApplicationContext(),"ERROR! Can't get location!",Toast.LENGTH_LONG).show();
                    return;
                }

                for (Location locationF : locationResult.getLocations()) {
                    if (locationF != null) {
                        writeLocationTextView(locationF);
                    }
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        // The commented code under is not used because it only searches for a cached location
        /*fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Toast.makeText(getApplicationContext(),"Using cached location",Toast.LENGTH_SHORT).show();

                            writeLocationTextView(location);
                        }
                        else {
                            // This runs if a cached location doesn't exist
                            Toast.makeText(getApplicationContext(),"Using current",Toast.LENGTH_SHORT).show();

                            LocationRequest locationRequest = LocationRequest.create();
                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            locationRequest.setInterval(10000);
                            locationRequest.setFastestInterval(5000);

                            locationCallback = new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    if (locationResult == null) {
                                        return;
                                    }

                                    for (Location locationF : locationResult.getLocations()) {
                                        if (locationF != null) {
                                            writeLocationTextView(locationF);
                                        }
                                    }
                                }
                            };
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                        }
                    }
                });*/
    }

    private void writeLocationTextView(Location location) {
        if (geocoder  != null) {
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    addressView.setText(address.getAddressLine(0));

                    latitude = Double.toString(location.getLatitude());
                    longitude = Double.toString(location.getLongitude());
                    coordinatesView.setText("Latitude: " + latitude + " | " + "Longitude: " + longitude);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            addressView.setText("Geocoder null error");
        }
    }
}
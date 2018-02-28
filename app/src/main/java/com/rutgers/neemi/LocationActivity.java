package com.rutgers.neemi;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.api.client.util.DateTime;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.model.Album;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.PlaceHasCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AppCompatActivity {

    private static final String TAG = "LocationActivity";
    LocationManager mlocManager;
    AlertDialog.Builder dialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Intent myIntent = new Intent(this, LocationService.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startService(myIntent);


//        dialog = new AlertDialog.Builder(this);
//
//        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        checkLocation();
//
//        Location earlierLocation = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        Location earlierLocation2 = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        if (earlierLocation == null) {
//            earlierLocation = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//        } else {
//
//        }
//
//        double lat = earlierLocation.getLatitude();
//        double lon = earlierLocation.getLongitude();
//        float accuracy = earlierLocation.getAccuracy();
//
//        TextView textView = (TextView) findViewById(R.id.gpsPosition);
//        textView.setText("My current gps location is: \n" +
//                "Latitude = " + lat + "\n" +
//                "Longitude = " + lon + "\n" +
//                "Accuracy = " + accuracy + " m");

//        Location earlierNetworkLocation = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        double lat2 = earlierNetworkLocation.getLatitude();
//        double lon2 = earlierNetworkLocation.getLongitude();
//        float accuracy2 = earlierNetworkLocation.getAccuracy();
//
//        TextView textNetView = (TextView) findViewById(R.id.networkPosition);
//        textNetView.setText("My current network location is: \n" +
//                "Latitude = " + lat2 + "\n" +
//                "Longitude = " + lon2 + "\n" +
//                "Accuracy = " + accuracy2 + " m");


//        LocationListener mlocListener = new MyLocationListener();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
    }


//    private boolean checkLocation() {
//        if (!isLocationEnabled()) {
//            showAlert();
//        }
//        return isLocationEnabled();
//    }
//
//
//    private boolean isLocationEnabled() {
//        return mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//    }
//
//    public class MyLocationListener implements LocationListener {
//        @Override
//        public void onLocationChanged(Location loc) {
//            double lat = loc.getLatitude();
//            double lon = loc.getLongitude();
//            float accuracy = loc.getAccuracy();
//
//            TextView textView = (TextView) findViewById(R.id.gpsPosition);
//            textView.setText("My current location is: \n" +
//                    "Latitude = " + lat + "\n" +
//                    "Longitude = " + lon + "\n" +
//                    "Accuracy = " + accuracy + " m");
//
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//            Toast.makeText(getApplicationContext(), "GPS Disabled", Toast.LENGTH_SHORT).show();
//
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//            Toast.makeText(getApplicationContext(), "GPS Enabled", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//    }
//
//
//    private void showAlert() {
//        dialog.setTitle("Enable Location")
//                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable GPS and Network Location for better results by " +
//                        "using this app")
//                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivity(myIntent);
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                        paramDialogInterface.cancel();
//                    }
//                });
//        AlertDialog alert = dialog.create();
//        alert.show();
//    }


}



package com.rutgers.neemi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.events.OpenFileCallback;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.DateTime;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PhotoResult;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.GPSLocation;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.PlaceHasCategory;
import com.rutgers.neemi.model.StayPoint;
import com.rutgers.neemi.model.StayPointHasPlaces;
import com.rutgers.neemi.model.Transaction;
import com.rutgers.neemi.model.TransactionHasCategory;
import com.rutgers.neemi.parser.BankDescriptionParser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * An abstract activity that handles authorization and connection to the Drive
 * services.
 */
public class GMapsDriveActivity extends AppCompatActivity {

    GeoApiContext geoApiContext = new GeoApiContext.Builder()
            .apiKey("AIzaSyCwsSXnT_jN107mMLhz55vo7JKwREaflJQ")
            .build();

    private static final String TAG = "Google Maps Drive Activity";
    Double overflow = Double.parseDouble("4294967296");


    /**
     * Request code for google sign-in
     */
    protected static final int REQUEST_CODE_SIGN_IN = 0;


    /**
     * Request code for the Drive picker
     */
    protected static final int REQUEST_CODE_OPEN_ITEM = 1;

    /**
     * Handles high-level drive functions like sync
     */
    private DriveClient mDriveClient;

    /**
     * Handle access to Drive resources/files.
     */
    private DriveResourceClient mDriveResourceClient;

    /**
     * Tracks completion of the drive picker
     */
    private TaskCompletionSource<DriveId> mOpenItemTaskSource;

    public DriveFile file;
    RuntimeExceptionDao<GPSLocation, String> gpsDao;

    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
    DatabaseHelper helper;
    RuntimeExceptionDao<StayPoint, String> stayPointDao;
    int items=1;
    private ProgressBar progressBar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmaps);
        helper=DatabaseHelper.getHelper(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);



        // Set the onClick listeners for the button bar.
        progressBar = findViewById(R.id.determinateBar);


        // Authenticate the user. For most apps, this should be done when the user performs an
        // action that requires Drive access rather than in onCreate.
        //requestSignIn();



//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_gmaps);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ActionBar ab = getSupportActionBar();
//
//        // Enable the Up button
//        mProgress = new ProgressDialog(this);
//        mProgress.setMessage("Getting your GPS locations ...");
//        mProgress.setIndeterminate(true);
//        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        mProgress.show();


        GoogleSignInClient mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);

        gpsDao = helper.getGpsLocationtRuntimeDao();

    }





    /**
     * Called when the activity will start interacting with the user.
     * At this point your activity is at the top of the activity stack,
     * with user input going to it.
     */


    @Override
    protected void onResume() {
        super.onResume();
        buildGoogleSignInClient();
    }



    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }


    protected Task<DriveId> pickTextFile() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        //.setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.script+json"))
                        .setActivityTitle(getString(R.string.select_file))
                        .build();
        return pickItem(openOptions);
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    protected Task<DriveId> pickFolder() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        .setSelectionFilter(
                                Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
                        .setActivityTitle(getString(R.string.select_folder))
                        .build();
        return pickItem(openOptions);
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @param openOptions Filter that should be applied to the selection
     * @return Task that resolves with the selected item's ID.
     */
    private Task<DriveId> pickItem(OpenFileActivityOptions openOptions) {
        mOpenItemTaskSource = new TaskCompletionSource<>();
        getDriveClient()
                .newOpenFileActivityIntentSender(openOptions)
                .continueWith(new Continuation<IntentSender, Void>() {
                    @Override
                    public Void then(@NonNull Task<IntentSender> task) throws Exception {
                        startIntentSenderForResult(
                                task.getResult(), REQUEST_CODE_OPEN_ITEM, null, 0, 0, 0);
                        return null;
                    }
                });
        return mOpenItemTaskSource.getTask();
    }

    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }



    /**
     *  Handle Response of selected file
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    initializeDriveClient(getAccountTask.getResult());
                } else {
                    Log.e(TAG, "Sign-in failed.");
                    finish();
                }
                break;
            case REQUEST_CODE_OPEN_ITEM:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    mOpenItemTaskSource.setResult(driveId);
                } else {
                    mOpenItemTaskSource.setException(new RuntimeException("Unable to open file"));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady();
    }


    protected DriveClient getDriveClient() {
        return mDriveClient;
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }


    protected void onDriveClientReady() {



        pickTextFile()
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveId>() {
                            @Override
                            public void onSuccess(DriveId driveId) {
                                retrieveContents(driveId.asDriveFile());
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "No file selected", e);
                        showMessage(getString(R.string.file_not_selected));
                        finish();
                    }
                });

    }

    private void retrieveContents(DriveFile file) {


        OpenFileCallback openCallback = new OpenFileCallback() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {

                // Update progress dialog with the latest progress.
                int progress = (int) (bytesDownloaded * 100 / bytesExpected);
                Log.d(TAG, String.format("Loading progress: %d percent", progress));
                progressBar.setIndeterminate(true);


            }

            public int readZipFile( InputStream is){

                int totalStayPoints=0;

                ZipInputStream zis;
                try
                {
                    String filename;
                    zis = new ZipInputStream(new BufferedInputStream(is));
                    ZipEntry ze;

                    while ((ze = zis.getNextEntry()) != null) {
                        filename = ze.getName();

                        if (filename.endsWith("History.json")) {

                            JsonReader jsonReader;
                            jsonReader = Json.createReader(zis);
                            JsonObject jsonObject=null;
                            try {
                                jsonObject = jsonReader.readObject();
                            }catch (Exception e){
                                System.err.println("Reading zip json error= "+e);
                                continue;
                            }
                            if (jsonObject != null) {
                                for (String key : jsonObject.keySet()) {
                                    JsonArray locations = (JsonArray) jsonObject.get(key);

                                    for (int j = 0; j < locations.size(); j++) {
                                        JsonObject location = (JsonObject) locations.get(j);
                                        Long timestamp = null;
                                        Double latitude = null;
                                        Double longitude = null;
                                        for (String locationKey : location.keySet()) {
                                            if (locationKey.startsWith("timestampMs")) {
                                                timestamp = Long.parseLong(location.get(locationKey).toString().substring(1, location.get(locationKey).toString().length() - 1));
                                                //if (timestamp < oneMonthTimestamp)
                                                //    break;
                                            } else if (locationKey.startsWith("latitude")) {
                                                if(Double.parseDouble(location.get(locationKey).toString())>900000000 || Double.parseDouble(location.get(locationKey).toString())<-900000000){
                                                    latitude = (Double.parseDouble(location.get(locationKey).toString())-overflow) / 10000000;
                                                }else {
                                                    latitude = Double.parseDouble(location.get(locationKey).toString()) / 10000000;
                                                }
                                            } else if (locationKey.startsWith("longitude")) {
                                                if(Double.parseDouble(location.get(locationKey).toString())>1800000000 || Double.parseDouble(location.get(locationKey).toString())<-1800000000){
                                                    longitude = (Double.parseDouble(location.get(locationKey).toString())-overflow) / 10000000;
                                                }else {
                                                    longitude = Double.parseDouble(location.get(locationKey).toString()) / 10000000;
                                                }

                                            }

                                        }
                                        if (timestamp != null && latitude != null && longitude != null) {
                                            GPSLocation gpsLocation = new GPSLocation(timestamp, latitude, longitude);

                                            gpsDao.create(gpsLocation);
                                        }
                                    }
                                }

                            }

                        }

                        zis.closeEntry();

                    }

                    zis.close();

                    totalStayPoints = getGPSPlaces();


                } catch(IOException e) {
                    Log.d(TAG, "error unzipping file");
                    e.printStackTrace();
                }

                return totalStayPoints;
            }


            @Override
            public void onContents(@NonNull DriveContents driveContents) {

                System.err.println("File has been downloaded... ");
                System.err.println("Parsing now... ");

                int nbrItems = readZipFile(driveContents.getInputStream());

                progressBar.setVisibility(View.INVISIBLE);
                Integer nbrOfPlaces = nbrItems;
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                myIntent.putExtra("key", "gmaps");
                myIntent.putExtra("items", nbrOfPlaces);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent);
            }



            @Override
            public void onError(@NonNull Exception e) {
                // Handle error
                // [START_EXCLUDE]
                Log.e(TAG, "Unable to read contents", e);
                showMessage(getString(R.string.read_failed));
                finish();
                // [END_EXCLUDE]
            }
        };

        getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY, openCallback);

    }


    private int getGPSPlaces(){
        Log.d(TAG, "Getting GPS places");
        stayPointDao = helper.getStayPointRuntimeDao();
        //int totalLocations = (int)helper.getGpsLocationtRuntimeDao().countOf();
        int totalStayPoints = 0;

        try {
            ArrayList<StayPoint> stayPoints = listOfStayPoints(helper.getGPSLocations());
            Log.d(TAG, "Saving staypoints.");
            totalStayPoints+=stayPoints.size();
            for (StayPoint sp : stayPoints) {
                stayPointDao.create(sp);
                try {
                    ArrayList<Place> places = getPlaces(sp.getCoord().getLatitude(), sp.getCoord().getLongitude());
                    if (places != null) {
                        for (Place p : places) {
                            Place pl = savePlace(p);
                            StayPointHasPlaces spHasPlaces = new StayPointHasPlaces(pl, sp);
                            helper.getStayPointHasPlacesDao().create(spHasPlaces);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (com.google.android.gms.common.api.ApiException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (com.google.maps.errors.ApiException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalStayPoints;
    }


    public Place savePlace(Place place) {
        //Place placeExists = helper.placeExistsByLatLong(place.getLatitude(), place.getLongitude());
        //if (placeExists == null) {
        helper.getPlaceDao().create(place);
        if (place.getCategories() != null) {
            for(String placeCategory: place.getCategories()){
                Category categoryExists = helper.placeCategoryExists(placeCategory);
                if (categoryExists == null) {
                    Category newCategory = new Category();
                    newCategory.setCategoryName(placeCategory);
                    helper.getCategoryDao().create(newCategory);
                    PlaceHasCategory placeHasCategories = new PlaceHasCategory(place, newCategory);
                    helper.getPlaceHasCategoryRuntimeDao().create(placeHasCategories);
                } else {
                    PlaceHasCategory place_categories = new PlaceHasCategory(place, categoryExists);
                    helper.getPlaceHasCategoryRuntimeDao().create(place_categories);
                }
            }
        }
        return place;
        //}else{
        //    return placeExists;
        // }
    }


    public ArrayList<Place> getPlaces(double lat, double lon) throws InterruptedException, com.google.android.gms.common.api.ApiException, IOException, com.google.maps.errors.ApiException {
        LatLng location = new LatLng(lat, lon);
        ArrayList<Place> places = new ArrayList<>();

        PlacesSearchResponse gmapsResponse = PlacesApi.nearbySearchQuery(geoApiContext, location)
                .radius(60)
                .type(PlaceType.RESTAURANT)
                .await();
        if (gmapsResponse.results != null) {
            if (gmapsResponse.results.length > 0) {
                for (int i = 0; i < gmapsResponse.results.length; i++) {
                    if (gmapsResponse.results[i] != null) {
                        PlacesSearchResult place = gmapsResponse.results[i];
                        Place newPlace = new Place();
                        newPlace.setLatitude(lat);
                        newPlace.setLongitude(lon);
                        if (place.vicinity != null) {
                            newPlace.setStreet(place.vicinity);
                        }
                        if (place.placeId != null) {
                            newPlace.setId(place.placeId);
                        }
                        if (place.name != null) {
                            newPlace.setName(place.name);
                        }

                        if (place.types != null) {
                            for(String placeCategory: gmapsResponse.results[0].types){
                                newPlace.addCategory(placeCategory);

                            }
                        }
                        places.add(newPlace);
                    }
                }
            }
        }

        return places;
    }


    public ArrayList<StayPoint> listOfStayPoints(ArrayList<GPSLocation> points){
        //Collections.sort(points);
        int distThres=80; //distance in meters
        int timeThres = 30*60;
        ArrayList<StayPoint> stayPointList = new ArrayList<>();
        int pointNum = points.size();
        Log.d(TAG,"Total points = "+pointNum);
        int i = 0;
        while (i < pointNum-1) {
            int j = i + 1;
            while (j < pointNum) {
                // Log.d(TAG,"Loop = "+i+" kai "+j);
                GPSLocation field_pointi = points.get(i);
                GPSLocation field_pointj = points.get(j);
                double dist = distance(field_pointi.getLatitude(), field_pointj.getLatitude(), field_pointi.getLongitude(), field_pointj.getLongitude(),0.0,0.0);
                if (dist > distThres) {
                    field_pointj = points.get(j-1);
                    Calendar calj = Calendar.getInstance(Calendar.getInstance().getTimeZone());
                    calj.setTimeInMillis(field_pointj.getTimestamp());
                    Timestamp tj = new Timestamp(calj.getTime().getTime());

                    Calendar cali = Calendar.getInstance(Calendar.getInstance().getTimeZone());
                    cali.setTimeInMillis(field_pointi.getTimestamp());
                    Timestamp ti = new Timestamp(cali.getTime().getTime());

                    int deltaT = (int)(tj.getTime() - ti.getTime())/1000;
                    if (deltaT > timeThres) {
                        StayPoint sp = new StayPoint();
                        sp.setCoord(computeMeanCoord(points.subList(i,j-1)));
                        sp.setArrive(field_pointi.getTimestamp());
                        sp.setLeave(field_pointj.getTimestamp());
                        sp.setDuration(deltaT);
                        stayPointList.add(sp);
                    }
                    i = j;
                    break;
                }
                j += 1;
            }
            if(j>=pointNum){
                i += 1;
            }
        }
        return stayPointList;
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;
        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }


    public GPSLocation computeMeanCoord(List<GPSLocation> gpsPoints){
        double lon = 0.0;
        double lat = 0.0;
        int N=gpsPoints.size();
        for (GPSLocation point : gpsPoints) {
            lon += point.getLongitude();
            lat += point.getLatitude();
        }
        return new GPSLocation(0, lat/N, lon/N );
    }


}
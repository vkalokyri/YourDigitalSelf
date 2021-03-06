package com.rutgers.neemi;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.internal.LockOnGetVariable;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.api.client.util.DateTime;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PhotoResult;
import com.google.maps.model.PlacesSearchResponse;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.model.Album;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.Feed;
import com.rutgers.neemi.model.FeedMessageTags;
import com.rutgers.neemi.model.FeedWithTags;
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

public class FacebookActivity extends AppCompatActivity {

    // Custom button
    // private Button fbbutton;
     ProgressDialog mProgress;
    // Creating Facebook CallbackManager Value
    public static CallbackManager callbackmanager;
    DatabaseHelper helper;
    int totalItemsInserted=0;
    String frequency;
    private static final String TAG = "FacebookActivity";
    GeoApiContext geoApiContext;

    RuntimeExceptionDao<Photo, String> photoDao;
    RuntimeExceptionDao<Feed, String> feedDao;
    RuntimeExceptionDao<Person, String> personDao;
    RuntimeExceptionDao<Album, String> albumDao;
    RuntimeExceptionDao<Place, String> placeDao;
    RuntimeExceptionDao<Event, String> eventDao;
    RuntimeExceptionDao<PhotoTags, String> photoTagsDao;
    RuntimeExceptionDao<FeedWithTags, String> feedWithTagsDao;
    RuntimeExceptionDao<FeedMessageTags, String> feedMessageTagsDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        helper=DatabaseHelper.getHelper(this);
        photoDao = helper.getPhotoDao();
        feedDao = helper.getFeedDao();
        personDao = helper.getPersonDao();
        albumDao = helper.getAlbumDao();
        placeDao = helper.getPlaceDao();
        eventDao = helper.getEventDao();
        photoTagsDao = helper.getPhotoTagsDao();
        feedWithTagsDao = helper.getFeedWithTagsDao();
        feedMessageTagsDao = helper.getFeedMessagesTagsDao();
        geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyCwsSXnT_jN107mMLhz55vo7JKwREaflJQ")
                .build();


        frequency = PreferenceManager.getDefaultSharedPreferences(this).getString("sync_frequency", "");

        // Initialize layout button
        //fbbutton = (Button) findViewById(R.id.login_button);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Getting your facebook data. Please wait ...");

        Intent i = getIntent();
        String permissionType = i.getStringExtra("action");

        if(permissionType.equals("grant")){
            grantPermissions();
            //getResultsFromApi();

        }else if(permissionType.equals("revoke")){
            revokePermissions();
            Intent myIntent = new Intent(this, MainActivity.class);
            myIntent.putExtra("key", "facebook");
            myIntent.putExtra("items", 0);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);
        }else if(permissionType.equals("sync")){
            getResultsFromApi();

        }



        //getResultsFromApi();



//        fbbutton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                getResultsFromApi();
//            }
//        });
    }

    public void revokePermissions(){
        Log.d(TAG ,"Revoke fb permissions");

        GraphRequest.Callback graphCallback = new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                Log.d(TAG ,"OnRevokePermissionsCompleted : " +response.getRawResponse());

            }
        };

        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "me/permissions",null, HttpMethod.DELETE, graphCallback).executeAsync();
    }

    public void grantPermissions(){

        callbackmanager = CallbackManager.Factory.create();
        final List<String> permissions = new ArrayList<String>();
        permissions.add("email");
        permissions.add("user_photos");
        permissions.add("user_events");
        permissions.add("user_posts");
        permissions.add("user_tagged_places");
        permissions.add("user_friends");

        LoginManager.getInstance().logInWithReadPermissions(this, permissions);

        LoginManager.getInstance().registerCallback(callbackmanager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        if(loginResult.getAccessToken().isExpired()){
                            Toast.makeText(getApplicationContext(), "Facebook access token has expired!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG,"OnGrantPermissionsSuccess : TokenHasExpired");
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            preferences.edit().putBoolean("facebook", false).apply();
                            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                            myIntent.putExtra("key", "facebook");
                            myIntent.putExtra("token", "expired");
                            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(myIntent);
                        }else {
                            if(loginResult.getAccessToken().getToken().contains("ACCESS_TOKEN_REMOVED")){
                                Toast.makeText(getApplicationContext(), "Facebook access token was removed.", Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"OnGrantPermissionsSuccess : ACCESS_TOKEN_REMOVED");
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                preferences.edit().putBoolean("facebook", false).apply();
                                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                                myIntent.putExtra("key", "facebook");
                                myIntent.putExtra("token", "removed");
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);

                            }else {

                                Log.d(TAG,"OnGrantPermissionsSuccess : " + loginResult.getAccessToken().getToken());
                                //DataSyncJob.scheduleAdvancedJob();


//                                AlertDialog.Builder builder;
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                    builder = new AlertDialog.Builder(FacebookActivity.this, android.R.style.Theme_Material_Dialog_Alert);
//                                } else {
//                                    builder = new AlertDialog.Builder(FacebookActivity.this);
//                                }
//
//
//
//                                builder.setTitle("Facebook was successfully authorized!")
//                                        .setMessage("Do you want the app to get your past month's data or start collecting data from today?")
//                                        .setPositiveButton("One month data", new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int which) {
//
//                                                getResultsFromApi();
//
//                                            }
//                                        })
//                                        .setNegativeButton("Start from today", new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
//                                                myIntent.putExtra("key", "facebook");
//                                                myIntent.putExtra("items", 0);
//                                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                startActivity(myIntent);
//
//                                            }
//                                        })
//                                        .setIcon(android.R.drawable.ic_dialog_info)
//                                        .show();


                                Toast.makeText(getApplicationContext(), "Facebook was successfully authorized!", Toast.LENGTH_SHORT).show();
                                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                                myIntent.putExtra("key", "facebook");
                                myIntent.putExtra("items", -1);
                                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(myIntent);

                            }
                        }
                    }

                    @Override
                    public void onCancel() {

                        Log.d("CANCEL", "On cancel");
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        preferences.edit().putBoolean("facebook", false).apply();

                        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                        myIntent.putExtra("key", "facebook");
                        myIntent.putExtra("items", 0);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("ERROR", error.toString());
                        Toast.makeText(getApplicationContext(), "An unexpected error has occured!", Toast.LENGTH_SHORT).show();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        preferences.edit().putBoolean("facebook", false).apply();
                        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                        myIntent.putExtra("key", "facebook");
                        myIntent.putExtra("items", 0);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                    }
                });
    }

    public Calendar getCalendarDate(String period){
        Calendar cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());

        if (period.equals("7")){
            cal.add(Calendar.DATE, -7);
        }else if(period.equals("30")){
            cal.add(Calendar.MONTH, -1);
        }else if(period.equals("180")){
            cal.add(Calendar.MONTH, -6);
        }else if(period.equals("365")){
            cal.add(Calendar.MONTH, -12);
        }else if(period.equals("1")){
            cal.add(Calendar.DATE, -1);
        }

        return  cal;

    }

    // Private method to handle Facebook login and callback
    public void getResultsFromApi() {


        callbackmanager = CallbackManager.Factory.create();
        final List<String> permissions = new ArrayList<String>();
        permissions.add("email");
        permissions.add("user_photos");
        permissions.add("user_events");
        permissions.add("user_posts");
        permissions.add("user_tagged_places");
        permissions.add("user_friends");
        //loginButton.setReadPermissions(permissions);

        // Set permissions
        LoginManager.getInstance().logInWithReadPermissions(this, permissions);

        LoginManager.getInstance().registerCallback(callbackmanager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        mProgress.show();
                        Log.d(TAG,"Login successfull");


                        Calendar cal = getCalendarDate(frequency);
                        DateTime since=new DateTime(cal.getTimeInMillis());
                        String timestamp = null;

                        GenericRawResults<String[]> photoRawResults = photoDao.queryRaw("select max(timestamp) from Photo where source=\"facebook\";");
                        List<String[]> results = null;
                        try {
                            results = photoRawResults.getResults();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (results!=null){
                            String[] resultArray = results.get(0);
                            timestamp=resultArray[0];
                        }
                        if (timestamp!=null) {
                            cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());
                            cal.setTimeInMillis(Long.parseLong(timestamp)*1000);
                            since = new DateTime(cal.getTimeInMillis());
                        }

                        GraphRequest.Callback photosGraphCallback = getFacebookPhotos();
                        Bundle parameters = new Bundle();
                        //parameters.putString("fields", "name,id,link,album{name, description, event, place, created_time, from{name, email, id}},created_time,event,place,tags{name, email,id},from{name, email,id},picture");
                        parameters.putString("since", since.toString());
                        new GraphRequest(AccessToken.getCurrentAccessToken(),"me/photos",parameters, HttpMethod.GET, photosGraphCallback).executeAsync();




                        /* POSTTTTT*/
                        cal = getCalendarDate(frequency);
                        since=new DateTime(cal.getTimeInMillis());
                        timestamp = null;

                        GenericRawResults<String[]> feedRawResults = feedDao.queryRaw("select max(timestamp) from Feed;");
                        results = null;
                        try {
                            results = feedRawResults.getResults();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (results!=null){
                            String[] resultArray = results.get(0);
                            //System.out.println("timestamp= " + resultArray[0]);
                            timestamp=resultArray[0];
                        }
                        if (timestamp!=null) {
                            cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());
                            cal.setTimeInMillis(Long.parseLong(timestamp)*1000);
                            since = new DateTime(cal.getTimeInMillis());
                        }

                        GraphRequest.Callback feedGraphCallback = getFacebookFeed();
                        Bundle params = new Bundle();
                        //params.putString("fields", "message,link,place,message_tags,with_tags,description,created_time,from,object_id,picture,story,type");
                        params.putString("since", since.toString());
                        new GraphRequest(AccessToken.getCurrentAccessToken(),"/me/feed",params,HttpMethod.GET, feedGraphCallback).executeAsync();



                        cal = getCalendarDate(frequency);
                        since=new DateTime(cal.getTimeInMillis());
                        timestamp = null;
                        parameters = new Bundle();


                        GenericRawResults<String[]> eventRawResults = eventDao.queryRaw("select max(start_time) from Event where source=\"facebook\";");
                        results = null;
                        try {
                            results = eventRawResults.getResults();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (results!=null){
                            String[] resultArray = results.get(0);
                            timestamp=resultArray[0];
                        }
                        if (timestamp!=null) {
                            cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());
                            cal.setTimeInMillis(Long.parseLong(timestamp)*1000);
                            since = new DateTime(cal.getTimeInMillis());
                            parameters.putString("start_time", since.toString());

                        }

                        GraphRequest.Callback eventsGraphCallback = getFacebookEvents();
                       // parameters.putString("fields", "description,category,name,owner,place,start_time,updated_time,scheduled_publish_time,rsvp_status,event_times,end_time");
                        new GraphRequest(AccessToken.getCurrentAccessToken(),"me/events",parameters, HttpMethod.GET, eventsGraphCallback).executeAsync();



                        mProgress.dismiss();


                        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                        myIntent.putExtra("key", "facebook");
                        myIntent.putExtra("items", totalItemsInserted);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);



                    }

                    @Override
                    public void onCancel() {
                        Log.d("CANCEL", "On cancel");
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        preferences.edit().putBoolean("facebook", false).apply();
                        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                        myIntent.putExtra("key", "facebook");
                        myIntent.putExtra("items", 0);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("ERROR", error.toString());
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        preferences.edit().putBoolean("facebook", false).apply();
                        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                        myIntent.putExtra("key", "facebook");
                        myIntent.putExtra("items", 0);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                    }


                });
    }

    /* GET FACEBOOK PHOTOS */
    public GraphRequest.Callback getFacebookPhotos() {

        return new GraphRequest.Callback(){
            @Override
            public void onCompleted(GraphResponse response) {

                if (response.getError() != null) {
                    // handle error
                    Log.e(TAG,"ERROR when getting photos response");
                } else {
                    System.out.println("Response for photos = " + response.getRawResponse());
                    JSONArray rawPhotosData = null;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                    //int totalItemsInserted = 0;
                    try {
                        rawPhotosData = response.getJSONObject().getJSONArray("data");
                        for (int j = 0; j < rawPhotosData.length(); j++) {
                            //save whatever data you want from the result
                            Photo photo = new Photo();
                            photo.setId((String) ((JSONObject) rawPhotosData.get(j)).get("id"));
                            try {
                                photo.setCreated_time(dateFormat.parse((String) ((JSONObject) rawPhotosData.get(j)).get("created_time")).getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if (((JSONObject) rawPhotosData.get(j)).has("picture")) {
                                photo.setPicture((String) ((JSONObject) rawPhotosData.get(j)).get("picture"));
                                System.out.println("Pic = " + photo.getPicture());
                            }
                            if (((JSONObject) rawPhotosData.get(j)).has("link")) {
                                photo.setLink((String) ((JSONObject) rawPhotosData.get(j)).get("link"));
                                System.out.println("Link = " + photo.getLink());
                            }
                            if (((JSONObject) rawPhotosData.get(j)).has("name")) {
                                photo.setName((String) ((JSONObject) rawPhotosData.get(j)).get("name"));
                                System.out.println("Name = " + photo.getName());
                            }
                            if (((JSONObject) rawPhotosData.get(j)).has("album")) {
                                JSONObject albumJson = (JSONObject) ((JSONObject) rawPhotosData.get(j)).get("album");
                                if (albumJson.has("id")) {
                                    Album album = helper.albumExistsById((String) albumJson.get("id"));
                                    if (album == null) {
                                        Album newAlbum = new Album();
                                        newAlbum.setId((String) albumJson.get("id"));
                                        if (albumJson.has("name")) {
                                            newAlbum.setName((String) albumJson.get("name"));
                                        }
                                        try {
                                            newAlbum.setCreated_time(dateFormat.parse((String) albumJson.get("created_time")).getTime());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        if (albumJson.has("description")) {
                                            newAlbum.setDescription((String) albumJson.get("description"));
                                        }
                                        if (albumJson.has("event")) {
                                            JSONObject jsonEvent = (JSONObject) albumJson.get("event");
                                            if (jsonEvent.has("id")) {
                                                Event event = helper.eventExistsById((String) jsonEvent.get("id"));
                                                if(event==null) {
                                                    Event newEvent = new Event();
                                                    newEvent.setId((String) jsonEvent.get("id"));
                                                    if (jsonEvent.has("description")) {
                                                        newEvent.setDescription((String) jsonEvent.get("description"));
                                                    }

                                                    if (jsonEvent.has("name")) {
                                                        newEvent.setTitle((String) jsonEvent.get("name"));
                                                    }
                                                    if (jsonEvent.has("end_time")) {
                                                        try {
                                                            newEvent.setEndTime(dateFormat.parse((String) jsonEvent.get("end_time")).getTime());
                                                        } catch (ParseException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    if (jsonEvent.has("start_time")) {
                                                        try {
                                                            newEvent.setStartTime(dateFormat.parse((String) jsonEvent.get("start_time")).getTime());
                                                        } catch (ParseException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    if (jsonEvent.has("owner")) {
                                                        Person owner = new Person();
                                                        JSONObject jsonOwner = (JSONObject) jsonEvent.get("owner");
                                                        if (jsonOwner.has("name")) {
                                                            owner.setName((String) jsonOwner.get("name"));
                                                        }
                                                        if (jsonOwner.has("id")) {
                                                            owner.setId((String) jsonOwner.get("id"));
                                                        }
                                                        newEvent.setOrganizer(owner);
                                                    }
                                                    eventDao.create(newEvent);
                                                    newAlbum.setEvent(newEvent);
                                                }else{
                                                    newAlbum.setEvent(event);
                                                }
                                            }
                                        }
                                        if (albumJson.has("place")) {
                                            JSONObject placeJson = (JSONObject) albumJson.get("place");
                                            if (placeJson.has("id")) {
                                                Place place = helper.placeExistsById((String) placeJson.get("id"));
                                                if (place == null) {
                                                    Place newPlace = new Place();
                                                    newPlace.setId((String) placeJson.get("id"));
                                                    if (placeJson.has("name")) {
                                                        newPlace.setName((String) placeJson.get("name"));
                                                    }

                                                    if (placeJson.has("location")) {
                                                        JSONObject locationJson = (JSONObject) placeJson.get("location");
                                                        if (locationJson.has("city")) {
                                                            newPlace.setCity((String) locationJson.get("city"));
                                                        }
                                                        if (locationJson.has("country")) {
                                                            newPlace.setCountry((String) locationJson.get("country"));
                                                        }
                                                        if (locationJson.has("latitude")) {
                                                            newPlace.setLatitude((double) locationJson.get("latitude"));
                                                        }
                                                        if (locationJson.has("longitude")) {
                                                            newPlace.setLongitude((double) locationJson.get("longitude"));
                                                        }
                                                        if (locationJson.has("state")) {
                                                            newPlace.setState((String) locationJson.get("state"));
                                                        }
                                                        if (locationJson.has("region")) {
                                                            newPlace.setRegion((String) locationJson.get("region"));
                                                        }
                                                        if (locationJson.has("street")) {
                                                            newPlace.setStreet((String) locationJson.get("street"));
                                                        }
                                                        if (locationJson.has("zip")) {
                                                            newPlace.setZip((String) locationJson.get("zip"));
                                                        }
                                                    }
                                                    placeDao.create(newPlace);
                                                    newAlbum.setPlace(newPlace);
                                                } else {
                                                    newAlbum.setPlace(place);
                                                }
                                            }
                                        }

                                        if (albumJson.has("from")) {
                                            JSONObject from = albumJson.getJSONObject("from");
                                            if(from.has("id")) {
                                                Person personExists = helper.personExistsById((String)from.get("id"));
                                                if (personExists ==null) {
                                                    Person newPerson = new Person();
                                                    newPerson.setId((String)from.get("id"));
                                                    if ( from.has("name")) {
                                                        newPerson.setName((String)from.get("name"));
                                                    }
                                                    if(from.has("email")) {
                                                        newPerson.setEmail((String)from.get("email"));
                                                    }
                                                    personDao.create(newPerson);
                                                    newAlbum.setCreator(newPerson);
                                                }else{
                                                    newAlbum.setCreator(personExists);
                                                }
                                            }
                                        }
                                        albumDao.create(newAlbum);
                                        photo.setAlbum(newAlbum);
                                    }else {
                                        photo.setAlbum(album);
                                    }
                                }

                            }
                            if (((JSONObject) rawPhotosData.get(j)).has("place")) {
                                JSONObject placeJson = (JSONObject) ((JSONObject) rawPhotosData.get(j)).get("place");
                                if (placeJson.has("id")) {
                                    Place place = helper.placeExistsById((String) placeJson.get("id"));
                                    if (place == null) {
                                        Place newPlace = new Place();
                                        newPlace.setId((String) placeJson.get("id"));
                                        if (placeJson.has("name")) {
                                            newPlace.setName((String) placeJson.get("name"));
                                        }
                                        if (placeJson.has("id")) {
                                            newPlace.setId((String) placeJson.get("id"));
                                        }
                                        if (placeJson.has("location")) {
                                            JSONObject locationJson = (JSONObject) placeJson.get("location");
                                            if (locationJson.has("city")) {
                                                newPlace.setCity((String) locationJson.get("city"));
                                            }
                                            if (locationJson.has("country")) {
                                                newPlace.setCountry((String) locationJson.get("country"));
                                            }
                                            if (locationJson.has("latitude")) {
                                                newPlace.setLatitude((double) locationJson.get("latitude"));
                                            }
                                            if (locationJson.has("longitude")) {
                                                newPlace.setLongitude((double) locationJson.get("longitude"));
                                            }
                                            if (locationJson.has("state")) {
                                                newPlace.setState((String) locationJson.get("state"));
                                            }
                                            if (locationJson.has("street")) {
                                                newPlace.setStreet((String) locationJson.get("street"));
                                            }
                                            if (locationJson.has("region")) {
                                                newPlace.setRegion((String) locationJson.get("region"));
                                            }
                                            if (locationJson.has("zip")) {
                                                newPlace.setZip((String) locationJson.get("zip"));
                                            }



                                            LatLng location = new LatLng(newPlace.getLatitude(),newPlace.getLongitude());
                                            try {
                                                PlacesSearchResponse gmapsResponse = PlacesApi.nearbySearchQuery(geoApiContext, location)
                                                        .radius(100)
                                                        .keyword(newPlace.getName())
                                                        .name(newPlace.getName())
                                                        .await();
                                                if (gmapsResponse.results!=null){
                                                    if (gmapsResponse.results.length>0) {
                                                        if (gmapsResponse.results[0].photos!=null) {
                                                            com.google.maps.model.Photo photoFound=null;
                                                            for (com.google.maps.model.Photo p: gmapsResponse.results[0].photos){
                                                                if (p.width>750) {
                                                                    photoFound=p;
                                                                    break;
                                                                }

                                                            }
                                                            if (photoFound!=null){
                                                                PhotoResult photoResult = PlacesApi.photo(geoApiContext,photoFound.photoReference).maxWidth(1600).await();
                                                                byte[] image = photoResult.imageData;
                                                                newPlace.setImage(image);
                                                            }else{
                                                                PhotoResult photoResult = PlacesApi.photo(geoApiContext,gmapsResponse.results[0].photos[0].photoReference).maxWidth(1600).await();
                                                                byte[] image = photoResult.imageData;
                                                                newPlace.setImage(image);
                                                            }


                                                        }
                                                        placeDao.create(newPlace);
                                                        photo.setPlace(newPlace);
                                                        for (String placeCategory : gmapsResponse.results[0].types) {

                                                            Category categoryExists = helper.placeCategoryExists(placeCategory);
                                                            if (categoryExists == null) {
                                                                Category newCategory = new Category();
                                                                newCategory.setCategoryName(placeCategory);
                                                                helper.getCategoryDao().create(newCategory);
                                                                PlaceHasCategory placeHasCategories = new PlaceHasCategory(newPlace, newCategory);
                                                                helper.getPlaceHasCategoryRuntimeDao().create(placeHasCategories);
                                                            } else {
                                                                PlaceHasCategory trans_categories = new PlaceHasCategory(newPlace, categoryExists);
                                                                helper.getPlaceHasCategoryRuntimeDao().create(trans_categories);
                                                            }
                                                        }
                                                    }
                                                }else{
                                                    placeDao.create(newPlace);
                                                    photo.setPlace(newPlace);
                                                }
                                            } catch (ApiException e) {
                                                placeDao.create(newPlace);
                                                photo.setPlace(newPlace);
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                placeDao.create(newPlace);
                                                photo.setPlace(newPlace);
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                placeDao.create(newPlace);
                                                photo.setPlace(newPlace);
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        photo.setPlace(place);
                                    }
                                }
                            }

                            if (((JSONObject) rawPhotosData.get(j)).has("from")) {
                                //Person creator = new Person();
                                JSONObject from = ((JSONObject) rawPhotosData.get(j)).getJSONObject("from");
                                if(from.has("id")) {
                                    Person personExists = helper.personExistsById((String)from.get("id"));
                                    if (personExists ==null) {
                                        Person newPerson = new Person();
                                        newPerson.setId((String)from.get("id"));
                                        if ( from.has("name")) {
                                            newPerson.setName((String)from.get("name"));
                                        }
                                        if(from.has("email")) {
                                            newPerson.setEmail((String)from.get("email"));
                                        }
                                        personDao.create(newPerson);
                                        photo.setCreator(newPerson);
                                    }else{
                                        photo.setCreator(personExists);
                                    }
                                }
                            }
                            photo.setTimestamp(System.currentTimeMillis() / 1000);
                            photo.setSource("facebook");
                            photoDao.create(photo);

                            List<Person> taggedList = new ArrayList<Person>();
                            if (((JSONObject) rawPhotosData.get(j)).has("tags")) {
                                JSONObject tagsJson = (JSONObject) ((JSONObject) rawPhotosData.get(j)).get("tags");
                                if (tagsJson.has("data")) {
                                    JSONArray tagsArray = tagsJson.getJSONArray("data");
                                    for (int k = 0; k < tagsArray.length(); k++) {
                                        //save whatever data you want from the result
                                        System.out.println((JSONObject) tagsArray.get(k));
                                        Person person = helper.personExistsById((String) ((JSONObject) tagsArray.get(k)).get("id"));
                                        if (person ==null) {
                                            Person newPerson = new Person();
                                            newPerson.setId((String) ((JSONObject) tagsArray.get(k)).get("id"));
                                            if ( ((JSONObject) tagsArray.get(k)).has("name")) {
                                                newPerson.setName((String) ((JSONObject) tagsArray.get(k)).get("name"));
                                            }
                                            if(((JSONObject) tagsArray.get(k)).has("email")) {
                                                newPerson.setEmail((String) ((JSONObject) tagsArray.get(k)).get("email"));
                                            }
                                            personDao.create(newPerson);
                                            taggedList.add(newPerson);

                                        }else{
                                            PhotoTags taggedPeople = new PhotoTags(person, photo);
                                            photoTagsDao.create(taggedPeople);
                                        }
                                    }
                                }
                            }
                            for(Person tagged:taggedList) {
                                PhotoTags taggedPeople = new PhotoTags(tagged, photo);
                                photoTagsDao.create(taggedPeople);
                            }
                            totalItemsInserted++;
                            System.out.println("FbPhotosInserted = " + totalItemsInserted);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //get next batch of results of exists
                    GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                    if (nextRequest != null) {
                        nextRequest.setCallback(this);
                        nextRequest.executeAsync();
                    }
                }


            }
        };
    }

    /* GET FACEBOOK FEED */
    public GraphRequest.Callback getFacebookFeed() {
        return new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {

                if (response.getError() != null) {
                    // handle error
                    Log.e(TAG, "ERROR in facebook FEED response");
                    System.out.println(response.getRawResponse());
                } else {
                    Log.d(TAG, "SUCCESS in facebook FEED response");
                    JSONArray rawFeedData = null;
                    System.out.println("FEED response = " + response.getRawResponse());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                    // int totalItemsInserted = 0;
                    try {
                        rawFeedData = response.getJSONObject().getJSONArray("data");
                        for (int j = 0; j < rawFeedData.length(); j++) {
                            //save whatever data you want from the result
                            Feed feed = new Feed();
                            System.out.println(totalItemsInserted + ": " + rawFeedData.get(j));
                            feed.setId((String) ((JSONObject) rawFeedData.get(j)).get("id"));

                            if (((JSONObject) rawFeedData.get(j)).has("message")) {
                                feed.setMessage((String) ((JSONObject) rawFeedData.get(j)).get("message"));
                                System.out.println(totalItemsInserted + ": " + (String) ((JSONObject) rawFeedData.get(j)).get("message"));

                            }

                            if (((JSONObject) rawFeedData.get(j)).has("link")) {
                                feed.setLink((String) ((JSONObject) rawFeedData.get(j)).get("link"));
                            }

                            try {
                                feed.setCreated_time(dateFormat.parse((String) ((JSONObject) rawFeedData.get(j)).get("created_time")).getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if (((JSONObject) rawFeedData.get(j)).has("picture")) {
                                feed.setPicture((String) ((JSONObject) rawFeedData.get(j)).get("picture"));
                            }

                            if (((JSONObject) rawFeedData.get(j)).has("place")) {
                                JSONObject placeJson = (JSONObject) ((JSONObject) rawFeedData.get(j)).get("place");
                                if (placeJson.has("id")) {
                                    Place place = helper.placeExistsById((String) placeJson.get("id"));
                                    if (place == null) {
                                        Place newPlace = new Place();
                                        newPlace.setId((String) placeJson.get("id"));
                                        if (placeJson.has("name")) {
                                            newPlace.setName((String) placeJson.get("name"));
                                        }
                                        if (placeJson.has("id")) {
                                            newPlace.setId((String) placeJson.get("id"));
                                        }
                                        if (placeJson.has("location")) {
                                            JSONObject locationJson = (JSONObject) placeJson.get("location");
                                            if (locationJson.has("city")) {
                                                newPlace.setCity((String) locationJson.get("city"));
                                            }
                                            if (locationJson.has("country")) {
                                                newPlace.setCountry((String) locationJson.get("country"));
                                            }
                                            if (locationJson.has("latitude")) {
                                                newPlace.setLatitude((double) locationJson.get("latitude"));
                                            }
                                            if (locationJson.has("longitude")) {
                                                newPlace.setLongitude((double) locationJson.get("longitude"));
                                            }
                                            if (locationJson.has("state")) {
                                                newPlace.setState((String) locationJson.get("state"));
                                            }
                                            if (locationJson.has("street")) {
                                                newPlace.setStreet((String) locationJson.get("street"));
                                            }
                                            if (locationJson.has("region")) {
                                                newPlace.setRegion((String) locationJson.get("region"));
                                            }
                                            if (locationJson.has("zip")) {
                                                newPlace.setZip((String) locationJson.get("zip"));
                                            }


                                            LatLng location = new LatLng(newPlace.getLatitude(), newPlace.getLongitude());
                                            try {
                                                PlacesSearchResponse gmapsResponse = PlacesApi.nearbySearchQuery(geoApiContext, location)
                                                        .radius(100)
                                                        .keyword(newPlace.getName())
                                                        .name(newPlace.getName())
                                                        .await();
                                                if (gmapsResponse.results != null) {
                                                    if (gmapsResponse.results.length > 0) {
                                                        for (String placeCategory : gmapsResponse.results[0].types) {
                                                            if (gmapsResponse.results[0].photos != null) {
                                                                com.google.maps.model.Photo photoFound = null;
                                                                for (com.google.maps.model.Photo p : gmapsResponse.results[0].photos) {
                                                                    if (p.width > 750) {
                                                                        photoFound = p;
                                                                        break;
                                                                    }

                                                                }
                                                                if (photoFound != null) {
                                                                    PhotoResult photoResult = PlacesApi.photo(geoApiContext, photoFound.photoReference).maxWidth(1600).await();
                                                                    byte[] image = photoResult.imageData;
                                                                    newPlace.setImage(image);
                                                                } else {
                                                                    PhotoResult photoResult = PlacesApi.photo(geoApiContext, gmapsResponse.results[0].photos[0].photoReference).maxWidth(1600).await();
                                                                    byte[] image = photoResult.imageData;
                                                                    newPlace.setImage(image);
                                                                }


                                                            }

                                                            placeDao.create(newPlace);
                                                            feed.setPlace(newPlace);
                                                            Category categoryExists = helper.placeCategoryExists(placeCategory);
                                                            if (categoryExists == null) {
                                                                Category newCategory = new Category();
                                                                newCategory.setCategoryName(placeCategory);
                                                                helper.getCategoryDao().create(newCategory);
                                                                PlaceHasCategory placeHasCategories = new PlaceHasCategory(newPlace, newCategory);
                                                                helper.getPlaceHasCategoryRuntimeDao().create(placeHasCategories);
                                                            } else {
                                                                PlaceHasCategory trans_categories = new PlaceHasCategory(newPlace, categoryExists);
                                                                helper.getPlaceHasCategoryRuntimeDao().create(trans_categories);
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    placeDao.create(newPlace);
                                                    feed.setPlace(newPlace);
                                                }
                                            } catch (ApiException e) {
                                                placeDao.create(newPlace);
                                                feed.setPlace(newPlace);
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                placeDao.create(newPlace);
                                                feed.setPlace(newPlace);
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                placeDao.create(newPlace);
                                                feed.setPlace(newPlace);
                                                e.printStackTrace();
                                            }

                                        }
                                    } else {
                                        feed.setPlace(place);
                                    }
                                }
                            }

                            if (((JSONObject) rawFeedData.get(j)).has("from")) {
                                //Person creator = new Person();
                                JSONObject from = ((JSONObject) rawFeedData.get(j)).getJSONObject("from");
                                if (from.has("id")) {
                                    Person personExists = helper.personExistsById((String) from.get("id"));
                                    if (personExists == null) {
                                        Person newPerson = new Person();
                                        newPerson.setId((String) from.get("id"));
                                        if (from.has("name")) {
                                            newPerson.setName((String) from.get("name"));
                                        }
                                        if (from.has("email")) {
                                            newPerson.setEmail((String) from.get("email"));
                                        }
                                        personDao.create(newPerson);
                                        feed.setCreator(newPerson);
                                    } else {
                                        feed.setCreator(personExists);
                                    }
                                }
                            }

                            if (((JSONObject) rawFeedData.get(j)).has("description")) {
                                feed.setDescription((String) ((JSONObject) rawFeedData.get(j)).get("description"));
                            }

                            if (((JSONObject) rawFeedData.get(j)).has("object_id")) {
                                feed.setObject_id((String) ((JSONObject) rawFeedData.get(j)).get("object_id"));
                            }

                            if (((JSONObject) rawFeedData.get(j)).has("story")) {
                                feed.setStory((String) ((JSONObject) rawFeedData.get(j)).get("story"));
                            }

                            if (((JSONObject) rawFeedData.get(j)).has("type")) {
                                feed.setType((String) ((JSONObject) rawFeedData.get(j)).get("type"));
                            }


                            feed.setTimestamp(System.currentTimeMillis() / 1000);
                            feed.setSource("facebook");
                            feedDao.create(feed);

                            List<Person> taggedList = new ArrayList<Person>();
                            if (((JSONObject) rawFeedData.get(j)).has("with_tags")) {
                                JSONArray tagsArray = new JSONArray();
                                if (((JSONObject) rawFeedData.get(j)).get("with_tags") instanceof JSONArray) {
                                    ;
                                } else {
                                    JSONObject tagsJson = (JSONObject) ((JSONObject) rawFeedData.get(j)).get("with_tags");
                                    if (tagsJson.has("data")) {
                                        tagsArray = tagsJson.getJSONArray("data");
                                        for (int k = 0; k < tagsArray.length(); k++) {
                                            //save whatever data you want from the result
                                            System.out.println((JSONObject) tagsArray.get(k));
                                            Person person = helper.personExistsById((String) ((JSONObject) tagsArray.get(k)).get("id"));
                                            if (person == null) {
                                                Person newPerson = new Person();
                                                newPerson.setId((String) ((JSONObject) tagsArray.get(k)).get("id"));
                                                if (((JSONObject) tagsArray.get(k)).has("name")) {
                                                    newPerson.setName((String) ((JSONObject) tagsArray.get(k)).get("name"));
                                                }
                                                if (((JSONObject) tagsArray.get(k)).has("email")) {
                                                    newPerson.setEmail((String) ((JSONObject) tagsArray.get(k)).get("email"));
                                                }
                                                personDao.create(newPerson);
                                                taggedList.add(newPerson);

                                            } else {
                                                FeedWithTags taggedPeople = new FeedWithTags(person, feed);
                                                feedWithTagsDao.create(taggedPeople);
                                            }
                                        }
                                    }
                                }
                            }
                            for (Person tagged : taggedList) {
                                FeedWithTags taggedPeople = new FeedWithTags(tagged, feed);
                                feedWithTagsDao.create(taggedPeople);
                            }

                            taggedList = new ArrayList<Person>();
                            if (((JSONObject) rawFeedData.get(j)).has("message_tags")) {
                                JSONArray tagsArray = ((JSONObject) rawFeedData.get(j)).getJSONArray("message_tags");
                                for (int k = 0; k < tagsArray.length(); k++) {
                                    //save whatever data you want from the result
                                    System.out.println((JSONObject) tagsArray.get(k));
                                    Person person = helper.personExistsById((String) ((JSONObject) tagsArray.get(k)).get("id"));
                                    if (person == null) {
                                        Person newPerson = new Person();
                                        newPerson.setId((String) ((JSONObject) tagsArray.get(k)).get("id"));
                                        if (((JSONObject) tagsArray.get(k)).has("name")) {
                                            newPerson.setName((String) ((JSONObject) tagsArray.get(k)).get("name"));
                                        }
                                        if (((JSONObject) tagsArray.get(k)).has("email")) {
                                            newPerson.setEmail((String) ((JSONObject) tagsArray.get(k)).get("email"));
                                        }
                                        personDao.create(newPerson);
                                        taggedList.add(newPerson);

                                    } else {
                                        FeedMessageTags taggedPeople = new FeedMessageTags(person, feed);
                                        feedMessageTagsDao.create(taggedPeople);
                                    }
                                }

                            }
                            for (Person tagged : taggedList) {
                                FeedMessageTags taggedPeople = new FeedMessageTags(tagged, feed);
                                feedMessageTagsDao.create(taggedPeople);
                            }


                            totalItemsInserted++;
                            Log.d(TAG, "FbPostsInserted = " + totalItemsInserted);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG,"EXCEPTION ON json");
                        e.printStackTrace();
                    }

                    //get next batch of results of exists
                    GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                    if (nextRequest != null) {
                        nextRequest.setCallback(this);
                        nextRequest.executeAsync();
                    }

                }
            }
        };
    }



    public GraphRequest.Callback getFacebookEvents() {
        return new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {

                if (response.getError() != null) {
                    // handle error
                    Log.e(TAG, "ERROR in facebook EVENT response");
                    System.out.println(response.getRawResponse());
                } else {
                    Log.d(TAG, "SUCCESS in facebook EVENT response");
                    JSONArray rawEventData = null;
                    System.out.println("Event response = " + response.getRawResponse());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                    // int totalItemsInserted = 0;
                    try {
                        rawEventData = response.getJSONObject().getJSONArray("data");
                        for (int j = 0; j < rawEventData.length(); j++) {
                            //save whatever data you want from the result
                            Event event = new Event();
                            System.out.println(totalItemsInserted + ": " + rawEventData.get(j));
                            event.setId((String) ((JSONObject) rawEventData.get(j)).get("id"));

                            if (((JSONObject) rawEventData.get(j)).has("name")) {
                                event.setTitle((String) ((JSONObject) rawEventData.get(j)).get("name"));
                                System.out.println(totalItemsInserted + ": " + (String) ((JSONObject) rawEventData.get(j)).get("name"));

                            }

                            if (((JSONObject) rawEventData.get(j)).has("scheduled_publish_time")) {
                                try {
                                    event.setDateCreated(dateFormat.parse((String) ((JSONObject) rawEventData.get(j)).get("scheduled_publish_time")).getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    continue;
                                }

                            }
                            if (((JSONObject) rawEventData.get(j)).has("start_time")) {
                                try {
                                    event.setStartTime(dateFormat.parse((String) ((JSONObject) rawEventData.get(j)).get("start_time")).getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    continue;
                                }

                            }

                            if (((JSONObject) rawEventData.get(j)).has("end_time")) {
                                try {
                                    event.setEndTime(dateFormat.parse((String) ((JSONObject) rawEventData.get(j)).get("end_time")).getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    continue;
                                }

                            }


                            if (((JSONObject) rawEventData.get(j)).has("place")) {
                                JSONObject placeJson = (JSONObject) ((JSONObject) rawEventData.get(j)).get("place");
                                if (placeJson.has("id")) {
                                    Place place = helper.placeExistsById((String) placeJson.get("id"));
                                    if (place == null) {
                                        Place newPlace = new Place();
                                        newPlace.setId((String) placeJson.get("id"));
                                        if (placeJson.has("name")) {
                                            newPlace.setName((String) placeJson.get("name"));
                                        }
                                        if (placeJson.has("id")) {
                                            newPlace.setId((String) placeJson.get("id"));
                                        }
                                        if (placeJson.has("location")) {
                                            JSONObject locationJson = (JSONObject) placeJson.get("location");
                                            if (locationJson.has("city")) {
                                                newPlace.setCity((String) locationJson.get("city"));
                                            }
                                            if (locationJson.has("country")) {
                                                newPlace.setCountry((String) locationJson.get("country"));
                                            }
                                            if (locationJson.has("latitude")) {
                                                newPlace.setLatitude((double) locationJson.get("latitude"));
                                            }
                                            if (locationJson.has("longitude")) {
                                                newPlace.setLongitude((double) locationJson.get("longitude"));
                                            }
                                            if (locationJson.has("state")) {
                                                newPlace.setState((String) locationJson.get("state"));
                                            }
                                            if (locationJson.has("street")) {
                                                newPlace.setStreet((String) locationJson.get("street"));
                                            }
                                            if (locationJson.has("region")) {
                                                newPlace.setRegion((String) locationJson.get("region"));
                                            }
                                            if (locationJson.has("zip")) {
                                                newPlace.setZip((String) locationJson.get("zip"));
                                            }


                                            LatLng location = new LatLng(newPlace.getLatitude(), newPlace.getLongitude());
                                            try {
                                                PlacesSearchResponse gmapsResponse = PlacesApi.nearbySearchQuery(geoApiContext, location)
                                                        .radius(100)
                                                        .keyword(newPlace.getName())
                                                        .name(newPlace.getName())
                                                        .await();
                                                if (gmapsResponse.results != null) {
                                                    if (gmapsResponse.results.length > 0) {
                                                        for (String placeCategory : gmapsResponse.results[0].types) {
                                                            if (gmapsResponse.results[0].photos != null) {
                                                                com.google.maps.model.Photo photoFound = null;
                                                                for (com.google.maps.model.Photo p : gmapsResponse.results[0].photos) {
                                                                    if (p.width > 750) {
                                                                        photoFound = p;
                                                                        break;
                                                                    }

                                                                }
                                                                if (photoFound != null) {
                                                                    PhotoResult photoResult = PlacesApi.photo(geoApiContext, photoFound.photoReference).maxWidth(1600).await();
                                                                    byte[] image = photoResult.imageData;
                                                                    newPlace.setImage(image);
                                                                } else {
                                                                    PhotoResult photoResult = PlacesApi.photo(geoApiContext, gmapsResponse.results[0].photos[0].photoReference).maxWidth(1600).await();
                                                                    byte[] image = photoResult.imageData;
                                                                    newPlace.setImage(image);
                                                                }


                                                            }

                                                            placeDao.create(newPlace);
                                                            event.setPlace(newPlace);
                                                            Category categoryExists = helper.placeCategoryExists(placeCategory);
                                                            if (categoryExists == null) {
                                                                Category newCategory = new Category();
                                                                newCategory.setCategoryName(placeCategory);
                                                                helper.getCategoryDao().create(newCategory);
                                                                PlaceHasCategory placeHasCategories = new PlaceHasCategory(newPlace, newCategory);
                                                                helper.getPlaceHasCategoryRuntimeDao().create(placeHasCategories);
                                                            } else {
                                                                PlaceHasCategory trans_categories = new PlaceHasCategory(newPlace, categoryExists);
                                                                helper.getPlaceHasCategoryRuntimeDao().create(trans_categories);
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    placeDao.create(newPlace);
                                                    event.setPlace(newPlace);
                                                }
                                            } catch (ApiException e) {
                                                placeDao.create(newPlace);
                                                event.setPlace(newPlace);
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                placeDao.create(newPlace);
                                                event.setPlace(newPlace);
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                placeDao.create(newPlace);
                                                event.setPlace(newPlace);
                                                e.printStackTrace();
                                            }

                                        }
                                    } else {
                                        event.setPlace(place);
                                    }
                                }
                            }

                            if (((JSONObject) rawEventData.get(j)).has("owner")) {
                                //Person creator = new Person();
                                JSONObject from = ((JSONObject) rawEventData.get(j)).getJSONObject("owner");
                                if (from.has("id")) {
                                    Person personExists = helper.personExistsById((String) from.get("id"));
                                    if (personExists == null) {
                                        Person newPerson = new Person();
                                        newPerson.setId((String) from.get("id"));
                                        if (from.has("name")) {
                                            newPerson.setName((String) from.get("name"));
                                        }
                                        if (from.has("email")) {
                                            newPerson.setEmail((String) from.get("email"));
                                        }
                                        personDao.create(newPerson);
                                        event.setCreator(newPerson);
                                    } else {
                                        event.setCreator(personExists);
                                    }
                                }
                            }

                            if (((JSONObject) rawEventData.get(j)).has("description")) {
                                event.setDescription((String) ((JSONObject) rawEventData.get(j)).get("description"));
                            }



                            event.setTimestamp(System.currentTimeMillis() / 1000);
                            event.setSource("facebook");
                            eventDao.create(event);



                            totalItemsInserted++;
                            Log.d(TAG, "FbEventsInserted = " + totalItemsInserted);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG,"EXCEPTION ON event json");
                        e.printStackTrace();
                    }

                    //get next batch of results of exists
                    GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                    if (nextRequest != null) {
                        nextRequest.setCallback(this);
                        nextRequest.executeAsync();
                    }

                }
            }
        };
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackmanager.onActivityResult(requestCode, resultCode, data);
    }







}

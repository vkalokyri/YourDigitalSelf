package com.rutgers.neemi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
import com.rutgers.neemi.model.PaymentHasCategory;
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
    private Button fbbutton;
    ProgressDialog mProgress;
    // Creating Facebook CallbackManager Value
    public static CallbackManager callbackmanager;
    DatabaseHelper helper;

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

        // Initialize layout button
        fbbutton = (Button) findViewById(R.id.login_button);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Getting your facebook photos. Please wait ...");

        fbbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getResultsFromApi();
            }
        });
    }

    // Private method to handle Facebook login and callback
    private void getResultsFromApi() {


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
                        System.out.println("Success");

                        final RuntimeExceptionDao<Photo, String> photoDao = helper.getPhotoDao();
                        final RuntimeExceptionDao<Person, String> personDao = helper.getPersonDao();
                        final RuntimeExceptionDao<Album, String> albumDao = helper.getAlbumDao();
                        final RuntimeExceptionDao<Place, String> placeDao = helper.getPlaceDao();
                        final RuntimeExceptionDao<Event, String> eventDao = helper.getEventDao();
                        final RuntimeExceptionDao<PhotoTags, String> photoTagsDao = helper.getPhotoTagsDao();


                        Calendar cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());
                        cal.add(Calendar.MONTH, -12); // substract 6 months
                        DateTime since=new DateTime(cal.getTimeInMillis());
                        System.out.println("since = "+since);
                        String timestamp = null;



                        GenericRawResults<String[]> rawResults = photoDao.queryRaw("select max(timestamp) from Photo where source=\"facebook\";");
                        List<String[]> results = null;
                        try {
                            results = rawResults.getResults();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (results!=null){
                            String[] resultArray = results.get(0);
                            System.out.println("timestamp= " + resultArray[0]);
                            timestamp=resultArray[0];
                        }


                        if (timestamp!=null) {
                            cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());
                            cal.setTimeInMillis(Long.parseLong(timestamp)*1000);
                            since = new DateTime(cal.getTimeInMillis());
                        }

                        System.out.println("Since="+since);


                        Bundle parameters = new Bundle();
                        //
                        parameters.putString("fields", "name,id,link,album{name, description, event, place, created_time, from{name, email, id}},created_time,event,place,tags{name, email,id},from{name, email,id},picture");
                        parameters.putString("since", since.toString());

                        GraphRequest.Callback graphCallback = new GraphRequest.Callback(){
                            @Override
                            public void onCompleted(GraphResponse response) {

                                GeoApiContext context = new GeoApiContext.Builder()
                                        .apiKey("AIzaSyAG3EDauXS9f5BsCEPb90rl7Cdub2VvUZE")
                                        .build();
                                LatLng latLng= new LatLng();


                                if (response.getError() != null) {
                                    // handle error
                                    System.out.println("ERROR");
                                } else {
                                    System.out.println("Success");
                                    System.out.println("Hellohello = " + response.getRawResponse());
                                    JSONArray rawPhotosData = null;
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                                    int totalItemsInserted = 0;
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



                                                            placeDao.create(newPlace);
                                                            photo.setPlace(newPlace);

                                                            LatLng location = new LatLng(newPlace.getLatitude(),newPlace.getLongitude());
                                                            try {
                                                                PlacesSearchResponse gmapsResponse = PlacesApi.nearbySearchQuery(context, location)
                                                                        .radius(100)
                                                                        .keyword(newPlace.getName())
                                                                        .name(newPlace.getName())
                                                                        .await();
                                                                if (gmapsResponse.results!=null){
                                                                    for(String placeCategory: gmapsResponse.results[0].types){

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
                                                            } catch (ApiException e) {
                                                                e.printStackTrace();
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            } catch (IOException e) {
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

                                    mProgress.hide();


                                    Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    myIntent.putExtra("key", "facebook");
                                    myIntent.putExtra("items", totalItemsInserted);
                                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(myIntent);

//                                    if (totalItemsInserted == 0) {
//                                        Snackbar.make(findViewById(R.id.fbCoordinatorLayout), "No facebook photos fetched.", Snackbar.LENGTH_LONG ).show();
//                                    } else {
//                                        Snackbar.make(findViewById(R.id.fbCoordinatorLayout), totalItemsInserted+" facebook photos fetched.", Snackbar.LENGTH_LONG).show();
//                                    }

//                                    if (totalItemsInserted == 0) {
//                                        info.setText("No new events to retrieve.");
//                                    } else {
//                                        String outputText;
//                                        outputText="Photos retrieved using the Facebook API: "+ totalItemsInserted;
//                                        info.setText(outputText);
//                                    }
                                }


                            }
                        };

                        new GraphRequest(AccessToken.getCurrentAccessToken(),
                                "me/photos",parameters, HttpMethod.GET, graphCallback).executeAsync();

                    }

                    @Override
                    public void onCancel() {
                        Log.d("CANCEL", "On cancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("ERROR", error.toString());
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackmanager.onActivityResult(requestCode, resultCode, data);
    }







}


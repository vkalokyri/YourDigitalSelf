package com.rutgers.neemi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PhotoResult;
import com.google.maps.model.PlacesSearchResponse;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.interfaces.AuthenticationListener;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.Data;
import com.rutgers.neemi.model.InstagramResponse;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.PlaceHasCategory;
import com.rutgers.neemi.rest.RestClient;
import com.rutgers.neemi.AuthenticationDialog;
import com.rutgers.neemi.util.ObscuredSharedPreferences;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class InstagramActivity extends AppCompatActivity implements AuthenticationListener {


    private AuthenticationDialog auth_dialog;
    private DatabaseHelper helper;
    private ProgressDialog mProgress;
    SharedPreferences prefs ;
    AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram);
        helper=DatabaseHelper.getHelper(this);
        prefs = new ObscuredSharedPreferences(
                this, this.getSharedPreferences("preferences", Context.MODE_PRIVATE) );


        Intent i = getIntent();
        String permissionType = i.getStringExtra("action");

        if(permissionType.equals("grant")) {
            grantPermissions();
        }else if(permissionType.equals("sync")){
            String instagramToken = prefs.getString("instagram", null);
            if(instagramToken==null){
                grantPermissions();
                new MakeRequestTask().execute(instagramToken);
                //fetchData(instagramToken);
            }else {
                new MakeRequestTask().execute(instagramToken);
                //fetchData(instagramToken);
            }
        }else{
            revokePermissions();
            Intent myIntent = new Intent(this, MainActivity.class);
            myIntent.putExtra("key", "instagram");
            myIntent.putExtra("items", 0);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);

        }


    }


    public void revokePermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Revoke Permissions")
                .setMessage("To revoke the app's access to your Instagram account, do the following: \n" +
                        "Log in to instagram.com\n" +
                        "Click your username and select \"Edit Profile\" in the drop-down menu\n" +
                        "Click on \"Manage Applications\" in the side menu\n" +
                        "Find YourDigitalSelf app in the list (you may have more than one)\n" +
                        "Click the \"Revoke Access\" button in the top right corner")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();


    }

    public void grantPermissions(){
        String instagramToken = prefs.getString("instagram", null);
        if (instagramToken!=null){
            Toast.makeText(getApplicationContext(), "Instagram is already authenticated!", Toast.LENGTH_SHORT).show();
            Log.i("InstagramAPI" , "AlreadyAuthenticated!");
            Log.i("InstagramAPI" , instagramToken);

            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.putExtra("key", "instagram");
            myIntent.putExtra("items", -1);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);

        }else {

            mProgress = new ProgressDialog(this);
            mProgress.setMessage("Trying to authenticate ...");
            auth_dialog = new AuthenticationDialog(InstagramActivity.this, InstagramActivity.this);
            auth_dialog.setCancelable(true);
            auth_dialog.show();

        }
    }

    @Override
    public void onCodeReceived(final String access_token) {
        if (access_token == null) {
            auth_dialog.dismiss();
            mProgress.setMessage("Sorry, Instagram couldn't be authenticated.");
            mProgress.show();

        }else{
            Log.i("InstagramAPI" , "Authenticated!");
            prefs.edit().putString("instagram",access_token).commit();
            Log.i("InstagramAPI" , "AuthToken stored!");
            auth_dialog.dismiss();

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            preferences.edit().putBoolean("instagram", true).apply();

            Toast.makeText(getApplicationContext(), "Instagram was successfully authorized!", Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.putExtra("key", "instagram");
            myIntent.putExtra("items", -1);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);




//            builder.setTitle("Instagram was successfully authorized!")
//                    .setMessage("Do you want the app to get your past month's data or start collecting data from today?")
//                    .setPositiveButton("One month data", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            fetchData(access_token);
//                            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
//                            myIntent.putExtra("key", "instagram");
//                            myIntent.putExtra("items", 0);
//                            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            startActivity(myIntent);
//
//                        }
//                    })
//                    .setNegativeButton("Start from today", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
//                            myIntent.putExtra("key", "instagram");
//                            myIntent.putExtra("items", 0);
//                            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            startActivity(myIntent);
//
//                        }
//                    })
//                    .setIcon(android.R.drawable.ic_dialog_info)
//                    .show();


        }


       //fetchData(access_token);
    }


    public class MakeRequestTask extends AsyncTask<String, Void, Integer> {

        private Exception mLastError = null;


        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */

        @Override
        protected Integer doInBackground(String... params) {
            String access_token = params[0];
            return fetchData(access_token);
        }

        @Override
        protected void onPostExecute(Integer photosReceived) {
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.putExtra("key", "instagram");
            myIntent.putExtra("items", photosReceived);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);


        }


        public int fetchData(String access_token) {

            final RuntimeExceptionDao<Person, String> personDao = helper.getPersonDao();
            final RuntimeExceptionDao<Photo, String> photoDao = helper.getPhotoDao();
            final RuntimeExceptionDao<Place, String> placeDao = helper.getPlaceDao();
            final RuntimeExceptionDao<PhotoTags, String> photoTagsDao = helper.getPhotoTagsDao();

            String id = null;
            GenericRawResults<String[]> rawResults = photoDao.queryRaw("select id from Photo where _id=(select max(_id) from Photo where source=\"instagram\");");
            List<String[]> results = null;
            try {
                results = rawResults.getResults();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (results != null && results.size() > 0) {
                String[] resultArray = results.get(0);
                System.out.println("id= " + resultArray[0]);
                id = resultArray[0];
            }

            Call<InstagramResponse> call = null;

            if (id != null) {
                call = RestClient.getRetrofitService().getRecentMediaAfterID(access_token, 30, id);
            } else {
                call = RestClient.getRetrofitService().getRecentMedia(access_token, 30);

            }


            ArrayList<Data> data = new ArrayList<>();
            try {
                Response<InstagramResponse> response = call.execute();


                while (response.body() != null) {
                    System.out.println("photos of call = " + response.body().getData().size());
                    data.addAll(response.body().getData());
                    if (response.body().getPagination().getNext_max_id() != null) {
                        System.out.println("maxID = " + response.body().getPagination().getNext_max_id());
                        String maxId = response.body().getPagination().getNext_max_id();
                        call = RestClient.getRetrofitService().getRecentMediaAfterID(access_token, 30, maxId);
                        response = call.execute();
                    } else {
                        break;
                    }

                }


            } catch (IOException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            int photosReceived = data.size();
            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyDe8nWbXFA6ESFS6GnQtYPPsXzYmLz3Lf0")
                    .build();
            for (Data inst_photo : data) {
                Photo photo = new Photo();

                Person p = helper.personExistsById(inst_photo.getUser().getId());
                if (p == null) {
                    p = new Person();
                    p.setName(inst_photo.getUser().getFull_name());
                    p.setUsername(inst_photo.getUser().getUsername());
                    p.setId(inst_photo.getUser().getId());
                    personDao.create(p);
                }

                if (inst_photo.getLocation() != null) {
                    //Place place = new Place();


                    Place place = helper.placeExistsById(inst_photo.getLocation().getId());
                    if (place == null) {
                        place = new Place();
                        place.setLatitude(inst_photo.getLocation().getLatitude());
                        place.setLongitude(inst_photo.getLocation().getLongitude());
                        place.setName(inst_photo.getLocation().getName());
                        place.setStreet(inst_photo.getLocation().getStreet_address());

                        LatLng location = new LatLng(place.getLatitude(), place.getLongitude());
                        try {

                            PlacesSearchResponse gmapsResponse = PlacesApi.nearbySearchQuery(geoApiContext, location)
                                    .radius(100)
                                    .keyword(place.getName())
                                    .name(place.getName())
                                    .await();
                            if (gmapsResponse.results != null) {
                                if (gmapsResponse.results.length > 0) {
                                    if (gmapsResponse.results[0].photos != null) {
                                        com.google.maps.model.Photo photoFound = null;
                                        for (com.google.maps.model.Photo insPhoto : gmapsResponse.results[0].photos) {
                                            if (insPhoto.width > 750) {
                                                photoFound = insPhoto;
                                                break;
                                            }

                                        }
                                        if (photoFound != null) {
                                            PhotoResult photoResult = PlacesApi.photo(geoApiContext, photoFound.photoReference).maxWidth(1600).await();
                                            byte[] image = photoResult.imageData;
                                            place.setImage(image);
                                        } else {
                                            PhotoResult photoResult = PlacesApi.photo(geoApiContext, gmapsResponse.results[0].photos[0].photoReference).maxWidth(1600).await();
                                            byte[] image = photoResult.imageData;
                                            place.setImage(image);
                                        }


                                    }
                                    placeDao.create(place);
                                    photo.setPlace(place);
                                    for (String placeCategory : gmapsResponse.results[0].types) {
                                        Category categoryExists = helper.placeCategoryExists(placeCategory);
                                        if (categoryExists == null) {
                                            Category newCategory = new Category();
                                            newCategory.setCategoryName(placeCategory);
                                            helper.getCategoryDao().create(newCategory);
                                            PlaceHasCategory placeHasCategories = new PlaceHasCategory(place, newCategory);
                                            helper.getPlaceHasCategoryRuntimeDao().create(placeHasCategories);
                                        } else {
                                            PlaceHasCategory trans_categories = new PlaceHasCategory(place, categoryExists);
                                            helper.getPlaceHasCategoryRuntimeDao().create(trans_categories);
                                        }
                                    }
                                }
                            } else {
                                placeDao.create(place);
                                photo.setPlace(place);
                            }
                        } catch (ApiException e) {
                            placeDao.create(place);
                            photo.setPlace(place);
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            placeDao.create(place);
                            photo.setPlace(place);
                            e.printStackTrace();
                        } catch (IOException e) {
                            placeDao.create(place);
                            photo.setPlace(place);
                            e.printStackTrace();
                        }


                        // placeDao.create(place);
                        // photo.setPlace(place);
                    }else{
                        photo.setPlace(place);
                    }
                }


                photo.setCreator(p);
                photo.setCreated_time(inst_photo.getCreated_time()*1000);
                photo.setName(inst_photo.getCaption().getText());
                photo.setPicture(inst_photo.getImages().getThumbnail().getUrl());
                photo.setLink(inst_photo.getLink());
                photo.setTimestamp(System.currentTimeMillis());
                photo.setSource("instagram");
                photo.setId(inst_photo.getId());
                photoDao.create(photo);

                for (Data.UsersTagged taggedUser : inst_photo.getUsers_in_photo()) {
                    if (taggedUser.getUser().getId() != null) {
                        Person taggedPerson = helper.personExistsById(taggedUser.getUser().getId());
                        if (taggedPerson == null) {
                            taggedPerson = new Person();
                            taggedPerson.setId(taggedUser.getUser().getId());
                            taggedPerson.setName(taggedUser.getUser().getFull_name());
                            taggedPerson.setUsername(taggedUser.getUser().getUsername());
                            personDao.create(taggedPerson);
                        }
                        PhotoTags taggedPeople = new PhotoTags(taggedPerson, photo);
                        photoTagsDao.create(taggedPeople);
                    } else if (taggedUser.getUser().getUsername() != null) {
                        Person taggedPerson = helper.personExistsByUsername(taggedUser.getUser().getUsername());
                        if (taggedPerson == null) {
                            taggedPerson = new Person();
                            taggedPerson.setUsername(taggedUser.getUser().getUsername());
                            taggedPerson.setName(taggedUser.getUser().getFull_name());
                            personDao.create(taggedPerson);
                        }
                        PhotoTags taggedPeople = new PhotoTags(taggedPerson, photo);
                        photoTagsDao.create(taggedPeople);

                    }
                }
            }

            return photosReceived;


//         call.enqueue(new Callback<InstagramResponse>() {
//            @Override
//            public void onResponse(Call<InstagramResponse> call, Response<InstagramResponse> response) {
//
//                Log.d("Instagram", response.message());
//
//
//                if (response.body() != null) {
//
//                    int photosReceived =response.body().getData().length;
//                    for(Data inst_photo: response.body().getData()){
//                        Photo photo =new Photo();
//
//                        Person p = helper.personExistsById(inst_photo.getUser().getId());
//                        if (p==null) {
//                            p = new Person();
//                            p.setName(inst_photo.getUser().getFull_name());
//                            p.setUsername(inst_photo.getUser().getUsername());
//                            p.setId(inst_photo.getUser().getId());
//                            personDao.create(p);
//                        }
//
//                        if(inst_photo.getLocation()!=null) {
//                            Place place = new Place();
//                            place.setLatitude(inst_photo.getLocation().getLatitude());
//                            place.setLongitude(inst_photo.getLocation().getLongitude());
//                            place.setName(inst_photo.getLocation().getName());
//                            place.setStreet(inst_photo.getLocation().getStreet_address());
//                            placeDao.create(place);
//                            photo.setPlace(place);
//                        }
//
//
//                        photo.setCreator(p);
//                        photo.setCreated_time(inst_photo.getCreated_time());
//                        photo.setName(inst_photo.getCaption().getText());
//                        photo.setPicture(inst_photo.getImages().getThumbnail().getUrl());
//                        photo.setTimestamp(System.currentTimeMillis());
//                        photo.setSource("instagram");
//                        photo.setId(inst_photo.getId());
//                        photoDao.create(photo);
//
//                        for (Data.UsersTagged taggedUser: inst_photo.getUsers_in_photo()){
//                            if(taggedUser.getUser().getId()!=null) {
//                                Person taggedPerson = helper.personExistsById(taggedUser.getUser().getId());
//                                if (taggedPerson == null) {
//                                    taggedPerson = new Person();
//                                    taggedPerson.setId(taggedUser.getUser().getId());
//                                    taggedPerson.setName(taggedUser.getUser().getFull_name());
//                                    taggedPerson.setUsername(taggedUser.getUser().getUsername());
//                                    personDao.create(taggedPerson);
//                                }
//                                PhotoTags taggedPeople = new PhotoTags(taggedPerson, photo);
//                                photoTagsDao.create(taggedPeople);
//                            }else if(taggedUser.getUser().getUsername()!=null) {
//                                Person taggedPerson = helper.personExistsByUsername(taggedUser.getUser().getUsername());
//                                if (taggedPerson == null) {
//                                    taggedPerson = new Person();
//                                    taggedPerson.setUsername(taggedUser.getUser().getUsername());
//                                    taggedPerson.setName(taggedUser.getUser().getFull_name());
//                                    personDao.create(taggedPerson);
//                                }
//                                PhotoTags taggedPeople = new PhotoTags(taggedPerson, photo);
//                                photoTagsDao.create(taggedPeople);
//
//                            }
//                        }
//                    }
//
//
//
//
//                    //mProgress.dismiss();
//                    Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
//                    myIntent.putExtra("key", "instagram");
//                    myIntent.putExtra("items", photosReceived);
//                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(myIntent);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<InstagramResponse> call, Throwable t) {
//                //Handle failure
//                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_LONG).show();
//            }
//        });

//        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
//        myIntent.putExtra("key", "instagram");
//        myIntent.putExtra("items", 0);
//        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(myIntent);
        }
    }



    }
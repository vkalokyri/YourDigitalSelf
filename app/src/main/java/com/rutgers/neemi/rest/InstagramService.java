package com.rutgers.neemi.rest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PhotoResult;
import com.google.maps.model.PlacesSearchResponse;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.AuthenticationDialog;
import com.rutgers.neemi.DatabaseHelper;
import com.rutgers.neemi.InstagramActivity;
import com.rutgers.neemi.MainActivity;
import com.rutgers.neemi.interfaces.AuthenticationListener;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.Data;
import com.rutgers.neemi.model.InstagramResponse;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.PlaceHasCategory;
import com.rutgers.neemi.util.ObscuredSharedPreferences;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CheckedOutputStream;

import retrofit2.Call;
import retrofit2.Response;

public class InstagramService implements AuthenticationListener {

    private DatabaseHelper helper;
    private SharedPreferences prefs;
    private AuthenticationDialog auth_dialog;
    Context context;


    public InstagramService(Context context) {
        this.context=context;
        helper = DatabaseHelper.getHelper(context);
        prefs = new ObscuredSharedPreferences(
                context, context.getSharedPreferences("preferences", Context.MODE_PRIVATE));

        String instagramToken = prefs.getString("instagram", null);
        if (instagramToken == null) {
            grantPermissions(context);
            new MakeRequestTask().execute(instagramToken);
        } else {
            new MakeRequestTask().execute(instagramToken);
        }

    }


    public void grantPermissions(Context context) {
        String instagramToken = prefs.getString("instagram", null);
        if (instagramToken != null) {
            Log.i("InstagramAPI", "AlreadyAuthenticated!");
            Log.i("InstagramAPI", instagramToken);
        } else {
            auth_dialog = new AuthenticationDialog(context, this);
            auth_dialog.setCancelable(true);
            auth_dialog.show();

        }
    }

    @Override
    public void onCodeReceived(String access_token) {
        if (access_token == null) {
            auth_dialog.dismiss();
           Log.e("InstagramService","Instagram couldn't get authenticated");

        }else {
            Log.i("InstagramAPI", "Authenticated!");
            prefs.edit().putString("instagram", access_token).commit();
            Log.i("InstagramAPI", "AuthToken stored!");
            auth_dialog.dismiss();


            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.edit().putBoolean("instagram", true).apply();
        }

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
            Log.i("Instagram service", "Received"+ photosReceived+ "instagram photos");

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
                    } else {
                        photo.setPlace(place);
                    }
                }


                photo.setCreator(p);
                photo.setCreated_time(inst_photo.getCreated_time() * 1000);
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


        }
    }
}

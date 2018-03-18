package com.rutgers.neemi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.interfaces.AuthenticationListener;
import com.rutgers.neemi.model.Data;
import com.rutgers.neemi.model.InstagramResponse;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.rest.RestClient;
import com.rutgers.neemi.AuthenticationDialog;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class InstagramActivity extends AppCompatActivity implements AuthenticationListener {


    private AuthenticationDialog auth_dialog;
    private Button btn_get_access_token;
    private DatabaseHelper helper;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram);
        helper=DatabaseHelper.getHelper(this);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Getting your recent instagram photos. Please wait ...");



        btn_get_access_token = (Button) findViewById(R.id.btn_get_access_token);

        btn_get_access_token.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth_dialog = new AuthenticationDialog(InstagramActivity.this, InstagramActivity.this);
                auth_dialog.setCancelable(true);
                auth_dialog.show();
            }
        });
    }

    @Override
    public void onCodeReceived(String access_token) {
        if (access_token == null) {
            auth_dialog.dismiss();
            mProgress.show();
        }

        fetchData(access_token);
    }


    public void fetchData(String access_token) {
        final RuntimeExceptionDao<Person, String> personDao = helper.getPersonDao();
        final RuntimeExceptionDao<Photo, String> photoDao = helper.getPhotoDao();
        final RuntimeExceptionDao<Place, String> placeDao = helper.getPlaceDao();
        final RuntimeExceptionDao<PhotoTags, String> photoTagsDao = helper.getPhotoTagsDao();

        String id=null;
        GenericRawResults<String[]> rawResults = photoDao.queryRaw("select id from Photo where _id=(select max(_id) from Photo where source=\"instagram\");");
        List<String[]> results = null;
        try {
            results = rawResults.getResults();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (results!=null && results.size()>0){
            String[] resultArray = results.get(0);
            System.out.println("id= " + resultArray[0]);
            id=resultArray[0];
        }

        Call<InstagramResponse> call=null;

        if (id!=null) {
             call = RestClient.getRetrofitService().getRecentMediaAfterID(access_token,id);
        }else{
            call = RestClient.getRetrofitService().getRecentMedia(access_token);

        }

        call.enqueue(new Callback<InstagramResponse>() {
            @Override
            public void onResponse(Call<InstagramResponse> call, Response<InstagramResponse> response) {

                if (response.body() != null) {
                    int photosReceived =response.body().getData().length;
                    for(Data inst_photo: response.body().getData()){
                        Photo photo =new Photo();

                        Person p = helper.personExistsById(inst_photo.getUser().getId());
                        if (p==null) {
                            p = new Person();
                            p.setName(inst_photo.getUser().getFull_name());
                            p.setUsername(inst_photo.getUser().getUsername());
                            p.setId(inst_photo.getUser().getId());
                            personDao.create(p);
                        }

                        if(inst_photo.getLocation()!=null) {
                            Place place = new Place();
                            place.setLatitude(inst_photo.getLocation().getLatitude());
                            place.setLongitude(inst_photo.getLocation().getLongitude());
                            place.setName(inst_photo.getLocation().getName());
                            place.setStreet(inst_photo.getLocation().getStreet_address());
                            placeDao.create(place);
                            photo.setPlace(place);
                        }


                        photo.setCreator(p);
                        photo.setCreated_time(inst_photo.getCreated_time());
                        photo.setName(inst_photo.getCaption().getText());
                        photo.setPicture(inst_photo.getImages().getThumbnail().getUrl());
                        photo.setTimestamp(System.currentTimeMillis());
                        photo.setSource("instagram");
                        photo.setId(inst_photo.getId());
                        photoDao.create(photo);

                        for (Data.UsersTagged taggedUser: inst_photo.getUsers_in_photo()){
                            Person taggedPerson = helper.personExistsById(taggedUser.getUser().getId());
                            if(taggedPerson==null){
                                taggedPerson=new Person();
                                taggedPerson.setId(taggedUser.getUser().getId());
                                taggedPerson.setName(taggedUser.getUser().getFull_name());
                                taggedPerson.setUsername(taggedUser.getUser().getUsername());
                                personDao.create(taggedPerson);
                            }
                            PhotoTags taggedPeople = new PhotoTags(taggedPerson, photo);
                            photoTagsDao.create(taggedPeople);
                        }
                    }
                    mProgress.dismiss();
                    Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                    myIntent.putExtra("key", "instagram");
                    myIntent.putExtra("items", photosReceived);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
            }

            @Override
            public void onFailure(Call<InstagramResponse> call, Throwable t) {
                //Handle failure
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_LONG).show();
            }
        });

        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        myIntent.putExtra("key", "instagram");
        myIntent.putExtra("items", 0);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myIntent);
    }



}
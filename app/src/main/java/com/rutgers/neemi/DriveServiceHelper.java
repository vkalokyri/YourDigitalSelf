package com.rutgers.neemi;

/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private static final String TAG = "DriveServiceHelper";
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    RuntimeExceptionDao<GPSLocation, String> gpsDao;
    DatabaseHelper helper;
    Double overflow = Double.parseDouble("4294967296");
    RuntimeExceptionDao<StayPoint, String> stayPointDao;
    GeoApiContext geoApiContext = new GeoApiContext.Builder()
            .apiKey("AIzaSyDe8nWbXFA6ESFS6GnQtYPPsXzYmLz3Lf0")
            .build();





    public DriveServiceHelper(Drive driveService, DatabaseHelper helper) {
        mDriveService = driveService;
        gpsDao = helper.getGpsLocationtRuntimeDao();
        this.helper = helper;

    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName("Untitled file");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.

            Log.d(TAG, "readFile");

            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("drive").execute());
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");

        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    public Task<Pair<String,Integer>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            Log.d(TAG, "On openFileUsingStorageAccessFramework");

            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            int readZip;
            try (InputStream is = contentResolver.openInputStream(uri);){
                  readZip = readZipFile(is);

            }

            return Pair.create(name, readZip);
        });
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
                } catch (ApiException e) {
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


    public ArrayList<Place> getPlaces(double lat, double lon) throws InterruptedException, ApiException, IOException, com.google.maps.errors.ApiException {
        LatLng location = new LatLng(lat, lon);
        ArrayList<Place> places = new ArrayList<>();

        PlacesSearchResponse gmapsResponse = PlacesApi.nearbySearchQuery(geoApiContext, location)
                .radius(50)
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
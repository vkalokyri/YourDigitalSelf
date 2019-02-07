package com.rutgers.neemi.model;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.j256.ormlite.field.DatabaseField;

import java.io.IOException;

public class GPSLocation {

    @DatabaseField
    long timestamp;
    @DatabaseField
    double latitude;
    @DatabaseField
    double longitude;


    public GPSLocation(long timestamp, double latitude, double longitude) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GPSLocation(){

    }


    public String getPlaceName(double lat, double lon) throws InterruptedException, ApiException, IOException {
        LatLng location =  new LatLng(lat, lon);
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyDe8nWbXFA6ESFS6GnQtYPPsXzYmLz3Lf0")
                .build();

        PlacesSearchResponse gmapsResponse = PlacesApi.nearbySearchQuery(geoApiContext, location)
                .radius(100)
                .await();
        if (gmapsResponse.results != null) {
            if (gmapsResponse.results.length > 0) {
                return gmapsResponse.results[0].name;
            }
        }
        return null;

    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

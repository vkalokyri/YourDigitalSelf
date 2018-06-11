package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;

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
}

package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by suitcase on 7/19/17.
 */

@DatabaseTable(tableName = "Place")
public class Place {

    public static final String FIELD_ID = "id";
    public static final String FIELD_LAT = "latitude";
    public static final String FIELD_LONG = "longitude";
    public static final String FIELD_PHONE ="phone";
    public static final String FIELD_CITY ="city";
    public static final String FIELD_STATE ="state";


    @DatabaseField(generatedId = true)
    int _id;
    @DatabaseField(columnName = FIELD_ID)
    String id;
    @DatabaseField
    String name;
    @DatabaseField(columnName = FIELD_CITY)
    String city;
    @DatabaseField
    String country;
    @DatabaseField(columnName = FIELD_LAT)
    double latitude;
    @DatabaseField(columnName = FIELD_LONG)
    double longitude;
    @DatabaseField
    String region;
    @DatabaseField(columnName = FIELD_STATE)
    String state;
    @DatabaseField
    String street;
    @DatabaseField
    String zip;
    @DatabaseField(columnName = FIELD_PHONE)
    String phone_number;
    @DatabaseField
    String place_photo_url;

    public Place() {
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getPlace_photo_url() {
        return place_photo_url;
    }

    public void setPlace_photo_url(String place_photo_url) {
        this.place_photo_url = place_photo_url;
    }
}

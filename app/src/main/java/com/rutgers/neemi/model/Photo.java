package com.rutgers.neemi.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 7/19/17.
 */
@DatabaseTable(tableName = "Photo")
public class Photo implements Serializable {

    @DatabaseField(generatedId = true)
    int _id;
    @DatabaseField
    long timestamp;
    @DatabaseField
    String id;
    @DatabaseField
    String name;
    @DatabaseField
    String link;
    @DatabaseField(canBeNull = true, foreign = true, columnName = "album_id")
    Album album;
    @DatabaseField
    long created_time;
    @DatabaseField(canBeNull = true, foreign = true, columnName = "event_id")
    Event event;
    @DatabaseField(canBeNull = true, foreign = true, columnName = "place_id")
    Place place;
    @DatabaseField(canBeNull = true, foreign = true, columnName = "creator_id")
    Person creator;
    @DatabaseField
    String picture;
    @DatabaseField
    String source;



    public Photo() {
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public long getCreated_time() {
        return created_time;
    }

    public void setCreated_time(long created_time) {
        this.created_time = created_time;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

//    public ForeignCollection<Person> getTags() {
//        return tags;
//    }
//
//    public void setTags(ForeignCollection<Person> tags) {
//        this.tags = tags;
//    }
//
//    public void setTag(Person taggedPerson) {this.tags.add(taggedPerson);    }

    public Person getCreator() {
        return creator;
    }

    public void setCreator(Person creator) {
        this.creator = creator;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}

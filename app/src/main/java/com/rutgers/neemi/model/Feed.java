package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 7/19/17.
 */
@DatabaseTable(tableName = "Feed")
public class Feed implements Serializable {

    @DatabaseField(generatedId = true)
    int _id;
    @DatabaseField
    long timestamp;
    @DatabaseField
    String id;
    @DatabaseField
    String message;
    @DatabaseField
    String link;
    @DatabaseField
    String description;
    @DatabaseField
    String object_id;
    @DatabaseField
    long created_time;
    @DatabaseField(canBeNull = true, foreign = true, foreignAutoRefresh=true)
    Place place;
    @DatabaseField(canBeNull = true, foreign = true, foreignAutoRefresh=true)
    Person creator;
    @DatabaseField
    String picture;
    @DatabaseField
    String story;
    @DatabaseField
    String type;
    @DatabaseField
    String source;



    public Feed() {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObject_id() {
        return object_id;
    }

    public void setObject_id(String object_id) {
        this.object_id = object_id;
    }

    public long getCreated_time() {
        return created_time;
    }

    public void setCreated_time(long created_time) {
        this.created_time = created_time;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

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

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
}

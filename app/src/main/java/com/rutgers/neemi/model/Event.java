package com.rutgers.neemi.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 6/26/17.
 */

@DatabaseTable(tableName = "Event")
public class Event implements Serializable {

    public static final String FIELD_ID = "ID";

    @DatabaseField(generatedId = true)
    int _id;
    @DatabaseField
    long timestamp;
    @DatabaseField (columnName = FIELD_ID)
    String id;
    @DatabaseField
    String title;
    @DatabaseField
    String description;
    //@ForeignCollectionField(eager = false, columnName="attendees")
    //ForeignCollection<Person> attendees;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Person(_id) ON DELETE CASCADE")
    Person creator;
    @DatabaseField
    long dateCreated;
    @DatabaseField
    long startTime;
    @DatabaseField
    long endTime;
    @DatabaseField
    String location;
    @DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Person(_id) ON DELETE CASCADE")
    Person organizer;
    @DatabaseField
    String source;
    @DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Place(_id) ON DELETE CASCADE")
    Place place;

    public Event() {

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public ForeignCollection<Person> getAttendees() {
//        return attendees;
//    }
//
//    public void setAttendees(ForeignCollection<Person> attendees) {
//        this.attendees = attendees;
//    }
//
//    public void setAttendee(Person attendee) {
//        this.attendees.add(attendee);
//    }

    public Person getCreator() {
        return creator;
    }

    public void setCreator(Person creator) {
        this.creator = creator;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Person getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Person organizer) {
        this.organizer = organizer;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}

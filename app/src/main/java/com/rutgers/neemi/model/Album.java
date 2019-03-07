package com.rutgers.neemi.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 7/19/17.
 */
@DatabaseTable(tableName = "Album")
public class Album implements Serializable {

    public static final String FIELD_ID = "ID";

    @DatabaseField(generatedId = true)
    int _id;
    @DatabaseField(columnName = FIELD_ID)
    String id;
    @DatabaseField
    String name;
    @DatabaseField
    String description;
    @DatabaseField
    long created_time;
    @DatabaseField(canBeNull = true, foreign = true,foreignAutoRefresh=true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Event(_id) ON DELETE CASCADE")
    Event event;
    @DatabaseField(canBeNull = true, foreign = true,foreignAutoRefresh=true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Place(_id) ON DELETE CASCADE")
    Place place;
    @DatabaseField(canBeNull = true, foreign = true,foreignAutoRefresh=true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Person(_id) ON DELETE CASCADE")
    Person creator;

    public Album() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Person getCreator() {
        return creator;
    }

    public void setCreator(Person creator) {
        this.creator = creator;
    }
}

package com.rutgers.neemi.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 6/26/17.
 */

@DatabaseTable(tableName = "EventAttendees")
public class EventAttendees implements Serializable {

    @DatabaseField(generatedId = true)
    int _id;


    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh=true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Person(_id) ON DELETE CASCADE")
    Person attendee;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh=true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Event(_id) ON DELETE CASCADE")
    Event event;

    public EventAttendees() {
        // for ormlite
    }

    public EventAttendees(Person person, Event event) {
        this.attendee = person;
        this.event = event;
    }

}

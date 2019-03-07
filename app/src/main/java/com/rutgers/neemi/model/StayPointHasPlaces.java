package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 7/19/17.
 */
@DatabaseTable(tableName = "StayPointHasPlaces")
public class StayPointHasPlaces implements Serializable {


    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Place(_id) ON DELETE CASCADE")
    Place place;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES StayPoint(_id) ON DELETE CASCADE")
    StayPoint stayPoint;

    public StayPointHasPlaces() {
        // for ormlite
    }

    public StayPointHasPlaces(Place place, StayPoint stayPoint) {
        this.place = place;
        this.stayPoint = stayPoint;
    }

}

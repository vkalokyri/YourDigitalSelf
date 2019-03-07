package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;


@DatabaseTable(tableName = "StayPoint")
public class StayPoint implements Serializable {

    @DatabaseField(generatedId = true)
    int _id;
    @DatabaseField (canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES GPSLocation(_id) ON DELETE CASCADE")
    private GPSLocation coord;
    @DatabaseField
    private long arrive;
    @DatabaseField
    private long leave;
    @DatabaseField
    private long duration;



    public StayPoint() {
    }

    public StayPoint(GPSLocation coord, long arrive, long leave) {
        this.coord = coord;
        this.arrive = arrive;
        this.leave = leave;
    }

    public GPSLocation getCoord() {
        return coord;
    }

    public void setCoord(GPSLocation coord) {
        this.coord = coord;
    }

    public long getArrive() {
        return arrive;
    }

    public void setArrive(long arrive) {
        this.arrive = arrive;
    }

    public long getLeave() {
        return leave;
    }

    public void setLeave(long leave) {
        this.leave = leave;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
}

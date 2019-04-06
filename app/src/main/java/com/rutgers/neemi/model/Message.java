package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by suitcase on 6/25/17.
 */

@DatabaseTable(tableName = "Message")
public class Message implements Serializable{

        @DatabaseField(generatedId = true)
        int _id;
        @DatabaseField
        String thread;
        @DatabaseField
        long timestamp;
        @DatabaseField (foreign = true,foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Person(_id) ON DELETE CASCADE")
        Person from;
        @DatabaseField
        String content;
        @DatabaseField
        Date contentDate;


        ArrayList<Person> to = new ArrayList<>();


    public Message() {
            // ORMLite needs a no-arg constructor
        }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public Person getFrom() {
        return from;
    }

    public void setFrom(Person from) {
        this.from = from;
    }

    public ArrayList<Person> getTo() {
        return to;
    }

    public void setTo(ArrayList<Person> to) {
        this.to = to;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getContentDate() {
        return contentDate;
    }

    public void setContentDate(Date contentDate) {
        this.contentDate = contentDate;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }
}
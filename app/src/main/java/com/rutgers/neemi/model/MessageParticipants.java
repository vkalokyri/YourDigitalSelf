package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 6/25/17.
 */

@DatabaseTable(tableName = "MessageParticipants")
public class MessageParticipants implements Serializable{


    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true,foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Person(_id) ON DELETE CASCADE")
    Person participant;

    @DatabaseField
    String thread_id;

    public MessageParticipants(Person participant, String thread_id) {
        this.participant = participant;
        this.thread_id = thread_id;
    }


    public MessageParticipants() {
            // ORMLite needs a no-arg constructor
    }

    public Person getParticipant() {
        return participant;
    }

    public void setParticipant(Person participant) {
        this.participant = participant;
    }

    public String getThread_id() {
        return thread_id;
    }

    public void setThread_id(String thread_id) {
        this.thread_id = thread_id;
    }
}
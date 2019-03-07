package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by suitcase on 6/25/17.
 */

@DatabaseTable(tableName = "EmailTo")
public class EmailTo implements Serializable{


    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true,foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Person(_id) ON DELETE CASCADE")
    Person to;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true,foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Εmail(_id) ON DELETE CASCADE")
    Email email;

    public EmailTo(Person person, Email email) {
        this.to = person;
        this.email = email;
    }


    public EmailTo() {
            // ORMLite needs a no-arg constructor
        }

    public Person getTo() {
        return to;
    }

    public void setTo(Person to) {
        this.to = to;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }
}
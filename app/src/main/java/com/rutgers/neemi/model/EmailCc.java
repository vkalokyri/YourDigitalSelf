package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 6/25/17.
 */

@DatabaseTable(tableName = "EmailCc")
public class EmailCc implements Serializable{


    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true,foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Person(_id) ON DELETE CASCADE")
    Person cc;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true,foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Email(_id) ON DELETE CASCADE")
    Email email;

    public EmailCc(Person person, Email email) {
        this.cc = person;
        this.email = email;
    }


    public EmailCc() {
            // ORMLite needs a no-arg constructor
        }

    public Person getCc() {
        return cc;
    }

    public void setCc(Person cc) {
        this.cc = cc;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }
}
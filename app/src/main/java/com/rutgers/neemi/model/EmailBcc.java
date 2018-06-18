package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 6/25/17.
 */

@DatabaseTable(tableName = "EmailBcc")
public class EmailBcc implements Serializable{


    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true,foreignAutoRefresh = true)
    Person bcc;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true,foreignAutoRefresh = true)
    Email email;

    public EmailBcc(Person person, Email email) {
        this.bcc = person;
        this.email = email;
    }


    public EmailBcc() {
            // ORMLite needs a no-arg constructor
        }

    public Person getBcc() {
        return bcc;
    }

    public void setBcCc(Person bcc) {
        this.bcc = bcc;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }
}
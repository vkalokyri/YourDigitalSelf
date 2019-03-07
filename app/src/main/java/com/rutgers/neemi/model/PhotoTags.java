package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 6/26/17.
 */

@DatabaseTable(tableName = "PhotoTags")
public class PhotoTags implements Serializable {

    @DatabaseField(generatedId = true)
    int _id;


    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true,foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Person(_id) ON DELETE CASCADE")
    Person tagged;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true,foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Photo(_id) ON DELETE CASCADE")
    Photo photo;

    public PhotoTags() {
        // for ormlite
    }

    public PhotoTags(Person person, Photo photo) {
        this.photo = photo;
        this.tagged = person;
    }

}

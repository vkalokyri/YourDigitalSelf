package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 6/26/17.
 */

@DatabaseTable(tableName = "FeedWithTags")
public class FeedWithTags implements Serializable {

    @DatabaseField(generatedId = true)
    int _id;


    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true,foreignAutoRefresh=true)
    Person tagged;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh=true)
    Feed feed;

    public FeedWithTags() {
        // for ormlite
    }

    public FeedWithTags(Person person, Feed feed) {
        this.feed = feed;
        this.tagged = person;
    }

}

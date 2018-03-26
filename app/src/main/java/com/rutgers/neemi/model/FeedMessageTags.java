package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 6/26/17.
 */

@DatabaseTable(tableName = "FeedMessageTags")
public class FeedMessageTags implements Serializable {

    @DatabaseField(generatedId = true)
    int _id;


    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true, columnName = "person_id")
    Person tagged;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, columnName = "feed_id")
    Feed feed;

    public FeedMessageTags() {
        // for ormlite
    }

    public FeedMessageTags(Person person, Feed feed) {
        this.feed = feed;
        this.tagged = person;
    }

}

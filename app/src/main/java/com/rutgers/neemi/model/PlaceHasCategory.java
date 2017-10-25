package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 7/19/17.
 */
@DatabaseTable(tableName = "PlaceHasCategory")
public class PlaceHasCategory implements Serializable {


    @DatabaseField(generatedId = true)
    int _id;

    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true, columnName = "place_id")
    Place place;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, columnName = "category_id")
    Category category;

    public PlaceHasCategory() {
        // for ormlite
    }

    public PlaceHasCategory(Place place, Category category) {
        this.place = place;
        this.category = category;
    }

}

package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 7/19/17.
 */
@DatabaseTable(tableName = "TransactionHasPlaces")
public class TransactionHasPlaces implements Serializable {


    @DatabaseField(generatedId = true)
    int _id;

    // This is a foreign object which just stores the id from the Person object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES `Transaction`(_id) ON DELETE CASCADE")
    Transaction transaction;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Place(_id) ON DELETE CASCADE")
    Place place;

    public TransactionHasPlaces() {
        // for ormlite
    }

    public TransactionHasPlaces(Transaction transaction, Place place) {
        this.transaction = transaction;
        this.place = place;
    }

}

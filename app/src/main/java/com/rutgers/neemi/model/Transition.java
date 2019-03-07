package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 7/19/17.
 */
@DatabaseTable(tableName = "Transition")
public class Transition implements Serializable {

    public static final String SOURCE = "source";
    public static final String TARGET = "target";


    @DatabaseField(foreign = true, columnName = SOURCE, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Task(_id) ON DELETE CASCADE")
    Task source;

    @DatabaseField(foreign = true, columnName = TARGET, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Task(_id) ON DELETE CASCADE")
    Task target;



    public Transition() {
    }


}

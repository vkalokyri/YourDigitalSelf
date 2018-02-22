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


    @DatabaseField(foreign = true, columnName = SOURCE)
    Task source;

    @DatabaseField(foreign = true, columnName = TARGET)
    Task target;



    public Transition() {
    }


}

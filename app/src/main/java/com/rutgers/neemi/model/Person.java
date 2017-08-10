package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 6/28/17.
 */

@DatabaseTable(tableName = "Person")
public class Person implements Serializable {

    public static final String FIELD_ID = "ID";
    public static final String FIELD_EMAIL = "EMAIL";
    public static final String FIELD_NAME = "NAME";

    @DatabaseField(generatedId = true)
    int _id;
    @DatabaseField(columnName = FIELD_ID)
    String id;
    @DatabaseField(columnName = FIELD_NAME)
    String name;
    @DatabaseField(columnName = FIELD_EMAIL)
    String email;
    @DatabaseField
    boolean isSelf;



    public Person(){
        
    }


    public Person(String name, String email, boolean isSelf) {
        this.name = name;
        this.email = email;
        this.isSelf = isSelf;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }
}

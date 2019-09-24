package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by suitcase on 6/28/17.
 */

@DatabaseTable(tableName = "Person")
public class Person implements Serializable {

    public static final String FIELD_ID = "id";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PHONE = "phone";


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
    @DatabaseField(columnName = FIELD_USERNAME)
    String username;
    @DatabaseField(columnName = FIELD_PHONE)
    String phone;


    public Person(){
        
    }

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Person(String phone, String name, String email) {
        this.phone = phone;
        this.name=name;
        this.email=email;
    }


    public Person(String name, String email, String username, boolean isSelf) {
        this.name = name;
        this.email = email;
        this.isSelf = isSelf;
        this.username = username;

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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

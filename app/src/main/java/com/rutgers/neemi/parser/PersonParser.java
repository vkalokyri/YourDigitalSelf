package com.rutgers.neemi.parser;

import android.content.Context;

import com.rutgers.neemi.DatabaseHelper;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.KeyValuePair;
import com.rutgers.neemi.model.Person;

import java.util.ArrayList;
import java.util.List;

public class PersonParser {

    DatabaseHelper helper;

    public PersonParser(DatabaseHelper helper){
        this.helper=helper;

    }

    public ArrayList<ArrayList<KeyValuePair>> parse() {
        List<Person> allPersons = helper.getPersonDao().queryForAll();
        ArrayList<ArrayList<KeyValuePair>> people = new ArrayList<ArrayList<KeyValuePair>>();

        for (Person person : allPersons) {
            ArrayList keyValues = new ArrayList<KeyValuePair>();
            KeyValuePair nameKeyValue = null;
            KeyValuePair emailKeyValue = null;

            String pname = person.getName();
            String pemail = person.getEmail();
            if (pname != null && !pname.equals("")) {
                nameKeyValue = new KeyValuePair("name", pname.trim());
                keyValues.add(nameKeyValue);
            }
            if (pemail != null && !pemail.equals("")) {
                emailKeyValue = new KeyValuePair("email", pemail.trim());
                keyValues.add(emailKeyValue);
            }
            if (nameKeyValue != null || emailKeyValue != null) {
                keyValues.add(new KeyValuePair("id", Integer.toString(person.get_id())));
            }

            people.add(keyValues);

        }
        return people;
    }


}

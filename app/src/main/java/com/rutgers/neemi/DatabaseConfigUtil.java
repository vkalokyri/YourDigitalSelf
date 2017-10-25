package com.rutgers.neemi;

import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;
import com.rutgers.neemi.model.Album;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.EventAttendees;
import com.rutgers.neemi.model.Payment;
import com.rutgers.neemi.model.PaymentHasCategory;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.PlaceHasCategory;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    // put all your database classes in this array
    private static final Class<?>[] classes = new Class[]{Email.class, Event.class, Person.class, Album.class, Photo.class, Place.class, EventAttendees.class, PhotoTags.class, PaymentHasCategory.class, PlaceHasCategory.class, Payment.class, Category.class};


    public static void main(String[] args) throws SQLException, IOException {
        writeConfigFile("ormlite_config.txt", classes);
    }
}

package com.rutgers.neemi;

import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;
import com.rutgers.neemi.model.Album;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.EmailBcc;
import com.rutgers.neemi.model.EmailCc;
import com.rutgers.neemi.model.EmailTo;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.EventAttendees;
import com.rutgers.neemi.model.Feed;
import com.rutgers.neemi.model.FeedMessageTags;
import com.rutgers.neemi.model.FeedWithTags;
import com.rutgers.neemi.model.GPSLocation;
import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.Message;
import com.rutgers.neemi.model.MessageParticipants;
import com.rutgers.neemi.model.ScriptLocalValues;
import com.rutgers.neemi.model.ScriptDefHasTaskDef;
import com.rutgers.neemi.model.ScriptLocalValues;
import com.rutgers.neemi.model.StayPoint;
import com.rutgers.neemi.model.StayPointHasPlaces;
import com.rutgers.neemi.model.TaskDefHasLocalProperties;
import com.rutgers.neemi.model.TaskLocalValues;
import com.rutgers.neemi.model.Transaction;
import com.rutgers.neemi.model.TransactionHasCategory;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.PlaceHasCategory;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.ScriptDefinition;
import com.rutgers.neemi.model.Subscript;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.model.TaskDefinition;
import com.rutgers.neemi.model.TransactionHasPlaces;
import com.rutgers.neemi.model.Transition;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    // put all your database classes in this array
    private static final Class<?>[] classes = new Class[]{Email.class, EmailCc.class,EmailBcc.class,EmailTo.class, Event.class, Person.class, Album.class, Photo.class, Place.class, EventAttendees.class, PhotoTags.class, TransactionHasCategory.class, PlaceHasCategory.class, Transaction.class, Category.class, Task.class, Script.class, TaskDefinition.class, ScriptDefinition.class, LocalProperties.class, TaskLocalValues.class, ScriptLocalValues.class, ScriptDefHasTaskDef.class, Subscript.class, Transition.class, Feed.class, FeedMessageTags.class, FeedWithTags.class, TaskDefHasLocalProperties.class, GPSLocation.class, StayPoint.class, StayPointHasPlaces.class, TransactionHasPlaces.class, Message.class, MessageParticipants.class};


    public static void main(String[] args) throws SQLException, IOException {
        writeConfigFile("ormlite_config.txt", classes);
    }
}

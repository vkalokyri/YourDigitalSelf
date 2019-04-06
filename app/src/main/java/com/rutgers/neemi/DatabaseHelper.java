package com.rutgers.neemi;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
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
import com.rutgers.neemi.model.ScriptDefHasLocalProperties;
import com.rutgers.neemi.model.ScriptLocalValues;
import com.rutgers.neemi.model.ScriptDefHasTaskDef;
import com.rutgers.neemi.model.ScriptLocalValues;
import com.rutgers.neemi.model.StayPoint;
import com.rutgers.neemi.model.StayPointHasPlaces;
import com.rutgers.neemi.model.TaskDefHasLocalProperties;
import com.rutgers.neemi.model.TaskLocalValues;
import com.rutgers.neemi.model.TransactionHasCategory;
import com.rutgers.neemi.model.Transaction;
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

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for the application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "neemi.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;

	// the DAO objects we use to access our tables
	private RuntimeExceptionDao<Email, String> emailRuntimeDao = null;
	private RuntimeExceptionDao<EmailTo, String> emailToRuntimeDao = null;
	private RuntimeExceptionDao<EmailCc, String> emailCcRuntimeDao = null;
	private RuntimeExceptionDao<EmailBcc, String> emailBccRuntimeDao = null;
	private RuntimeExceptionDao<Event, String> calEventRuntimeDao = null;
	private RuntimeExceptionDao<Person, String> personRuntimeDao = null;
	private RuntimeExceptionDao<Photo, String> photoRuntimeDao = null;
	private RuntimeExceptionDao<Feed, String> feedRuntimeDao = null;
	private RuntimeExceptionDao<Album, String> albumRuntimeDao = null;
	private RuntimeExceptionDao<Place, String> placeRuntimeDao = null;
	private RuntimeExceptionDao<EventAttendees, String> eventAttendeesRuntimeDao = null;
	private RuntimeExceptionDao<PhotoTags, String> photoTagsRuntimeDao = null;
	private RuntimeExceptionDao<FeedWithTags, String> feedWithTagsRuntimeDao = null;
	private RuntimeExceptionDao<FeedMessageTags, String> feedMessageTagsRuntimeDao = null;
	private RuntimeExceptionDao<Transaction, String> transactionRuntimeDao = null;
	private RuntimeExceptionDao<Category, String> categoryRuntimeDao = null;
	private RuntimeExceptionDao<PlaceHasCategory, String> placeHasCategoryRuntimeDao = null;
	private RuntimeExceptionDao<TransactionHasCategory, String> transactionHasCategoryRuntimeDao = null;
	private RuntimeExceptionDao<Script, String> scriptRuntimeDao = null;
    private RuntimeExceptionDao<ScriptDefinition, String> scriptDefRuntimeDao = null;
    private RuntimeExceptionDao<ScriptDefHasTaskDef, String> scriptDefHasTaskDefRuntimeDao = null;
	private RuntimeExceptionDao<ScriptDefHasLocalProperties, String> scriptDefHasLocalPropertiesRuntimeDao = null;
	private RuntimeExceptionDao<TaskDefHasLocalProperties, String> taskDefHasLocalPropertiesRuntimeDao = null;
	private RuntimeExceptionDao<Task, String> taskRuntimeDao = null;
    private RuntimeExceptionDao<TaskDefinition, String> taskDefRuntimeDao = null;
	private RuntimeExceptionDao<LocalProperties, String> localPropertiesRuntimeDao = null;
    private RuntimeExceptionDao<ScriptLocalValues, String> scriptlocalsRuntimeDao = null;
	private RuntimeExceptionDao<TaskLocalValues, String> tasklocalsRuntimeDao = null;
	private RuntimeExceptionDao<Subscript, String> subscriptRuntimeDao = null;
	private RuntimeExceptionDao<GPSLocation, String> gpsLocationtRuntimeDao = null;
	private RuntimeExceptionDao<StayPoint, String> stayPointRuntimeDao = null;
	private RuntimeExceptionDao<StayPointHasPlaces, String> stayPointHasPlacesDao = null;
	private RuntimeExceptionDao<TransactionHasPlaces, String> transactionHasPlacesDao = null;
	private RuntimeExceptionDao<Message, String> messageDao = null;
	private RuntimeExceptionDao<MessageParticipants, String> messageParticipantsDao = null;



	Context context;

	public static SQLiteDatabase myDB;

	private static DatabaseHelper instance;

	public static synchronized DatabaseHelper getHelper(Context context)
	{
		if (instance == null)
			instance = new DatabaseHelper(context);
		return instance;
	}


	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
		this.context=context;
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			myDB=db;
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Email.class);
			TableUtils.createTable(connectionSource, EmailTo.class);
			TableUtils.createTable(connectionSource, EmailCc.class);
			TableUtils.createTable(connectionSource, EmailBcc.class);
			TableUtils.createTable(connectionSource, Event.class);
			TableUtils.createTable(connectionSource, Person.class);
			TableUtils.createTable(connectionSource, Album.class);
			TableUtils.createTable(connectionSource, Photo.class);
			TableUtils.createTable(connectionSource, Feed.class);
			TableUtils.createTable(connectionSource, Place.class);
			TableUtils.createTable(connectionSource, EventAttendees.class);
			TableUtils.createTable(connectionSource, PhotoTags.class);
			TableUtils.createTable(connectionSource, FeedWithTags.class);
			TableUtils.createTable(connectionSource, FeedMessageTags.class);
			TableUtils.createTable(connectionSource, Category.class);
			TableUtils.createTable(connectionSource, Transaction.class);
			TableUtils.createTable(connectionSource, TransactionHasCategory.class);
			TableUtils.createTable(connectionSource, PlaceHasCategory.class);
			TableUtils.createTable(connectionSource, Script.class);
            TableUtils.createTable(connectionSource, ScriptDefinition.class);
			TableUtils.createTable(connectionSource, LocalProperties.class);
            TableUtils.createTable(connectionSource, ScriptLocalValues.class);
			TableUtils.createTable(connectionSource, ScriptDefHasLocalProperties.class);
			TableUtils.createTable(connectionSource, TaskDefHasLocalProperties.class);
			TableUtils.createTable(connectionSource, TaskLocalValues.class);
			TableUtils.createTable(connectionSource, Task.class);
            TableUtils.createTable(connectionSource, TaskDefinition.class);
            TableUtils.createTable(connectionSource, ScriptDefHasTaskDef.class);
            TableUtils.createTable(connectionSource, Subscript.class);
			TableUtils.createTable(connectionSource, GPSLocation.class);
			TableUtils.createTable(connectionSource, StayPoint.class);
			TableUtils.createTable(connectionSource, StayPointHasPlaces.class);
			TableUtils.createTable(connectionSource, TransactionHasPlaces.class);
			TableUtils.createTable(connectionSource, Message.class);
			TableUtils.createTable(connectionSource, MessageParticipants.class);


			createIndexes();

		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}

		// here we try inserting data in the on-create as a test
//		RuntimeExceptionDao<Email, Integer> dao = getSimpleDataDao();
//		long millis = System.currentTimeMillis();
//		// create some entries in the onCreate
//		Email message = new Email();
//		dao.create(simple);
//		simple = new SimpleData(millis + 1);
//		dao.create(simple);
//		Log.i(DatabaseHelper.class.getName(), "created new entries in onCreate: " + millis);
	}

	public void createIndexes(){
		getEmailDao().queryRaw("CREATE VIRTUAL TABLE Email_fts USING fts4 ( \"_id\", \"textContent\", \"subject\"  )");
		getEventDao().queryRaw("CREATE VIRTUAL TABLE Event_fts USING fts4 ( \"_id\", \"title\" )");
		getMessageDao().queryRaw("CREATE VIRTUAL TABLE Message_fts USING fts4 ( \"_id\", \"content\" )");

	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Email.class, true);
			TableUtils.dropTable(connectionSource, EmailBcc.class, true);
			TableUtils.dropTable(connectionSource, EmailTo.class, true);
			TableUtils.dropTable(connectionSource, EmailCc.class, true);
            TableUtils.dropTable(connectionSource, Event.class, true);
			TableUtils.dropTable(connectionSource, Person.class, true);
			TableUtils.dropTable(connectionSource, Album.class,true);
			TableUtils.dropTable(connectionSource, Photo.class,true);
			TableUtils.dropTable(connectionSource, Place.class,true);
			TableUtils.dropTable(connectionSource, EventAttendees.class,true);
			TableUtils.dropTable(connectionSource, PhotoTags.class, true);
			TableUtils.dropTable(connectionSource, Category.class, true);
			TableUtils.dropTable(connectionSource, Transaction.class, true);
			TableUtils.dropTable(connectionSource, TransactionHasCategory.class, true);
			TableUtils.dropTable(connectionSource, PlaceHasCategory.class, true);
			TableUtils.dropTable(connectionSource, Script.class, true);
            TableUtils.dropTable(connectionSource, ScriptDefinition.class, true);
            TableUtils.dropTable(connectionSource, ScriptDefHasTaskDef.class, true);
            TableUtils.dropTable(connectionSource, LocalProperties.class, true);
			TableUtils.dropTable(connectionSource, ScriptLocalValues.class, true);
			TableUtils.dropTable(connectionSource, TaskLocalValues.class, true);
			TableUtils.dropTable(connectionSource, Task.class, true);
            TableUtils.dropTable(connectionSource, TaskDefinition.class, true);
            TableUtils.dropTable(connectionSource, Subscript.class, true);
			TableUtils.dropTable(connectionSource, Feed.class, true);
			TableUtils.dropTable(connectionSource, FeedWithTags.class, true);
			TableUtils.dropTable(connectionSource, FeedMessageTags.class, true);
			TableUtils.dropTable(connectionSource, ScriptDefHasLocalProperties.class, true);
			TableUtils.dropTable(connectionSource, TaskDefHasLocalProperties.class, true);
			TableUtils.dropTable(connectionSource, GPSLocation.class, true);
			TableUtils.dropTable(connectionSource, StayPoint.class, true);
			TableUtils.dropTable(connectionSource, StayPointHasPlaces.class, true);
			TableUtils.dropTable(connectionSource, TransactionHasPlaces.class, true);
			TableUtils.dropTable(connectionSource, Message.class, true);
			TableUtils.dropTable(connectionSource, MessageParticipants.class, true);


			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the Database Access Object (DAO) for our Email class. It will create it or just give the cached
	 * value.
	 */
//	public Dao<Email, String> getDao() throws SQLException {
//		if (emailDao == null) {
//			emailDao = getDao(Email.class);
//		}
//		return emailDao;
//	}
//
//    public Dao<Event, String> getDao() throws SQLException {
//        if (calendarEventDao == null) {
//            calendarEventDao = getDao(Event.class);
//        }
//        return calendarEventDao;
//    }

	/**
	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our SimpleData class. It will
	 * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<Email, String> getEmailDao() {
		if (emailRuntimeDao == null) {
			emailRuntimeDao = getRuntimeExceptionDao(Email.class);
		}
		return emailRuntimeDao;
	}

	public RuntimeExceptionDao<EmailTo, String> getEmailToDao() {
		if (emailToRuntimeDao == null) {
			emailToRuntimeDao = getRuntimeExceptionDao(EmailTo.class);
		}
		return emailToRuntimeDao;
	}

	public RuntimeExceptionDao<EmailCc, String> getEmailCcDao() {
		if (emailCcRuntimeDao == null) {
			emailCcRuntimeDao = getRuntimeExceptionDao(EmailCc.class);
		}
		return emailCcRuntimeDao;
	}

	public RuntimeExceptionDao<EmailBcc, String> getEmailBccDao() {
		if (emailBccRuntimeDao == null) {
			emailBccRuntimeDao = getRuntimeExceptionDao(EmailBcc.class);
		}
		return emailBccRuntimeDao;
	}

    public RuntimeExceptionDao<Event, String> getEventDao() {
        if (calEventRuntimeDao == null) {
            calEventRuntimeDao = getRuntimeExceptionDao(Event.class);
        }
        return calEventRuntimeDao;
    }

	public RuntimeExceptionDao<Person, String> getPersonDao() {
		if (personRuntimeDao == null) {
			personRuntimeDao = getRuntimeExceptionDao(Person.class);
		}
		return personRuntimeDao;
	}

	public RuntimeExceptionDao<Place, String> getPlaceDao() {
		if (placeRuntimeDao == null) {
			placeRuntimeDao = getRuntimeExceptionDao(Place.class);
		}
		return placeRuntimeDao;
	}

	public RuntimeExceptionDao<Photo, String> getPhotoDao() {
		if (photoRuntimeDao == null) {
			photoRuntimeDao = getRuntimeExceptionDao(Photo.class);
		}
		return photoRuntimeDao;
	}

	public RuntimeExceptionDao<Feed, String> getFeedDao() {
		if (feedRuntimeDao == null) {
			feedRuntimeDao = getRuntimeExceptionDao(Feed.class);
		}
		return feedRuntimeDao;
	}

	public RuntimeExceptionDao<FeedWithTags, String> getFeedWithTagsDao() {
		if (feedWithTagsRuntimeDao == null) {
			feedWithTagsRuntimeDao = getRuntimeExceptionDao(FeedWithTags.class);
		}
		return feedWithTagsRuntimeDao;
	}

	public RuntimeExceptionDao<FeedMessageTags, String> getFeedMessagesTagsDao() {
		if (feedMessageTagsRuntimeDao == null) {
			feedMessageTagsRuntimeDao = getRuntimeExceptionDao(FeedMessageTags.class);
		}
		return feedMessageTagsRuntimeDao;
	}

	public RuntimeExceptionDao<Album, String> getAlbumDao() {
		if (albumRuntimeDao == null) {
			albumRuntimeDao = getRuntimeExceptionDao(Album.class);
		}
		return albumRuntimeDao;
	}

	public RuntimeExceptionDao<EventAttendees, String> getEventAttendeesDao() {
		if (eventAttendeesRuntimeDao == null) {
			eventAttendeesRuntimeDao = getRuntimeExceptionDao(EventAttendees.class);
		}
		return eventAttendeesRuntimeDao;
	}

	public RuntimeExceptionDao<PhotoTags, String> getPhotoTagsDao() {
		if (photoTagsRuntimeDao == null) {
			photoTagsRuntimeDao = getRuntimeExceptionDao(PhotoTags.class);
		}
		return photoTagsRuntimeDao;
	}

	public RuntimeExceptionDao<Transaction, String> getTransactionDao() {
		if (transactionRuntimeDao == null) {
			transactionRuntimeDao = getRuntimeExceptionDao(Transaction.class);
		}
		return transactionRuntimeDao;
	}

	public RuntimeExceptionDao<Category, String> getCategoryDao() {
		if (categoryRuntimeDao == null) {
			categoryRuntimeDao = getRuntimeExceptionDao(Category.class);
		}
		return categoryRuntimeDao;
	}

	public RuntimeExceptionDao<TransactionHasCategory, String> getTransactionHasCategoryRuntimeDao() {
		if (transactionHasCategoryRuntimeDao == null) {
			transactionHasCategoryRuntimeDao = getRuntimeExceptionDao(TransactionHasCategory.class);
		}
		return transactionHasCategoryRuntimeDao;
	}

	public RuntimeExceptionDao<PlaceHasCategory, String> getPlaceHasCategoryRuntimeDao() {
		if (placeHasCategoryRuntimeDao == null) {
			placeHasCategoryRuntimeDao = getRuntimeExceptionDao(PlaceHasCategory.class);
		}
		return placeHasCategoryRuntimeDao;
	}

	public RuntimeExceptionDao<Script, String> getScriptDao() {
		if (scriptRuntimeDao == null) {
			scriptRuntimeDao = getRuntimeExceptionDao(Script.class);
		}
		return scriptRuntimeDao;
	}

    public RuntimeExceptionDao<Subscript, String> getSubScriptDao() {
        if (subscriptRuntimeDao == null) {
            subscriptRuntimeDao = getRuntimeExceptionDao(Subscript.class);
        }
        return subscriptRuntimeDao;
    }

    public RuntimeExceptionDao<ScriptDefinition, String> getScriptDefDao() {
        if (scriptDefRuntimeDao == null) {
            scriptDefRuntimeDao = getRuntimeExceptionDao(ScriptDefinition.class);
        }
        return scriptDefRuntimeDao;
    }

	public RuntimeExceptionDao<Task, String> getTaskDao() {
		if (taskRuntimeDao == null) {
			taskRuntimeDao = getRuntimeExceptionDao(Task.class);
		}
		return taskRuntimeDao;
	}

    public RuntimeExceptionDao<TaskDefinition, String> getTaskDefinitionDao() {
        if (taskDefRuntimeDao == null) {
            taskDefRuntimeDao = getRuntimeExceptionDao(TaskDefinition.class);
        }
        return taskDefRuntimeDao;
    }

    public RuntimeExceptionDao<ScriptDefHasTaskDef, String> getScriptHasTasksDao() {
        if (scriptDefHasTaskDefRuntimeDao == null) {
            scriptDefHasTaskDefRuntimeDao = getRuntimeExceptionDao(ScriptDefHasTaskDef.class);
        }
        return scriptDefHasTaskDefRuntimeDao;
    }

	public RuntimeExceptionDao<LocalProperties, String> getLocalPropertiesDao() {
		if (localPropertiesRuntimeDao == null) {
            localPropertiesRuntimeDao = getRuntimeExceptionDao(LocalProperties.class);
		}
		return localPropertiesRuntimeDao;
	}

    public RuntimeExceptionDao<ScriptLocalValues, String> getScriptLocalValuesDao() {
        if ( scriptlocalsRuntimeDao == null) {
            scriptlocalsRuntimeDao = getRuntimeExceptionDao(ScriptLocalValues.class);
        }
        return  scriptlocalsRuntimeDao;
    }


	public RuntimeExceptionDao<TaskLocalValues, String> getTaskLocalValuesDao() {
		if ( tasklocalsRuntimeDao == null) {
			tasklocalsRuntimeDao = getRuntimeExceptionDao(TaskLocalValues.class);
		}
		return  tasklocalsRuntimeDao;
	}

	public RuntimeExceptionDao<ScriptDefHasLocalProperties, String> getScriptDefHasLocalPropertiesDao() {
		if ( scriptDefHasLocalPropertiesRuntimeDao == null) {
			scriptDefHasLocalPropertiesRuntimeDao = getRuntimeExceptionDao(ScriptDefHasLocalProperties.class);
		}
		return  scriptDefHasLocalPropertiesRuntimeDao;
	}

	public RuntimeExceptionDao<TaskDefHasLocalProperties, String> getTaskDefHasLocalPropertiesDao() {
		if ( taskDefHasLocalPropertiesRuntimeDao == null) {
			taskDefHasLocalPropertiesRuntimeDao = getRuntimeExceptionDao(TaskDefHasLocalProperties.class);
		}
		return  taskDefHasLocalPropertiesRuntimeDao;
	}

	public RuntimeExceptionDao<GPSLocation, String> getGpsLocationtRuntimeDao() {
		if ( gpsLocationtRuntimeDao == null) {
			gpsLocationtRuntimeDao = getRuntimeExceptionDao(GPSLocation.class);
		}
		return  gpsLocationtRuntimeDao;
	}

	public RuntimeExceptionDao<StayPoint, String> getStayPointRuntimeDao() {
		if ( stayPointRuntimeDao == null) {
			stayPointRuntimeDao = getRuntimeExceptionDao(StayPoint.class);
		}
		return  stayPointRuntimeDao;
	}

	public RuntimeExceptionDao<StayPointHasPlaces, String> getStayPointHasPlacesDao() {
		if ( stayPointHasPlacesDao == null) {
			stayPointHasPlacesDao = getRuntimeExceptionDao(StayPointHasPlaces.class);
		}
		return  stayPointHasPlacesDao;
	}


	public RuntimeExceptionDao<TransactionHasPlaces, String> getTransactionHasPlacesDao() {
		if ( transactionHasPlacesDao == null) {
			transactionHasPlacesDao = getRuntimeExceptionDao(TransactionHasPlaces.class);
		}
		return  transactionHasPlacesDao;
	}

	public RuntimeExceptionDao<Message, String> getMessageDao() {
		if ( messageDao == null) {
			messageDao = getRuntimeExceptionDao(Message.class);
		}
		return  messageDao;
	}

	public RuntimeExceptionDao<MessageParticipants, String> getMessageParticipantsDao() {
		if ( messageParticipantsDao == null) {
			messageParticipantsDao = getRuntimeExceptionDao(MessageParticipants.class);
		}
		return  messageParticipantsDao;
	}



	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		emailRuntimeDao = null;
        calEventRuntimeDao = null;
		personRuntimeDao =null;
		placeRuntimeDao =null;
		albumRuntimeDao =null;
		photoRuntimeDao =null;
		feedRuntimeDao =null;
		feedWithTagsRuntimeDao=null;
		feedMessageTagsRuntimeDao=null;
		photoTagsRuntimeDao=null;
		eventAttendeesRuntimeDao = null;
		placeHasCategoryRuntimeDao =null;
		transactionHasCategoryRuntimeDao =null;
		categoryRuntimeDao = null;
		transactionRuntimeDao = null;
        subscriptRuntimeDao =null;
        gpsLocationtRuntimeDao = null;
		stayPointRuntimeDao = null;
		transactionHasPlacesDao = null;
		messageParticipantsDao = null;
		messageDao = null;
	}



	public Person personExistsById(String id) {

		RuntimeExceptionDao<Person, String> personDao = this.getPersonDao();

		QueryBuilder<Person, String> queryBuilder =
				personDao.queryBuilder();
		Where<Person, String> where = queryBuilder.where();
		try {
			where.eq(Person.FIELD_ID, id);
			List<Person> results = queryBuilder.query();
			if (results.size()!=0){
				return results.get(0);
			}else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public Person personExistsByEmail(String email) {

		RuntimeExceptionDao<Person, String> personDao = this.getPersonDao();

		QueryBuilder<Person, String> queryBuilder =
				personDao.queryBuilder();
		Where<Person, String> where = queryBuilder.where();
		try {
			where.eq(Person.FIELD_EMAIL, email);
			List<Person> results = queryBuilder.query();
			if (results.size()!=0){
				return results.get(0);
			}else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public Person personExistsByName(String name) {

		RuntimeExceptionDao<Person, String> personDao = this.getPersonDao();

		QueryBuilder<Person, String> queryBuilder =
				personDao.queryBuilder();
		Where<Person, String> where = queryBuilder.where();
		try {
			where.eq(Person.FIELD_NAME, name);
			List<Person> results = queryBuilder.query();
			if (results.size()!=0){
				return results.get(0);
			}else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Person personExistsByUsername(String username) {

		RuntimeExceptionDao<Person, String> personDao = this.getPersonDao();

		QueryBuilder<Person, String> queryBuilder =
				personDao.queryBuilder();
		Where<Person, String> where = queryBuilder.where();
		try {
			where.eq(Person.FIELD_USERNAME, username);
			List<Person> results = queryBuilder.query();
			if (results.size()!=0){
				return results.get(0);
			}else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Place placeExistsById(String id) {

		RuntimeExceptionDao<Place, String> placeDao = this.getPlaceDao();

		QueryBuilder<Place, String> queryBuilder =
				placeDao.queryBuilder();
		Where<Place, String> where = queryBuilder.where();
		try {
			where.eq(Place.FIELD_ID, id);
			List<Place> results = queryBuilder.query();
			if (results.size()!=0){
				return results.get(0);
			}else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Place placeExistsByPhone(String phone) {

		RuntimeExceptionDao<Place, String> placeDao = this.getPlaceDao();

		QueryBuilder<Place, String> queryBuilder =
				placeDao.queryBuilder();
		Where<Place, String> where = queryBuilder.where();
		try {
			where.eq(Place.FIELD_PHONE, phone);
			List<Place> results = queryBuilder.query();
			if (results.size()!=0){
				return results.get(0);
			}else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public Place placeExistsByStateCity(String state, String city) {

		RuntimeExceptionDao<Place, String> placeDao = this.getPlaceDao();

		QueryBuilder<Place, String> queryBuilder =
				placeDao.queryBuilder();
		Where<Place, String> where = queryBuilder.where();
		try {
			where.eq(Place.FIELD_STATE, state);
			where.eq(Place.FIELD_CITY, city);
			where.and(2);
			List<Place> results = queryBuilder.query();
			if (results.size()!=0){
				return results.get(0);
			}else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}



	public Place placeExistsByLatLong(double lat, double lon) {

		RuntimeExceptionDao<Place, String> placeDao = this.getPlaceDao();

		QueryBuilder<Place, String> queryBuilder =
				placeDao.queryBuilder();
		Where<Place, String> where = queryBuilder.where();
		try {
			where.eq(Place.FIELD_LAT, lat);
			where.eq(Place.FIELD_LONG, lon);
			where.and(2);
			List<Place> results = queryBuilder.query();
			if (results.size()!=0){
				return results.get(0);
			}else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public Album albumExistsById(String id) {

		RuntimeExceptionDao<Album, String> albumDao = this.getAlbumDao();

		QueryBuilder<Album, String> queryBuilder =
				albumDao.queryBuilder();
		Where<Album, String> where = queryBuilder.where();
		try {
			where.eq(Album.FIELD_ID, id);
			List<Album> results = queryBuilder.query();
			if (results.size()!=0){
				return results.get(0);
			}else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public Event eventExistsById(String id) {

		RuntimeExceptionDao<Event, String> eventDao = this.getEventDao();

		QueryBuilder<Event, String> queryBuilder =
				eventDao.queryBuilder();
		Where<Event, String> where = queryBuilder.where();
		try {
			where.eq(Event.FIELD_ID, id);
			List<Event> results = queryBuilder.query();
			if (results.size()!=0){
				return results.get(0);
			}else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public Category placeCategoryExists(String name) {

		RuntimeExceptionDao<Category, String> placeCategoryDao = getCategoryDao();

		QueryBuilder<Category, String> queryBuilder =
				placeCategoryDao.queryBuilder();
		Where<Category, String> where = queryBuilder.where();
		try {
			where.eq(Category.CATEGORY, name);
			List<Category> results = queryBuilder.query();
			if (results.size() != 0) {
				return results.get(0);
			} else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

    public TaskDefinition taskDefinitionExists(String name) {

        RuntimeExceptionDao<TaskDefinition, String> taskDefDao = getTaskDefinitionDao();

        QueryBuilder<TaskDefinition, String> queryBuilder =
                taskDefDao.queryBuilder();
        Where<TaskDefinition, String> where = queryBuilder.where();
        try {
            where.eq("name", name);
            List<TaskDefinition> results = queryBuilder.query();
            if (results.size() != 0) {
                return results.get(0);
            } else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ScriptDefinition scriptDefinitionExists(String name, String arg) {

        RuntimeExceptionDao<ScriptDefinition, String> scriptDefDao = getScriptDefDao();

        QueryBuilder<ScriptDefinition, String> queryBuilder =
                scriptDefDao.queryBuilder();
        Where<ScriptDefinition, String> where = queryBuilder.where();
        try {
            where.eq("name", name);
			where.eq("ofType", arg);
			where.and(2);
			List<ScriptDefinition> results = queryBuilder.query();
            if (results.size() != 0) {
				ArrayList<LocalProperties> localProps =  getScriptLocals(name);
				ScriptDefinition scriptDef = results.get(0);
				scriptDef.setLocalProperties(localProps);
                return scriptDef;
            } else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


	public ArrayList<LocalProperties> extractTaskLocals(String taskName) throws SQLException {

		RuntimeExceptionDao<LocalProperties, String> localPropertiesDao = getLocalPropertiesDao();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT label_id, w5h_label, w5h_value FROM LocalProperties, TaskDefHasLocalProperties, TaskDefinition where TaskDefinition.name='");
		sb.append(taskName);
		sb.append("' and taskdefinition_id=id and localProperties_id=label_id;");

		GenericRawResults<LocalProperties> rawResults =
				localPropertiesDao.queryRaw(sb.toString(),
						new RawRowMapper<LocalProperties>() {
							public LocalProperties mapRow(String[] columnNames,
											  String[] resultColumns) {
								return new LocalProperties(Integer.parseInt(resultColumns[0]),
										(String)resultColumns[1],(String)resultColumns[2]);
							}
						});

		return (ArrayList<LocalProperties>)rawResults.getResults();

	}

	public ArrayList<LocalProperties> getScriptLocals(String scriptName) throws SQLException {
		RuntimeExceptionDao<LocalProperties, String> localPropertiesDao = getLocalPropertiesDao();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT label_id, w5h_label, w5h_value from LocalProperties, ScriptDefHasLocalProperties, ScriptDefinition where name='");
		sb.append(scriptName);
		sb.append("' and scriptDefinition_id=_id; and localProperties_id=label_id");

		GenericRawResults<LocalProperties> rawResults =
				localPropertiesDao.queryRaw(sb.toString(),
						new RawRowMapper<LocalProperties>() {
							public LocalProperties mapRow(String[] columnNames,
														  String[] resultColumns) {
								return new LocalProperties(Integer.parseInt(resultColumns[0]),
										(String)resultColumns[1],(String)resultColumns[2]);
							}
						});

		return (ArrayList<LocalProperties>)rawResults.getResults();

	}


//	public ScriptDefinition getScriptDefinition(ArrayList<Task> tasks) throws SQLException {
//
//
//		ScriptDefinition scriptDefinition = null;
//		HashSet<String> scriptsSet = new HashSet<String>();
//		for(Task task:tasks) {
//			String scriptName = task.getScript().getName(); //get Task's script
//			String ofType = task.getScript().getOfType();
//			if (scriptDefinition ==null) {
//				scriptDefinition = getTopScripts(scriptName, ofType);
//			}else{
//				if (scriptDefinition.getName()==scriptName){
//					scriptDefinition.addTaskMap(task.getName(),new TaskDefinition(task.getName()));
//				}else{
//					for (ScriptDefinition sd: scriptDefinition.getSubscripts()){
//						if(sd.getName()==scriptName){
//							sd.addTaskMap(task.getName(),new TaskDefinition(task.getName()));
//							break;
//						}
//
//					}
//				}
//			}
//
//
//		}
//
//	}

	public ScriptDefinition getScriptDefinition(String scriptName, String ofType) throws SQLException {
		RuntimeExceptionDao<ScriptDefinition, String> scriptDefinitionDao = getScriptDefDao();


		//Get one level above scripts that have this Task
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct sd._id, sd.name, sd.ofType from ScriptDefinition sd where name='");
		sb.append(scriptName);
		sb.append("' and ofType='");
		sb.append(ofType);
		sb.append("';");

		GenericRawResults<ScriptDefinition> rawResults =
				scriptDefinitionDao.queryRaw(sb.toString(),
						new RawRowMapper<ScriptDefinition>() {
							public ScriptDefinition mapRow(String[] columnNames,
														   String[] resultColumns) {
								ScriptDefinition sd=new ScriptDefinition();
								sd.setId(Integer.parseInt(resultColumns[0]));
								sd.setName((String)resultColumns[1]);
								sd.setOfType((String)resultColumns[2]);
								return sd;
							}
						});

		List<ScriptDefinition> list = rawResults.getResults();
		if(!list.isEmpty()){
			return list.get(0);
		}else{
			return null;
		}

	}

	//updated/last one
	public ScriptDefinition getScriptDefinition(int scriptID, ScriptDefinition sd) throws SQLException {

		RuntimeExceptionDao<ScriptDefinition, String> scriptDefinitionDao = getScriptDefDao();


		//get the Local Properties for this one level up scripts
		StringBuilder sb=new StringBuilder();
		sb.append("select label_id, w5h_label, w5h_value from LocalProperties, ScriptDefHasLocalProperties where localProperties_id=label_id and scriptDefinition_id=");
		sb.append(scriptID);


		GenericRawResults<LocalProperties> localPropertiesForScript =
				scriptDefinitionDao.queryRaw(sb.toString(),
						new RawRowMapper<LocalProperties>() {
							public LocalProperties mapRow(String[] columnNames,
														  String[] resultColumns) {
								LocalProperties lp = new LocalProperties(Integer.parseInt(resultColumns[0]),
										(String) resultColumns[1], (String) resultColumns[2]);
								return lp;
							}
						});

		sd.setLocalProperties((ArrayList<LocalProperties>) localPropertiesForScript.getResults());



		sb = new StringBuilder();
		sb.append("select distinct superscript_id,name,ofType from subscript, ScriptDefinition s where s._id=superscript_id and subscript_id=");
		sb.append(scriptID);


		GenericRawResults<ScriptDefinition> superscript =
				scriptDefinitionDao.queryRaw(sb.toString(),
						new RawRowMapper<ScriptDefinition>() {
							public ScriptDefinition mapRow(String[] columnNames,
														   String[] resultColumns) {
								ScriptDefinition sd = new ScriptDefinition();
								sd.setId(Integer.parseInt(resultColumns[0]));
								sd.setName((String) resultColumns[1]);
								sd.setOfType((String) resultColumns[2]);
								return sd;
							}
						});

		List<ScriptDefinition> list = superscript.getResults();
		if(!list.isEmpty()){
			ScriptDefinition script = list.get(0);
			script.addSubscript(sd);
			return getScriptDefinition(script.getId(), script);
		}else{
			return sd;
		}
	}

//	public ScriptDefinition getTopScripts(String scriptName, String ofType, ArrayList<Task> tasks) throws SQLException {
//		RuntimeExceptionDao<ScriptDefinition, String> scriptDefinitionDao = getScriptDefDao();
//
//
//		//Get one level above scripts that have this Task
//		StringBuilder sb = new StringBuilder();
//		sb.append("select distinct sd._id, sd.name, sd.ofType from ScriptDefinition sd where name='");
//		sb.append(scriptName);
//		sb.append("' and ofType='");
//		sb.append(ofType);
//		sb.append("';");
//
//		GenericRawResults<ScriptDefinition> rawResults =
//				scriptDefinitionDao.queryRaw(sb.toString(),
//						new RawRowMapper<ScriptDefinition>() {
//							public ScriptDefinition mapRow(String[] columnNames,
//														   String[] resultColumns) {
//								ScriptDefinition sd=new ScriptDefinition();
//								sd.setId(Integer.parseInt(resultColumns[0]));
//								sd.setName((String)resultColumns[1]);
//								sd.setOfType((String)resultColumns[2]);
//								return sd;
//							}
//						});
//
//
//		for (ScriptDefinition superscriptDownLevel :(ArrayList<ScriptDefinition>)rawResults.getResults()) {
//
//
//			for (Task task:tasks){
//				superscriptDownLevel.addTaskMap(task.getName(), task.getTaskDefinition() );
//			}
//
//
//			//get the Local Properties for this one level up scripts
//			sb=new StringBuilder();
//			sb.append("select label_id, w5h_label, w5h_value from LocalProperties, ScriptDefHasLocalProperties where localProperties_id=label_id and scriptDefinition_id=");
//			sb.append(superscriptDownLevel.getId());
//
//
//			GenericRawResults<LocalProperties> localPropertiesOneLevelUpScripts =
//					scriptDefinitionDao.queryRaw(sb.toString(),
//							new RawRowMapper<LocalProperties>() {
//								public LocalProperties mapRow(String[] columnNames,
//															  String[] resultColumns) {
//									LocalProperties lp = new LocalProperties(Integer.parseInt(resultColumns[0]),
//											(String) resultColumns[1], (String) resultColumns[2]);
//									return lp;
//								}
//							});
//
//			superscriptDownLevel.setLocalProperties((ArrayList<LocalProperties>) localPropertiesOneLevelUpScripts.getResults());
//			sb = new StringBuilder();
//			sb.append("select distinct superscript_id,name,ofType from subscript, ScriptDefinition s where s._id=superscript_id and subscript_id=");
//			sb.append(superscriptDownLevel.getId());
//
//
//			GenericRawResults<ScriptDefinition> topLevelSuperscripts =
//					scriptDefinitionDao.queryRaw(sb.toString(),
//							new RawRowMapper<ScriptDefinition>() {
//								public ScriptDefinition mapRow(String[] columnNames,
//															   String[] resultColumns) {
//									ScriptDefinition sd = new ScriptDefinition();
//									sd.setId(Integer.parseInt(resultColumns[0]));
//									sd.setName((String) resultColumns[1]);
//									sd.setOfType((String)resultColumns[2]);
//									return sd;
//								}
//							});
//
//			for(ScriptDefinition topLevel :topLevelSuperscripts){
//				sb = new StringBuilder();
//				sb.append("select label_id, w5h_label, w5h_value from LocalProperties, ScriptDefHasLocalProperties where localProperties_id=label_id and scriptDefinition_id=");
//				sb.append(topLevel.getId());
//
//
//				GenericRawResults<LocalProperties> TopLevelLocalDefinitions =
//						scriptDefinitionDao.queryRaw(sb.toString(),
//								new RawRowMapper<LocalProperties>() {
//									public LocalProperties mapRow(String[] columnNames,
//																  String[] resultColumns) {
//										LocalProperties lp = new LocalProperties(Integer.parseInt(resultColumns[0]),
//												(String) resultColumns[1], (String) resultColumns[2]);
//										return lp;
//									}
//								});
//
//				topLevel.setLocalProperties((ArrayList<LocalProperties>) TopLevelLocalDefinitions.getResults());
//				topLevel.addSubscript(superscriptDownLevel);
//				return topLevel;
//			}
//
//
//
//		}
//
//		return null;
//
//
//		//return (ArrayList<ScriptDefinition>)rawResults.getResults();
//
//	}



//	public ScriptDefinition getTopScriptsByTask(String taskName) throws SQLException {
//		RuntimeExceptionDao<ScriptDefinition, String> scriptDefinitionDao = getScriptDefDao();
//
//
//		//Get one level above scripts that have this Task
//		StringBuilder sb = new StringBuilder();
//		sb.append("select distinct sd.id, sd.name from ScriptDefinition sd, TaskDefinition tf, ScriptDefHasTaskDef sht where tf.name='");
//		sb.append(taskName);
//		sb.append("' and sht.task_id=tf.id and sht.script_id=sd.id;");
//
//		GenericRawResults<ScriptDefinition> rawResults =
//				scriptDefinitionDao.queryRaw(sb.toString(),
//						new RawRowMapper<ScriptDefinition>() {
//							public ScriptDefinition mapRow(String[] columnNames,
//														  String[] resultColumns) {
//								ScriptDefinition sd=new ScriptDefinition();
//								sd.setId(Integer.parseInt(resultColumns[0]));
//								sd.setName((String)resultColumns[1]);
//								return sd;
//							}
//						});
//
//
//		for (ScriptDefinition superscriptDownLevel :(ArrayList<ScriptDefinition>)rawResults.getResults()) {
//			//get the Local Properties for this one level up scripts
//			sb=new StringBuilder();
//			sb.append("select label_id, w5h_label, w5h_value from LocalProperties lp where lp.script_id=");
//			sb.append(superscriptDownLevel.getId());
//
//			GenericRawResults<LocalProperties> localPropertiesOneLevelUpScripts =
//					scriptDefinitionDao.queryRaw(sb.toString(),
//							new RawRowMapper<LocalProperties>() {
//								public LocalProperties mapRow(String[] columnNames,
//															   String[] resultColumns) {
//									LocalProperties lp = new LocalProperties(Integer.parseInt(resultColumns[0]),
//											(String) resultColumns[1], (String) resultColumns[2]);
//									return lp;
//								}
//							});
//
//			superscriptDownLevel.setLocalProperties((ArrayList<LocalProperties>) localPropertiesOneLevelUpScripts.getResults());
//			sb = new StringBuilder();
//			sb.append("select distinct superscript_id,name  from subscript, ScriptDefinition s where s.id=superscript_id and  subscript_id=");
//			sb.append(superscriptDownLevel.getId());
//
//
//			GenericRawResults<ScriptDefinition> topLevelSuperscripts =
//					scriptDefinitionDao.queryRaw(sb.toString(),
//							new RawRowMapper<ScriptDefinition>() {
//								public ScriptDefinition mapRow(String[] columnNames,
//															   String[] resultColumns) {
//									ScriptDefinition sd = new ScriptDefinition();
//									sd.setId(Integer.parseInt(resultColumns[0]));
//									sd.setName((String) resultColumns[1]);
//									return sd;
//								}
//							});
//
//			for(ScriptDefinition topLevel :topLevelSuperscripts){
//				sb = new StringBuilder();
//				sb.append("select label_id, w5h_label, w5h_value from LocalProperties where script_id=");
//				sb.append(topLevel.getId());
//
//
//				GenericRawResults<LocalProperties> TopLevelLocalDefinitions =
//						scriptDefinitionDao.queryRaw(sb.toString(),
//								new RawRowMapper<LocalProperties>() {
//									public LocalProperties mapRow(String[] columnNames,
//																   String[] resultColumns) {
//										LocalProperties lp = new LocalProperties(Integer.parseInt(resultColumns[0]),
//												(String) resultColumns[1], (String) resultColumns[2]);
//										return lp;
//									}
//								});
//
//				topLevel.setLocalProperties((ArrayList<LocalProperties>) TopLevelLocalDefinitions.getResults());
//				topLevel.addSubscript(superscriptDownLevel);
//				return topLevel;
//			}
//
//
//
//		}
//
//		return null;
//
//
//		//return (ArrayList<ScriptDefinition>)rawResults.getResults();
//
//	}


	public Category categoryExists(String name) {

		QueryBuilder<Category, String> queryBuilder =
				this.categoryRuntimeDao.queryBuilder();
		Where<Category, String> where = queryBuilder.where();
		try {
			where.eq(Category.CATEGORY, name);
			List<Category> results = queryBuilder.query();
			if (results.size() != 0) {
				return results.get(0);
			} else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	public ArrayList<Person> getFeedWithTags(int feed_id) throws SQLException {
		RuntimeExceptionDao<Person, String> personDao = getPersonDao();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Person.name,Person.email,Person.username,Person.isSelf  from Person, FeedWithTags, Feed where Feed._id=");
		sb.append(feed_id);
		sb.append(" and Feed._id=feed_id and tagged_id=Person._id;");

		GenericRawResults<Person> rawResults =
				personDao.queryRaw(sb.toString(),
						new RawRowMapper<Person>() {
							public Person mapRow(String[] columnNames,
												 String[] resultColumns) {
								return new Person((String) resultColumns[0], (String) resultColumns[1], (String) resultColumns[2], Boolean.parseBoolean(resultColumns[3]));
							}
						});

		return (ArrayList<Person>) rawResults.getResults();

	}



	public ArrayList<Person> getPhotoWithTags(int photo_id) throws SQLException {
		RuntimeExceptionDao<Person, String> personDao = getPersonDao();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Person.name,Person.email,Person.username,Person.isSelf  from Person, PhotoTags, Photo where Photo._id=");
		sb.append(photo_id);
		sb.append(" and Photo._id=photo_id and tagged_id=Person._id;");

		GenericRawResults<Person> rawResults =
				personDao.queryRaw(sb.toString(),
						new RawRowMapper<Person>() {
							public Person mapRow(String[] columnNames,
												 String[] resultColumns) {
								return new Person((String)resultColumns[0],(String)resultColumns[1],(String)resultColumns[2],Boolean.parseBoolean(resultColumns[3]));
							}
						});

		return (ArrayList<Person>)rawResults.getResults();

	}


	public Message getToMessage(Message msg) throws SQLException {
		String tempQuery = "select Person.name, Person.email from Person, MessageParticipants where Person._id=participant_id and thread_id='" +msg.getThread()+"'";
		GenericRawResults<Person> to =
				getEmailDao().queryRaw(tempQuery,
						new RawRowMapper<Person>() {
							public Person mapRow(String[] columnNames,
												 String[] resultColumns) {
								return new Person((String)resultColumns[0],(String)resultColumns[1]);
							}
						});
		msg.setTo((ArrayList<Person>)to.getResults());

		return msg;

	}

	public Email getToCcBcc(Email email) throws SQLException {
		String tempQuery = "select Person.name, Person.email from Person, EmailTo where Person._id=to_id and email_id=" +email.get_id();
		GenericRawResults<Person> to =
				getEmailDao().queryRaw(tempQuery,
						new RawRowMapper<Person>() {
							public Person mapRow(String[] columnNames,
												 String[] resultColumns) {
								return new Person((String)resultColumns[0],(String)resultColumns[1]);
							}
						});
		email.setTo((ArrayList<Person>)to.getResults());

		tempQuery = "select Person.name, Person.email from Person, EmailBcc where Person._id=bcc_id and email_id=" +email.get_id();
		GenericRawResults<Person> bcc =
				getEmailDao().queryRaw(tempQuery,
						new RawRowMapper<Person>() {
							public Person mapRow(String[] columnNames,
												 String[] resultColumns) {
								return new Person((String)resultColumns[0],(String)resultColumns[1]);
							}
						});
		email.setBcc((ArrayList<Person>)bcc.getResults());

		tempQuery = "select Person.name, Person.email from Person, EmailCc where Person._id=cc_id and email_id=" +email.get_id();
		GenericRawResults<Person> cc =
				getEmailDao().queryRaw(tempQuery,
						new RawRowMapper<Person>() {
							public Person mapRow(String[] columnNames,
												 String[] resultColumns) {
								return new Person((String)resultColumns[0],(String)resultColumns[1]);
							}
						});
		email.setCc((ArrayList<Person>)cc.getResults());

		return email;

	}



	public ArrayList<GPSLocation> getGPSLocations() throws SQLException {

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT timestamp, latitude, longitude  from gpslocation order by timestamp asc");

		GenericRawResults<GPSLocation> rawResults =
				gpsLocationtRuntimeDao.queryRaw(sb.toString(),
						new RawRowMapper<GPSLocation>() {
							public GPSLocation mapRow(String[] columnNames,
												 String[] resultColumns) {
								return new GPSLocation(Long.parseLong(resultColumns[0]),Double.parseDouble(resultColumns[1]), Double.parseDouble(resultColumns[2]));
							}
						});

		return (ArrayList<GPSLocation>) rawResults.getResults();

	}


	public void confirmPlace(int place_id) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from Place where _id IN (select p.place_id from StayPointHasPlaces p where p.StayPoint_id = (select  stayPoint_id from StayPointHasPlaces where place_id="+ place_id +") and not exists (select * from Place where p.place_id="+ place_id +"))");

		placeRuntimeDao.queryRaw(sb.toString());

	}


	public void deletePlaces(int place_id) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from Place where _id IN (select p.place_id from StayPointHasPlaces p where p.StayPoint_id = (select  stayPoint_id from StayPointHasPlaces where place_id="+ place_id +"))");

		placeRuntimeDao.queryRaw(sb.toString());

	}

	public ArrayList<Place> getOfficialNameOfTranscationPlace(int id) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("select Place._id, Place.name from TransactionHasPlaces, Place where TransactionHasPlaces.transaction_id="+id+" and Place._id=TransactionHasPlaces.place_id");

		GenericRawResults<Place> rawResults =
				placeRuntimeDao.queryRaw(sb.toString(),
						new RawRowMapper<Place>() {
							public Place mapRow(String[] columnNames,
													  String[] resultColumns) {
								return new Place(Integer.parseInt(resultColumns[0]),resultColumns[1]);
							}
						});

		return (ArrayList<Place>) rawResults.getResults();


	}

	public Place getPlace(int _id) throws SQLException {

		RuntimeExceptionDao<Place, String> placeDao = this.getPlaceDao();

		QueryBuilder<Place, String> queryBuilder =
				placeDao.queryBuilder();
		Where<Place, String> where = queryBuilder.where();
		try {
			where.eq(Place.FIELD_AutoID, _id);
			ArrayList<Place> results = (ArrayList<Place>) queryBuilder.query();
			if (results.size() != 0) {
				return results.get(0);
			} else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}



}

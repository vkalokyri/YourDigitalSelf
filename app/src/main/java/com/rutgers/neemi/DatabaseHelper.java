package com.rutgers.neemi;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.rutgers.neemi.model.Album;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.EventAttendees;
import com.rutgers.neemi.model.PaymentCategory;
import com.rutgers.neemi.model.Payment;
import com.rutgers.neemi.model.PaymentHasCategory;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.model.Place;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "neemi.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;

	// the DAO object we use to access the Email table
	private RuntimeExceptionDao<Email, String> emailRuntimeDao = null;
    private RuntimeExceptionDao<Event, String> calEventRuntimeDao = null;
	private RuntimeExceptionDao<Person, String> personRuntimeDao = null;
	private RuntimeExceptionDao<Photo, String> photoRuntimeDao = null;
	private RuntimeExceptionDao<Album, String> albumRuntimeDao = null;
	private RuntimeExceptionDao<Place, String> placeRuntimeDao = null;
	private RuntimeExceptionDao<EventAttendees, String> eventAttendeesRuntimeDao = null;
	private RuntimeExceptionDao<PhotoTags, String> photoTagsRuntimeDao = null;
	private RuntimeExceptionDao<Payment, String> paymentRuntimeDao = null;
	private RuntimeExceptionDao<PaymentCategory, String> categoryRuntimeDao = null;
	private RuntimeExceptionDao<PaymentHasCategory, String> transactionCategoriesRuntimeDao = null;



	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Email.class);
            TableUtils.createTable(connectionSource, Event.class);
			TableUtils.createTable(connectionSource, Person.class);
			TableUtils.createTable(connectionSource, Album.class);
			TableUtils.createTable(connectionSource, Photo.class);
			TableUtils.createTable(connectionSource, Place.class);
			TableUtils.createTable(connectionSource, EventAttendees.class);
			TableUtils.createTable(connectionSource, PhotoTags.class);
			TableUtils.createTable(connectionSource, PaymentCategory.class);
			TableUtils.createTable(connectionSource, Payment.class);
			TableUtils.createTable(connectionSource, PaymentHasCategory.class);
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

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Email.class, true);
            TableUtils.dropTable(connectionSource, Event.class, true);
			TableUtils.dropTable(connectionSource, Person.class, true);
			TableUtils.dropTable(connectionSource, Album.class,true);
			TableUtils.dropTable(connectionSource, Photo.class,true);
			TableUtils.dropTable(connectionSource, Place.class,true);
			TableUtils.dropTable(connectionSource, EventAttendees.class,true);
			TableUtils.dropTable(connectionSource, PhotoTags.class, true);
			TableUtils.dropTable(connectionSource, PaymentCategory.class, true);
			TableUtils.dropTable(connectionSource, Payment.class, true);
			TableUtils.dropTable(connectionSource, PaymentHasCategory.class, true);
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

	public RuntimeExceptionDao<Payment, String> getPaymentDao() {
		if (paymentRuntimeDao == null) {
			paymentRuntimeDao = getRuntimeExceptionDao(Payment.class);
		}
		return paymentRuntimeDao;
	}

	public RuntimeExceptionDao<PaymentCategory, String> getCategoryDao() {
		if (categoryRuntimeDao == null) {
			categoryRuntimeDao = getRuntimeExceptionDao(PaymentCategory.class);
		}
		return categoryRuntimeDao;
	}

	public RuntimeExceptionDao<PaymentHasCategory, String> getTransactionCategoriesDao() {
		if (transactionCategoriesRuntimeDao == null) {
			transactionCategoriesRuntimeDao = getRuntimeExceptionDao(PaymentHasCategory.class);
		}
		return transactionCategoriesRuntimeDao;
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
		photoTagsRuntimeDao=null;
		eventAttendeesRuntimeDao = null;
		transactionCategoriesRuntimeDao =null;
		categoryRuntimeDao = null;
		paymentRuntimeDao = null;

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



}

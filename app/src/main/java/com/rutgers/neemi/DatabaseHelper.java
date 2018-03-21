package com.rutgers.neemi;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
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
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.EventAttendees;
import com.rutgers.neemi.model.LocalProperties;
import com.rutgers.neemi.model.LocalValues;
import com.rutgers.neemi.model.PaymentHasCategory;
import com.rutgers.neemi.model.Payment;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Photo;
import com.rutgers.neemi.model.PhotoTags;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.PlaceHasCategory;
import com.rutgers.neemi.model.Script;
import com.rutgers.neemi.model.ScriptDefinition;
import com.rutgers.neemi.model.ScriptHasTasks;
import com.rutgers.neemi.model.Subscript;
import com.rutgers.neemi.model.Task;
import com.rutgers.neemi.model.TaskDefinition;
import com.rutgers.neemi.util.ApplicationManager;

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
    private RuntimeExceptionDao<Event, String> calEventRuntimeDao = null;
	private RuntimeExceptionDao<Person, String> personRuntimeDao = null;
	private RuntimeExceptionDao<Photo, String> photoRuntimeDao = null;
	private RuntimeExceptionDao<Album, String> albumRuntimeDao = null;
	private RuntimeExceptionDao<Place, String> placeRuntimeDao = null;
	private RuntimeExceptionDao<EventAttendees, String> eventAttendeesRuntimeDao = null;
	private RuntimeExceptionDao<PhotoTags, String> photoTagsRuntimeDao = null;
	private RuntimeExceptionDao<Payment, String> paymentRuntimeDao = null;
	private RuntimeExceptionDao<Category, String> categoryRuntimeDao = null;
	private RuntimeExceptionDao<PlaceHasCategory, String> placeHasCategoryRuntimeDao = null;
	private RuntimeExceptionDao<PaymentHasCategory, String> paymentHasCategoryRuntimeDao = null;
	private RuntimeExceptionDao<Script, String> scriptRuntimeDao = null;
    private RuntimeExceptionDao<ScriptDefinition, String> scriptDefRuntimeDao = null;
    private RuntimeExceptionDao<ScriptHasTasks, String> scriptDefHasTaskDefRuntimeDao = null;
    private RuntimeExceptionDao<Task, String> taskRuntimeDao = null;
    private RuntimeExceptionDao<TaskDefinition, String> taskDefRuntimeDao = null;
	private RuntimeExceptionDao<LocalProperties, String> localPropertiesRuntimeDao = null;
    private RuntimeExceptionDao<LocalValues, String> localsRuntimeDao = null;
    private RuntimeExceptionDao<Subscript, String> subscriptRuntimeDao = null;
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
            TableUtils.createTable(connectionSource, Event.class);
			TableUtils.createTable(connectionSource, Person.class);
			TableUtils.createTable(connectionSource, Album.class);
			TableUtils.createTable(connectionSource, Photo.class);
			TableUtils.createTable(connectionSource, Place.class);
			TableUtils.createTable(connectionSource, EventAttendees.class);
			TableUtils.createTable(connectionSource, PhotoTags.class);
			TableUtils.createTable(connectionSource, Category.class);
			TableUtils.createTable(connectionSource, Payment.class);
			TableUtils.createTable(connectionSource, PaymentHasCategory.class);
			TableUtils.createTable(connectionSource, PlaceHasCategory.class);
			TableUtils.createTable(connectionSource, Script.class);
            TableUtils.createTable(connectionSource, ScriptDefinition.class);
			TableUtils.createTable(connectionSource, LocalProperties.class);
            TableUtils.createTable(connectionSource, LocalValues.class);
            TableUtils.createTable(connectionSource, Task.class);
            TableUtils.createTable(connectionSource, TaskDefinition.class);
            TableUtils.createTable(connectionSource, ScriptHasTasks.class);
            TableUtils.createTable(connectionSource, Subscript.class);

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
		getEventDao().queryRaw("CREATE VIRTUAL TABLE Event_fts USING fts4 ( \"_id\", \"description\" )");
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
			TableUtils.dropTable(connectionSource, Category.class, true);
			TableUtils.dropTable(connectionSource, Payment.class, true);
			TableUtils.dropTable(connectionSource, PaymentHasCategory.class, true);
			TableUtils.dropTable(connectionSource, PlaceHasCategory.class, true);
			TableUtils.dropTable(connectionSource, Script.class, true);
            TableUtils.dropTable(connectionSource, ScriptDefinition.class, true);
            TableUtils.dropTable(connectionSource, ScriptHasTasks.class, true);
            TableUtils.dropTable(connectionSource, LocalProperties.class, true);
			TableUtils.dropTable(connectionSource, Task.class, true);
            TableUtils.dropTable(connectionSource, TaskDefinition.class, true);
            TableUtils.dropTable(connectionSource, Subscript.class, true);
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

	public RuntimeExceptionDao<Category, String> getCategoryDao() {
		if (categoryRuntimeDao == null) {
			categoryRuntimeDao = getRuntimeExceptionDao(Category.class);
		}
		return categoryRuntimeDao;
	}

	public RuntimeExceptionDao<PaymentHasCategory, String> getPaymentHasCategoryRuntimeDao() {
		if (paymentHasCategoryRuntimeDao == null) {
			paymentHasCategoryRuntimeDao = getRuntimeExceptionDao(PaymentHasCategory.class);
		}
		return paymentHasCategoryRuntimeDao;
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

    public RuntimeExceptionDao<ScriptHasTasks, String> getScriptHasTasksDao() {
        if (scriptDefHasTaskDefRuntimeDao == null) {
            scriptDefHasTaskDefRuntimeDao = getRuntimeExceptionDao(ScriptHasTasks.class);
        }
        return scriptDefHasTaskDefRuntimeDao;
    }

	public RuntimeExceptionDao<LocalProperties, String> getLocalPropertiesDao() {
		if (localPropertiesRuntimeDao == null) {
            localPropertiesRuntimeDao = getRuntimeExceptionDao(LocalProperties.class);
		}
		return localPropertiesRuntimeDao;
	}

    public RuntimeExceptionDao<LocalValues, String> getLocalValuesDao() {
        if ( localsRuntimeDao == null) {
            localsRuntimeDao = getRuntimeExceptionDao(LocalValues.class);
        }
        return  localsRuntimeDao;
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
		placeHasCategoryRuntimeDao =null;
		paymentHasCategoryRuntimeDao =null;
		categoryRuntimeDao = null;
		paymentRuntimeDao = null;
        subscriptRuntimeDao =null;
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
			where.eq("argument", arg);
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
		sb.append("SELECT label_id, w5h_label, w5h_value FROM LocalProperties, TaskDefinition where TaskDefinition.name='");
		sb.append(taskName);
		sb.append("'and TaskDefinition.id=task_id;");

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
		sb.append("SELECT label_id, w5h_label, w5h_value from LocalProperties, ScriptDefinition where name='");
		sb.append(scriptName);
		sb.append("' and id=script_id;");

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


	public ScriptDefinition getTopScripts(String scriptName, String ofType) throws SQLException {
		RuntimeExceptionDao<ScriptDefinition, String> scriptDefinitionDao = getScriptDefDao();


		//Get one level above scripts that have this Task
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct sd.id, sd.name from ScriptDefinition sd where name='");
		sb.append(scriptName);
		sb.append("' and argument='");
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
								return sd;
							}
						});


		for (ScriptDefinition superscriptDownLevel :(ArrayList<ScriptDefinition>)rawResults.getResults()) {
			//get the Local Properties for this one level up scripts
			sb=new StringBuilder();
			sb.append("select label_id, w5h_label, w5h_value from LocalProperties lp where lp.script_id=");
			sb.append(superscriptDownLevel.getId());

			GenericRawResults<LocalProperties> localPropertiesOneLevelUpScripts =
					scriptDefinitionDao.queryRaw(sb.toString(),
							new RawRowMapper<LocalProperties>() {
								public LocalProperties mapRow(String[] columnNames,
															  String[] resultColumns) {
									LocalProperties lp = new LocalProperties(Integer.parseInt(resultColumns[0]),
											(String) resultColumns[1], (String) resultColumns[2]);
									return lp;
								}
							});

			superscriptDownLevel.setLocalProperties((ArrayList<LocalProperties>) localPropertiesOneLevelUpScripts.getResults());
			sb = new StringBuilder();
			sb.append("select distinct superscript_id,name from subscript, ScriptDefinition s where s.id=superscript_id and subscript_id=");
			sb.append(superscriptDownLevel.getId());


			GenericRawResults<ScriptDefinition> topLevelSuperscripts =
					scriptDefinitionDao.queryRaw(sb.toString(),
							new RawRowMapper<ScriptDefinition>() {
								public ScriptDefinition mapRow(String[] columnNames,
															   String[] resultColumns) {
									ScriptDefinition sd = new ScriptDefinition();
									sd.setId(Integer.parseInt(resultColumns[0]));
									sd.setName((String) resultColumns[1]);
									return sd;
								}
							});

			for(ScriptDefinition topLevel :topLevelSuperscripts){
				sb = new StringBuilder();
				sb.append("select label_id, w5h_label, w5h_value from LocalProperties where script_id=");
				sb.append(topLevel.getId());


				GenericRawResults<LocalProperties> TopLevelLocalDefinitions =
						scriptDefinitionDao.queryRaw(sb.toString(),
								new RawRowMapper<LocalProperties>() {
									public LocalProperties mapRow(String[] columnNames,
																  String[] resultColumns) {
										LocalProperties lp = new LocalProperties(Integer.parseInt(resultColumns[0]),
												(String) resultColumns[1], (String) resultColumns[2]);
										return lp;
									}
								});

				topLevel.setLocalProperties((ArrayList<LocalProperties>) TopLevelLocalDefinitions.getResults());
				topLevel.addSubscript(superscriptDownLevel);
				return topLevel;
			}



		}

		return null;


		//return (ArrayList<ScriptDefinition>)rawResults.getResults();

	}



	public ScriptDefinition getTopScriptsByTask(String taskName) throws SQLException {
		RuntimeExceptionDao<ScriptDefinition, String> scriptDefinitionDao = getScriptDefDao();


		//Get one level above scripts that have this Task
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct sd.id, sd.name from ScriptDefinition sd, TaskDefinition tf, ScriptHasTasks sht where tf.name='");
		sb.append(taskName);
		sb.append("' and sht.task_id=tf.id and sht.script_id=sd.id;");

		GenericRawResults<ScriptDefinition> rawResults =
				scriptDefinitionDao.queryRaw(sb.toString(),
						new RawRowMapper<ScriptDefinition>() {
							public ScriptDefinition mapRow(String[] columnNames,
														  String[] resultColumns) {
								ScriptDefinition sd=new ScriptDefinition();
								sd.setId(Integer.parseInt(resultColumns[0]));
								sd.setName((String)resultColumns[1]);
								return sd;
							}
						});


		for (ScriptDefinition superscriptDownLevel :(ArrayList<ScriptDefinition>)rawResults.getResults()) {
			//get the Local Properties for this one level up scripts
			sb=new StringBuilder();
			sb.append("select label_id, w5h_label, w5h_value from LocalProperties lp where lp.script_id=");
			sb.append(superscriptDownLevel.getId());

			GenericRawResults<LocalProperties> localPropertiesOneLevelUpScripts =
					scriptDefinitionDao.queryRaw(sb.toString(),
							new RawRowMapper<LocalProperties>() {
								public LocalProperties mapRow(String[] columnNames,
															   String[] resultColumns) {
									LocalProperties lp = new LocalProperties(Integer.parseInt(resultColumns[0]),
											(String) resultColumns[1], (String) resultColumns[2]);
									return lp;
								}
							});

			superscriptDownLevel.setLocalProperties((ArrayList<LocalProperties>) localPropertiesOneLevelUpScripts.getResults());
			sb = new StringBuilder();
			sb.append("select distinct superscript_id,name  from subscript, ScriptDefinition s where s.id=superscript_id and  subscript_id=");
			sb.append(superscriptDownLevel.getId());


			GenericRawResults<ScriptDefinition> topLevelSuperscripts =
					scriptDefinitionDao.queryRaw(sb.toString(),
							new RawRowMapper<ScriptDefinition>() {
								public ScriptDefinition mapRow(String[] columnNames,
															   String[] resultColumns) {
									ScriptDefinition sd = new ScriptDefinition();
									sd.setId(Integer.parseInt(resultColumns[0]));
									sd.setName((String) resultColumns[1]);
									return sd;
								}
							});

			for(ScriptDefinition topLevel :topLevelSuperscripts){
				sb = new StringBuilder();
				sb.append("select label_id, w5h_label, w5h_value from LocalProperties where script_id=");
				sb.append(topLevel.getId());


				GenericRawResults<LocalProperties> TopLevelLocalDefinitions =
						scriptDefinitionDao.queryRaw(sb.toString(),
								new RawRowMapper<LocalProperties>() {
									public LocalProperties mapRow(String[] columnNames,
																   String[] resultColumns) {
										LocalProperties lp = new LocalProperties(Integer.parseInt(resultColumns[0]),
												(String) resultColumns[1], (String) resultColumns[2]);
										return lp;
									}
								});

				topLevel.setLocalProperties((ArrayList<LocalProperties>) TopLevelLocalDefinitions.getResults());
				topLevel.addSubscript(superscriptDownLevel);
				return topLevel;
			}



		}

		return null;


		//return (ArrayList<ScriptDefinition>)rawResults.getResults();

	}


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


}

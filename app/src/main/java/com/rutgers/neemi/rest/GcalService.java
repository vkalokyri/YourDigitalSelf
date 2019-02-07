package com.rutgers.neemi.rest;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.DatabaseHelper;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.EventAttendees;
import com.rutgers.neemi.model.Person;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.rutgers.neemi.GmailActivity.PREF_ACCOUNT_NAME;
import static com.rutgers.neemi.GmailActivity.mCredential;

public class GcalService {

    DatabaseHelper dbHelper;
    com.google.api.services.calendar.Calendar calendarService = null;
    private Exception mLastError = null;


    public GcalService(Context context){
        dbHelper= DatabaseHelper.getHelper(context);


        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        String accountName = context.getSharedPreferences( "credentials", Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccount(new Account(accountName, "com.rutgers.neemi"));
            mCredential.setSelectedAccountName(accountName);

            calendarService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("Google Calendar API Android")
                    .build();
        }else{
            Log.e("ERROR IN dataSyncJob", "not Authorized");
        }
        try {
            getDataFromApi();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * Fetch a list of the next 10 events from the primary calendar.
     *
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    private int getDataFromApi() throws IOException {



//            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
//            ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
//            try {
//                TableUtils.dropTable(connectionSource, Event.class,false);
//               // TableUtils.dropTable(connectionSource, Person.class,false);
//
//            } catch (SQLException e) {
//                Log.e("GcalActivity","Error when dropping tables in DB");
//                e.printStackTrace();
//            }
        //TableUtils.createTable(connectionSource, Person.class);
        //TableUtils.createTable(connectionSource, Event.class);

        RuntimeExceptionDao<Event, String> calendarDao = dbHelper.getEventDao();
        RuntimeExceptionDao<Person, String> personDao = dbHelper.getPersonDao();
        RuntimeExceptionDao<EventAttendees, String> eventAttendeesDao = dbHelper.getEventAttendeesDao();


        String user = "me";
        int totalItemsInserted=0;
        String pageToken = null;
        Calendar cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());
        DateTime now = new DateTime(cal.getTimeInMillis());
        cal.add(Calendar.DATE, -1); // substract 6 months
        DateTime since=new DateTime(cal.getTimeInMillis());
        System.out.println("since = "+since);
        String timestamp = null;

        GenericRawResults<String[]> rawResults = calendarDao.queryRaw("select max(timestamp) from Event;");
        List<String[]> results = null;
        try {
            results = rawResults.getResults();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (results!=null){
            String[] resultArray = results.get(0);
            System.out.println("timestamp= " + resultArray[0]);
            timestamp=resultArray[0];
        }


        if (timestamp!=null) {
            cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());
            cal.setTimeInMillis(Long.parseLong(timestamp)*1000);
            since = new DateTime(cal.getTimeInMillis());
        }

        System.out.println("Since="+since);
        System.out.println("Now"+now);


        do {
            Events events = calendarService.events().list("primary")
                    .setPageToken(pageToken)
                    .setOrderBy("startTime")
                    .setTimeMin(since)
                    .setTimeMax(now)
                    .setSingleEvents(true)
                    .execute();

            pageToken = events.getNextPageToken();

            if (events!=null){
                List<com.google.api.services.calendar.model.Event> items = events.getItems();

                for (com.google.api.services.calendar.model.Event gcalevent : items) {
                    try {
                        Event event = new Event();
                        //calendarDao.assignEmptyForeignCollection(event, "attendees");
                        // this would add it the collection and the internal DAO
                        if(gcalevent.getDescription()!=null){
                            System.out.println(gcalevent.getDescription());
                            event.setDescription(gcalevent.getDescription());
                        }
                        if(gcalevent.getSummary()!=null){
                            event.setTitle(gcalevent.getSummary());
                        }
                        if(gcalevent.getCreated()!=null) {
                            event.setDateCreated(gcalevent.getCreated().getValue());
                        }
                        if(gcalevent.getCreator()!=null) {
                            Person person = dbHelper.personExistsByEmail(gcalevent.getCreator().getEmail());
                            if (person ==null) {
                                Person newPerson = new Person(gcalevent.getCreator().getDisplayName(), gcalevent.getCreator().getEmail(),null,gcalevent.getCreator().isSelf());
                                personDao.create(newPerson);
                                event.setCreator(newPerson);
                            }else{
                                event.setCreator(person);
                            }
                        }
                        DateTime end = gcalevent.getEnd().getDateTime();
                        if(end==null){
                            event.setEndTime(gcalevent.getEnd().getDate().getValue());
                        }else{
                            event.setEndTime(end.getValue());
                        }
                        if(gcalevent.getLocation()!=null){
                            event.setLocation(gcalevent.getLocation());
                        }
                        event.setId(gcalevent.getId());
                        if(gcalevent.getOrganizer()!=null){
                            Person person = dbHelper.personExistsByEmail(gcalevent.getOrganizer().getEmail());
                            if (person ==null) {
                                Person newPerson = new Person(gcalevent.getOrganizer().getDisplayName(), gcalevent.getOrganizer().getEmail(),null, gcalevent.getCreator().isSelf());
                                personDao.create(newPerson);
                                event.setOrganizer(newPerson);
                            }else{
                                event.setOrganizer(person);
                            }
                        }
                        DateTime start = gcalevent.getStart().getDateTime();
                        if(start==null){
                            event.setStartTime(gcalevent.getStart().getDate().getValue());
                        }else{
                            event.setStartTime(start.getValue());
                        }
                        if(gcalevent.getSource()!=null) {
                            event.setSource(gcalevent.getSource().getUrl());
                        }
                        List<Person> attendeesList = new ArrayList<Person>();
                        if (gcalevent.getAttendees()!=null){
                            for (EventAttendee attendee:gcalevent.getAttendees()){
                                Person person = dbHelper.personExistsByEmail(attendee.getEmail());
                                if (person ==null) {
                                    Person newPerson = new Person(attendee.getDisplayName(), attendee.getEmail(),null, attendee.isSelf());
                                    personDao.create(newPerson);
                                    attendeesList.add(newPerson);
                                }else{
                                    attendeesList.add(person);

                                }
                            }
                        }
                        event.setTimestamp(System.currentTimeMillis() / 1000);
                        calendarDao.create(event);
                        for(Person attendee:attendeesList) {
                            EventAttendees attendees = new EventAttendees(attendee, event);
                            eventAttendeesDao.create(attendees);
                        }
                        totalItemsInserted++;
                        System.out.println("Gcal inserted = " + totalItemsInserted);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }while(pageToken != null);

        System.out.println("Gcal inserted = " + totalItemsInserted);
        loadEventIndex(totalItemsInserted);


        return totalItemsInserted;
    }

    private void loadEventIndex(final int totalItemsInserted) {

        try {
            GenericRawResults<String[]> rawResults = dbHelper.getEventDao().queryRaw("select * from Event_fts limit 1");
            if (rawResults.getResults().size()==0){
                dbHelper.getEventDao().queryRaw("INSERT INTO Event_fts SELECT \"_id\", \"title\" from Event");
            }else{
                dbHelper.getEventDao().queryRaw("INSERT INTO Event_fts SELECT \"_id\", \"title\" from Event order by \"_id\" desc limit "+totalItemsInserted);
            }
            GenericRawResults<String[]> vrResults =dbHelper.getEventDao().queryRaw("SELECT * FROM Event_fts;");
            System.err.println("VIRTUAL TABLE ADDED = "+vrResults.getResults().size());

        }catch (SQLException e){
            dbHelper.getEventDao().queryRaw("DROP TABLE IF EXISTS Event_fts ");
            dbHelper.getEventDao().queryRaw("CREATE VIRTUAL TABLE Event_fts USING fts4 ( \"_id\", \"title\" )");
            dbHelper.getEventDao().queryRaw("INSERT INTO Event_fts SELECT \"_id\", \"title\" from Event");
        }
    }


}

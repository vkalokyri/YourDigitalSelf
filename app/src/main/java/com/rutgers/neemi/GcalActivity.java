package com.rutgers.neemi;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.rutgers.neemi.model.Event;
import com.rutgers.neemi.model.EventAttendees;
import com.rutgers.neemi.model.Person;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class GcalActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    GoogleAccountCredential mCredential;
    private SignInButton gcalButton;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    DatabaseHelper helper;


    public void deleteDB(){

        this.deleteDatabase("neemi.db"); // specified in DatabaseHelper class in the DATABASE_NAME field
        OpenHelperManager.releaseHelper();
        OpenHelperManager.setHelper(new DatabaseHelper(this));


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcal2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);



        //fa = super.getActivity();
        //view = inflater.inflate(R.layout.activity_gcal,container,false);


        //deleteDB();
        //setContentView(R.layout.activity_gcal);


        helper=DatabaseHelper.getHelper(this);


        //Google widgets
        gcalButton = (SignInButton) findViewById(R.id.gcalApiButton);
        gcalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gcalButton.setEnabled(false);
                getResultsFromApi();
                gcalButton.setEnabled(true);
            }
        });


        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Getting your calendar events. Please wait ...");

        mCredential = GoogleAccountCredential.usingOAuth2(
                this, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Snackbar.make(findViewById(R.id.gcalCoordinatorLayout), "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Snackbar.LENGTH_SHORT ).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                               getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }

    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Snackbar.make(findViewById(R.id.gcalCoordinatorLayout), "No network connection available", Snackbar.LENGTH_SHORT ).show();
        } else {
            //new GetContacts().execute();
            new MakeRequestTask(mCredential).execute();
        }
    }


    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }


    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }


    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Integer> {



        private com.google.api.services.calendar.Calendar calendarService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            calendarService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android")
                    .build();
        }


        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected Integer doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;

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

            RuntimeExceptionDao<Event, String> calendarDao = helper.getEventDao();
            RuntimeExceptionDao<Person, String> personDao = helper.getPersonDao();
            RuntimeExceptionDao<EventAttendees, String> eventAttendeesDao = helper.getEventAttendeesDao();


            String user = "me";
            int totalItemsInserted=0;
            String pageToken = null;
            Calendar cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());
            DateTime now = new DateTime(cal.getTimeInMillis());
            cal.add(Calendar.MONTH, -6); // substract 6 months
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
                                Person person = helper.personExistsByEmail(gcalevent.getCreator().getEmail());
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
                                Person person = helper.personExistsByEmail(gcalevent.getOrganizer().getEmail());
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
                                    Person person = helper.personExistsByEmail(attendee.getEmail());
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
                    GenericRawResults<String[]> rawResults = helper.getEventDao().queryRaw("select * from Event_fts limit 1");
                    if (rawResults.getResults().size()==0){
                        helper.getEventDao().queryRaw("INSERT INTO Event_fts SELECT \"_id\", \"description\" from Event");
                    }else{
                        helper.getEventDao().queryRaw("INSERT INTO Event_fts SELECT \"_id\", \"description\" from Event order by \"_id\" desc limit "+totalItemsInserted);
                    }
                    GenericRawResults<String[]> vrResults =helper.getEventDao().queryRaw("SELECT * FROM Event_fts;");
                    System.err.println("VIRTUAL TABLE ADDED = "+vrResults.getResults().size());

                }catch (SQLException e){
                    helper.getEventDao().queryRaw("DROP TABLE IF EXISTS Event_fts ");
                    helper.getEventDao().queryRaw("CREATE VIRTUAL TABLE Event_fts USING fts4 ( \"_id\", \"description\" )");
                    helper.getEventDao().queryRaw("INSERT INTO Event_fts SELECT \"_id\", \"description\" from Event");
                }
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Integer output) {
            mProgress.hide();
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.putExtra("key", "gcal");
            myIntent.putExtra("items", output);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);

//            if (output == 0) {
//                Snackbar.make(findViewById(R.id.gcalCoordinatorLayout), "No calendar events fetched.", Snackbar.LENGTH_SHORT ).show();
//            } else {
//                Snackbar.make(findViewById(R.id.gcalCoordinatorLayout), output+" calendar events fetched.", Snackbar.LENGTH_SHORT ).show();
//            }


        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GcalActivity.REQUEST_AUTHORIZATION);
                } else {
                    Snackbar.make(findViewById(R.id.gcalCoordinatorLayout), "The following error occurred:\n"
                            + mLastError.getMessage(), Snackbar.LENGTH_SHORT ).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.gcalCoordinatorLayout), "Request cancelled"
                        + mLastError.getMessage(), Snackbar.LENGTH_SHORT ).show();
            }
        }

    }

}




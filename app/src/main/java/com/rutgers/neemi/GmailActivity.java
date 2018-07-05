package com.rutgers.neemi;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.BooleanResult;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.Data;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.ListSendAsResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.SendAs;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.ParseLocation;
import com.joestelmach.natty.Parser;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.EmailBcc;
import com.rutgers.neemi.model.EmailCc;
import com.rutgers.neemi.model.EmailTo;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.TaskDefinition;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;


public class GmailActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    private SignInButton gmailButton;
    ProgressDialog mProgress;
    private static final String TAG = "GmailActivity";

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {GmailScopes.GMAIL_READONLY};
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);


        dbHelper=DatabaseHelper.getHelper(this);

        //Google widgets
        gmailButton = (SignInButton) findViewById(R.id.gmailApiButton);
        gmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gmailButton.setEnabled(false);
                getResultsFromApi();
                gmailButton.setEnabled(true);
            }
        });



        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Getting your emails ...");
        mProgress.setIndeterminate(false);
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);


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
                                this.getPreferences(Context.MODE_PRIVATE);
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
            Snackbar.make(findViewById(R.id.gmailCoordinatorLayout), "No network connection available", Snackbar.LENGTH_SHORT ).show();
        } else {
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


    private class ExtractTimeTask extends AsyncTask<Void, Void, Boolean> {

        Parser parser = new Parser();


        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return extractEmailTime();
            } catch (Exception e) {
                return false;

            }
        }

        @Override
        protected void onPostExecute(Boolean output) {

            if (output) {
                Log.d(TAG,"All dates have been extracted");
            } else {
                Log.e(TAG,"There was an error while extracting email dates");
            }
        }


        private boolean extractEmailTime() {

            RuntimeExceptionDao<Email, String> emailDao = dbHelper.getEmailDao();
            List<Email> results = null;

            QueryBuilder<Email, String> queryBuilder = emailDao.queryBuilder();
            try {
                results = queryBuilder.query();
            } catch (SQLException e) {
                e.printStackTrace();
                //return false;
            }
            //int count=0;
            try {
                for (Email email : results) {
                    Date extractedDate=null;
                    //Log.e(TAG, String.valueOf(count));
                    if (email.getTextContent() != null) {
                        extractedDate = extractTime(email.getTextContent(), email.getDate());
                        if (extractedDate != null) {
                            email.setBodyDate(extractedDate);
                        }
                    }
                    if (email.getTextContent() == null || extractedDate==null){
                        extractedDate = extractTime(email.getSnippet(), email.getDate());
                        if (extractedDate != null) {
                            email.setBodyDate(extractedDate);
                        }
                    }
                    if (email.getSubject() != null) {
                        extractedDate = extractTime(email.getSubject(), email.getDate());
                        if (extractedDate != null) {
                            email.setSubjectDate(extractedDate);
                        }
                    }
                    emailDao.update(email);

                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }

        private Date extractTime(String text, Date referDate) {

            Date extractedDate = null;

            try {
                List<DateGroup> groups = parser.parse(text, referDate);
                for (DateGroup group : groups) {
                    List dates = group.getDates();
//                int line = group.getLine();
//                int column = group.getPosition();
//                String matchingValue = group.getText();
//                String syntaxTree = group.getSyntaxTree().toStringTree();
//                Map<String, List<ParseLocation>> parseMap = group.getParseLocations();
//                boolean isRecurring = group.isRecurring();
//                Date recursUntil = group.getRecursUntil();
                    if (dates!=null && dates.size()>0) {
                        extractedDate = (Date) dates.get(0);
                        break;
                    }
                }
            }catch(Exception e){
                return null;
            }

            return extractedDate;

        }
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Integer> {

        private com.google.api.services.gmail.Gmail gmailService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            gmailService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Gmail API Android")
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
         * @return List of Strings describing returned emails.
         * @throws IOException
         */
        private int getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            RuntimeExceptionDao<Email, String> emailDao = dbHelper.getEmailDao();
            RuntimeExceptionDao<EmailTo, String> emailToDao = dbHelper.getEmailToDao();
            RuntimeExceptionDao<EmailCc, String> emailCcDao = dbHelper.getEmailCcDao();
            RuntimeExceptionDao<EmailBcc, String> emailBccDao = dbHelper.getEmailBccDao();
            RuntimeExceptionDao<Person, String> personDao = dbHelper.getPersonDao();



            //final SQLiteDatabase db = dbHelper.getWritableDatabase();
            ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
            //emailDao.queryRaw("delete from  Email;");
            //emailDao.queryRaw("delete from  Email_fts;");
//                try {
//                    //TableUtils.clearTable(connectionSource, Email.class,false);
//                    TableUtils.clearTable(connectionSource, Email.class);
//
//
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
            //RuntimeExceptionDao<Email, String> emailDao = helper.getEmailDao();
//            helper.getEmailDao().queryRaw("CREATE VIRTUAL TABLE Email_fts USING fts4 ( \"_id\", \"textContent\",\"subject\" )");
//            helper.getEmailDao().queryRaw("INSERT INTO Email_fts SELECT \"_id\", \"textContent\",\"subject\" from Email");
//            helper.getEmailDao().queryRaw("INSERT INTO Email_fts SELECT \"_id\", \"textContent\",\"subject\" from Email order by \"_id\" desc");

                String user = "me";
                int totalItemsInserted = 0;
                String pageToken = null;
                Calendar cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());
                cal.add(Calendar.MONTH, -3); // substract 6 months
                //cal.add(Calendar.DATE, -1); // substract 1 day

            Long since = cal.getTimeInMillis() / 1000;
                System.out.println("since = " + since);
                String timestamp = null;

                GenericRawResults<String[]> rawResults = emailDao.queryRaw("select max(timestamp) from Email;");
                List<String[]> results = null;
                try {
                    results = rawResults.getResults();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (results != null) {
                    String[] resultArray = results.get(0);
                    System.out.println("timestamp= " + resultArray[0]);
                    timestamp = resultArray[0];
                }


                if (timestamp != null) {
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    since = cal.getTimeInMillis();
                }

                System.out.println("Since=" + since);


            ListMessagesResponse response = gmailService.users().messages().list(user)
                    .setPageToken(pageToken)
                    .setQ("after:" + since)
                    .execute();

            //pageToken = response.getNextPageToken();

            List<Message> messages = new ArrayList<Message>();
            while (response.getMessages() != null) {
                messages.addAll(response.getMessages());
                if (response.getNextPageToken() != null) {
                    pageToken = response.getNextPageToken();
                    response = gmailService.users().messages().list(user)
                            .setPageToken(pageToken)
                            .setQ("after:" + since)
                            .execute();
                } else {
                    break;
                }
            }

            mProgress.setMax(messages.size());

//                final List<Message> messageslist = new ArrayList<Message>();
//
//                JsonBatchCallback<Message> callback = new JsonBatchCallback<Message>() {
//                    public void onSuccess(Message message, HttpHeaders responseHeaders) {
//                        System.out.println("MessageThreadID:"+ message.getThreadId());
//                        System.out.println("MessageID:"+ message.getId());
//                        synchronized (messageslist) {
//                            messageslist.add(message);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders)
//                            throws IOException {
//                    }
//                };


//                BatchRequest batch  = gmailService.batch();
//                if (messages!=null) {
//                    for (int i = 0; i < messages.size(); i++) {
//                        gmailService.users().messages().get(user, messages.get(i).get("id").toString()).queue(batch, callback);
//                    }
//                }

                //batch.execute();

                System.out.println("Getting EMAILS!!!");

                if (messages!=null){
                    for (int i = 0; i < messages.size(); i++) {
                       // try {
                            Message msg = gmailService.users().messages().get(user, messages.get(i).get("id").toString()).execute();
                            List<String> labels =  msg.getLabelIds();
                            if(labels.contains("CATEGORY_PROMOTIONS")){
                                continue;
                            }

                            List<MessagePart> parts = msg.getPayload().getParts();
                            List<MessagePartHeader> headers = msg.getPayload().getHeaders();
                            Email email = readParts(parts);
                            email.setTimestamp(System.currentTimeMillis() / 1000);
                            email.setId(msg.getId());
                            email.setThreadId(msg.getThreadId());
                            email.setSnippet(msg.getSnippet());
                            //email.setLabelIds(msg.getLabelIds());
                            email.setHistoryId(msg.getHistoryId());
                            email.setDate(new Date(msg.getInternalDate()));

                            if(parts==null) {
                                if (msg.getPayload().getMimeType().contentEquals("text/plain")) {
                                    String s = new String(Base64.decodeBase64(msg.getPayload().getBody().getData().getBytes()));
                                    email.setTextContent(s);
                                }
                                if (msg.getPayload().getMimeType().contentEquals("text/html")) {
                                    String s = new String(Base64.decodeBase64(msg.getPayload().getBody().getData().getBytes()));
                                    email.setHtmlContent(s);
                                }
                            }

                            for (MessagePartHeader header : headers) {
                                String name = header.getName();
                                if (name.equalsIgnoreCase("From")) {
                                    Person p =parsePerson(header.getValue());
                                    Person person = dbHelper.personExistsByEmail(p.getEmail());
                                    if (person ==null) {
                                        personDao.create(p);
                                        email.setFrom(p);
                                    }else {
                                        email.setFrom(person);
                                    }
                                } else if (name.equalsIgnoreCase("To")) {
                                    email.setTo(parsePeople(header.getValue()));
                                } else if (name.equalsIgnoreCase("Bcc")) {
                                    email.setBcc(parsePeople(header.getValue()));
                                } else if (name.equalsIgnoreCase("Cc")) {
                                    email.setCc(parsePeople(header.getValue()));
                                } else if (name.equalsIgnoreCase("subject")) {
                                    email.setSubject(header.getValue());
                                }
                            }

                            emailDao.create(email);

                            if (email.getTo()!=null && !email.getTo().isEmpty()) {
                                for (Person p:email.getTo()) {
                                    Person person = dbHelper.personExistsByEmail(p.getEmail());
                                    if (person ==null) {
                                        personDao.create(p);
                                        emailToDao.create(new EmailTo(p,email));
                                    }else {
                                        emailToDao.create(new EmailTo(person,email));
                                    }
                                }
                            }
                            if (email.getCc()!=null && !email.getCc().isEmpty()) {
                                for (Person p:email.getCc()) {
                                    Person person = dbHelper.personExistsByEmail(p.getEmail());
                                    if (person ==null) {
                                        personDao.create(p);
                                        emailCcDao.create(new EmailCc(p,email));
                                    }else {
                                        emailCcDao.create(new EmailCc(person,email));
                                    }
                                }
                            }
                            if (email.getBcc()!=null && !email.getBcc().isEmpty()) {
                                for (Person p:email.getBcc()) {
                                    Person person = dbHelper.personExistsByEmail(p.getEmail());
                                    if (person ==null) {
                                        personDao.create(p);
                                        emailBccDao.create(new EmailBcc(p,email));
                                    }else {
                                        emailBccDao.create(new EmailBcc(person,email));
                                    }
                                }
                            }

                            totalItemsInserted++;
                            mProgress.setProgress(totalItemsInserted);
                        //} catch (Exception e) {
                        //    System.out.println("Exception :" + e.getMessage());
                        //}
                    }
                }
                System.out.println("EmailsInserted Final = " + totalItemsInserted);

                loadEmailIndex(totalItemsInserted);
                //db.setTransactionSuccessful();
                //db.endTransaction();
                return totalItemsInserted;

        }


        private Person parsePerson(String person){
            Person p = new Person();

            if(person.contains("<")){
                String[] whoNames = person.split("<");
                if (whoNames.length > 1) { //it has both email and name
                    if (whoNames[0].contains("\"") && whoNames[0].length()>2) {
                        p.setName(whoNames[0].substring(1, whoNames[0].length() - 2));
                    } else {
                        p.setName(whoNames[0]);
                    }
                    p.setEmail(whoNames[1].substring(0,whoNames[1].length() - 1));
                } else {
                    p.setEmail(whoNames[0]);
                }
            }else {
                p.setEmail(person);
            }

            return p;
        }

        private ArrayList<Person> parsePeople(String persons){
            ArrayList<Person> people = new ArrayList<>();
            String[] whos = persons.split(",");
            for(String who: whos){
                people.add(parsePerson(who));
            }

            return people;
        }



        private void loadEmailIndex(final int totalItemsInserted) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        GenericRawResults<String[]> rawResults = dbHelper.getEmailDao().queryRaw("select * from Email_fts limit 1");
                        if (rawResults.getResults().size()==0){
                            dbHelper.getEmailDao().queryRaw("INSERT INTO Email_fts SELECT \"_id\", \"textContent\", \"subject\" from Email");
                        }else{
                            dbHelper.getEmailDao().queryRaw("INSERT INTO Email_fts SELECT \"_id\", \"textContent\",\"subject\" from Email order by \"_id\" desc limit "+totalItemsInserted);
                        }
                        GenericRawResults<String[]> vrResults =dbHelper.getEmailDao().queryRaw("SELECT * FROM Email_fts;");
                        System.err.println("VIRTUAL TABLE ADDED = "+vrResults.getResults().size());

                    }catch (Exception e){
                        dbHelper.getEmailDao().queryRaw("DROP TABLE IF EXISTS Email_fts ");
                        dbHelper.getEmailDao().queryRaw("CREATE VIRTUAL TABLE Email_fts USING fts4 ( \"_id\", \"textContent\",\"subject\" )");
                        dbHelper.getEmailDao().queryRaw("INSERT INTO Email_fts SELECT \"_id\", \"textContent\",\"subject\" from Email");
                    }


                }
            }).start();
        }


    private Email readParts(List<MessagePart> parts){
        Email email = new Email();
        if(parts!=null) {
            for (MessagePart part : parts) {
                try {
                    String mime = part.getMimeType();
                    if (mime.contentEquals("text/plain")) {
                        String s = new String(Base64.decodeBase64(part.getBody().getData().getBytes()));
                        email.setTextContent(s);
                    } else if (mime.contentEquals("text/html")) {
                        String s = new String(Base64.decodeBase64(part.getBody().getData().getBytes()));
                        email.setHtmlContent(s);
                    } else if (mime.contentEquals("multipart/alternative") || mime.contentEquals("multipart/related") || mime.contentEquals("multipart/mixed")) {
                        List<MessagePart> subparts = part.getParts();
                        Email subreader = readParts(subparts);
                        email.setHtmlContent(subreader.getHtmlContent());
                        email.setTextContent(subreader.getTextContent());
                    } else if (mime.contains("application") || mime.contains("image")) {
                        email.setHasAttachments(true);
                    }else{
                        System.err.println("Mime-type = "+mime);
                    }

                } catch (Exception e) {
                    System.err.println("Error on reading email parts" + e);// get file here
                }
            }
        }
        return email;
    }


    @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Integer output) {
            mProgress.dismiss();
            new ExtractTimeTask().execute();

            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.putExtra("key", "gmail");
            myIntent.putExtra("items", output);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);

            if (output == 0) {
                Snackbar.make(findViewById(R.id.gmailCoordinatorLayout), "No emails fetched.", Snackbar.LENGTH_LONG ).show();
            } else {
                Snackbar.make(findViewById(R.id.gmailCoordinatorLayout), output+" emails fetched.", Snackbar.LENGTH_LONG ).show();
            }


        }

        @Override
        protected void onCancelled() {
            mProgress.dismiss();
            if (mLastError != null) {
                Log.d(TAG, "ERROR "+mLastError);
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GcalFragment.REQUEST_AUTHORIZATION);
                } else {
                    mLastError.printStackTrace();
                    Snackbar.make(findViewById(R.id.gmailCoordinatorLayout), "Something went wrong.. ", Snackbar.LENGTH_LONG ).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.gmailCoordinatorLayout), "Request cancelled"
                        + mLastError.getMessage(), Snackbar.LENGTH_LONG ).show();
            }
        }


    }
}




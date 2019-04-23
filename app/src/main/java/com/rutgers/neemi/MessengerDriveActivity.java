/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rutgers.neemi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.util.Base64;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.events.OpenFileCallback;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.GPSLocation;
import com.rutgers.neemi.model.Message;
import com.rutgers.neemi.model.MessageHasPlaces;
import com.rutgers.neemi.model.MessageParticipants;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.PlaceHasCategory;
import com.rutgers.neemi.model.StayPoint;
import com.rutgers.neemi.model.StayPointHasPlaces;
import com.rutgers.neemi.model.TransactionHasPlaces;
import com.rutgers.neemi.util.OpenNLP;
import com.rutgers.neemi.util.Utilities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * An abstract activity that handles authorization and connection to the Drive
 * services.
 */
public class MessengerDriveActivity extends AppCompatActivity {


    private static final String TAG = "Messenger Drive Activity";


    /**
     * Request code for google sign-in
     */
    protected static final int REQUEST_CODE_SIGN_IN = 0;


    /**
     * Request code for the Drive picker
     */
    protected static final int REQUEST_CODE_OPEN_ITEM = 1;

    /**
     * Handles high-level drive functions like sync
     */
    private DriveClient mDriveClient;

    /**
     * Handle access to Drive resources/files.
     */
    private DriveResourceClient mDriveResourceClient;

    /**
     * Tracks completion of the drive picker
     */
    private TaskCompletionSource<DriveId> mOpenItemTaskSource;

    public DriveFile file;

    private ProgressDialog mProgressDialog;

    RuntimeExceptionDao<GPSLocation, String> gpsDao;

    GeoApiContext gmapsContext = new GeoApiContext.Builder()
            .apiKey("AIzaSyDe8nWbXFA6ESFS6GnQtYPPsXzYmLz3Lf0")
            .build();




    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
    DatabaseHelper helper;
    RuntimeExceptionDao<StayPoint, String> stayPointDao;
    int items=1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmaps);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Getting your Messenger messages ...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);


        GoogleSignInClient mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);



        helper=DatabaseHelper.getHelper(this);
        gpsDao = helper.getGpsLocationtRuntimeDao();

    }





    /**
     * Called when the activity will start interacting with the user.
     * At this point your activity is at the top of the activity stack,
     * with user input going to it.
     */


    @Override
    protected void onResume() {
        super.onResume();
        buildGoogleSignInClient();
    }



    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }


    protected Task<DriveId> pickTextFile() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        //.setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.script+json"))
                        .setActivityTitle(getString(R.string.select_file))
                        .build();
        return pickItem(openOptions);
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    protected Task<DriveId> pickFolder() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        .setSelectionFilter(
                                Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
                        .setActivityTitle(getString(R.string.select_folder))
                        .build();
        return pickItem(openOptions);
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @param openOptions Filter that should be applied to the selection
     * @return Task that resolves with the selected item's ID.
     */
    private Task<DriveId> pickItem(OpenFileActivityOptions openOptions) {
        mOpenItemTaskSource = new TaskCompletionSource<>();
        getDriveClient()
                .newOpenFileActivityIntentSender(openOptions)
                .continueWith(new Continuation<IntentSender, Void>() {
                    @Override
                    public Void then(@NonNull Task<IntentSender> task) throws Exception {
                        startIntentSenderForResult(
                                task.getResult(), REQUEST_CODE_OPEN_ITEM, null, 0, 0, 0);
                        return null;
                    }
                });
        return mOpenItemTaskSource.getTask();
    }

    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }



    /**
     *  Handle Response of selected file
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    initializeDriveClient(getAccountTask.getResult());
                } else {
                    Log.e(TAG, "Sign-in failed.");
                    finish();
                }
                break;
            case REQUEST_CODE_OPEN_ITEM:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    mOpenItemTaskSource.setResult(driveId);
                } else {
                    mOpenItemTaskSource.setException(new RuntimeException("Unable to open file"));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady();
    }


    protected DriveClient getDriveClient() {
        return mDriveClient;
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }


    protected void onDriveClientReady() {



        pickTextFile()
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveId>() {
                            @Override
                            public void onSuccess(DriveId driveId) {
                                retrieveContents(driveId.asDriveFile());
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "No file selected", e);
                        showMessage(getString(R.string.file_not_selected));
                        finish();
                    }
                });
    }

    private void retrieveContents(DriveFile file) {


        OpenFileCallback openCallback = new OpenFileCallback() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {
                // Update progress dialog with the latest progress.
                int progress = (int) (bytesDownloaded * 100 / bytesExpected);
              //  Log.d(TAG, String.format("Loading progress: %d percent", progress));
                mProgressDialog.setProgress(progress);
                mProgressDialog.show();
            }

            private void readZipFile(DriveContents contents){
                InputStream is;
                ZipInputStream zis;
                try
                {
                    String filename;
                    is = contents.getInputStream();
                    zis = new ZipInputStream(new BufferedInputStream(is), Charset.forName("UTF-8"));
                    ZipEntry ze;
                    int totalItemsInserted = 0;

                    while ((ze = zis.getNextEntry()) != null) {
                        filename = ze.getName();

                        if (filename.endsWith(".json")) {

                            JsonReader jsonReader = Json.createReader(zis);
                            JsonObject jsonObject=null;
                            try {
                                jsonObject = jsonReader.readObject();
                            }catch (Exception e){
                                System.err.println("Reading zip json error= "+e);
                            }

                            if (jsonObject != null) {

                                String thread_path = jsonObject.get("thread_path").toString();
                                String thread = thread_path.substring(thread_path.indexOf("/")+1,thread_path.length()-1);
                                int thread_id=1;

                                JsonArray participants = (JsonArray) jsonObject.get("participants");
                                if(participants!=null) {
                                    for (int j = 0; j < participants.size(); j++) {
                                        JsonObject participant = (JsonObject) participants.get(j);
                                        Person p = new Person();
                                        p.setName(convertToUTF8(participant.getString("name")));
                                        System.err.println("participant: " + p.getName());
                                        Person person = helper.personExistsByName(p.getName());
                                        if (person == null) {
                                            helper.getPersonDao().create(p);
                                            MessageParticipants mp = new MessageParticipants(p, thread);
                                            helper.getMessageParticipantsDao().create(mp);
                                        }else {
                                            MessageParticipants mp = new MessageParticipants(person, thread);
                                            helper.getMessageParticipantsDao().create(mp);
                                        }
                                    }
                                }

                                //read thread's messages
                                JsonArray messages = (JsonArray) jsonObject.get("messages");
                                if(messages!=null) {
                                    Long previous_timestamp = null;
                                    for (int j = 0; j < messages.size(); j++) {
                                        JsonObject msg = (JsonObject) messages.get(j);
                                        Message message = new Message();
                                        message.setThread(thread);
                                        ArrayList<Place> listOfPlaces=new ArrayList<>();
                                        if(msg.containsKey("content")) {
                                            //get message content
                                            message.setContent(convertToUTF8(msg.getString("content")));
                                            //get message Timestamp
                                            Long timestamp = Long.parseLong(msg.getJsonNumber("timestamp_ms").toString());
                                            message.setTimestamp(timestamp);
                                            if (hasNminutesDifference(previous_timestamp, timestamp, 3)) {
                                                thread_id++;
                                            }
                                            previous_timestamp = timestamp;
                                            //get message sender
                                            Person p = new Person();
                                            p.setName(msg.getString("sender_name"));
                                            Person person = helper.personExistsByName(p.getName());
                                            if (person == null) {
                                                helper.getPersonDao().create(p);
                                                message.setFrom(p);
                                            } else {
                                                message.setFrom(person);
                                            }

                                            message.setThread_id(thread_id);
                                            helper.getMessageDao().create(message);

//                                            try {
//                                                System.out.println("-------Finding entities belonging to category : location------");
//                                                OpenNLP openNLP = new OpenNLP(getApplicationContext());
//                                                ArrayList<String> locations = openNLP.findLocation(message.getContent());
//                                                for (String location:locations){
//                                                    PlacesSearchResponse gmapsResponse = PlacesApi.textSearchQuery(gmapsContext, location).await();
//                                                    listOfPlaces.addAll(Utilities.getUtilities(getApplicationContext()).findTransactionPlaces(gmapsResponse));
//                                                }
//                                                for (Place extractedPlace:listOfPlaces){
//                                                    MessageHasPlaces msgHasPlaces = new MessageHasPlaces(message,extractedPlace);
//                                                    helper.getMessageHasPlacesDao().create(msgHasPlaces);
//                                                }
//
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            } catch (ApiException e) {
//                                                e.printStackTrace();
//                                            }



                                            totalItemsInserted++;
                                        }
                                    }
                                }
                            }

                        }

                        zis.closeEntry();

                    }

                    zis.close();
                    if (totalItemsInserted>1) {
                        loadEmailIndex(totalItemsInserted);
                    }


                    new ExtractTimeTask().execute();

                } catch(IOException e) {
                    showMessage("error unzipping file");
                    e.printStackTrace();
                    mProgressDialog.dismiss();
                }

            }


            public boolean hasNminutesDifference(Long timestampAfter, Long timestampBefore, int hoursDifference){

                if(timestampAfter==null)
                    return false;

                Long difference = Math.abs(timestampAfter - timestampBefore)/1000;


                // calculate hours minutes and seconds
                Long hours = difference / 3600;
                Long minutes = (difference % 3600) / 60;
                Long seconds = (difference % 3600) % 60;

                if (hours>=hoursDifference){
                    return true;
                }
                return false;
            }



            private void loadEmailIndex(final int totalItemsInserted) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            GenericRawResults<String[]> rawResults = helper.getEmailDao().queryRaw("select * from Message_fts limit 1");
                            if (rawResults.getResults().size()==0){
                                helper.getEmailDao().queryRaw("INSERT INTO Message_fts SELECT \"_id\", \"content\" from Message");
                            }else{
                                helper.getEmailDao().queryRaw("INSERT INTO Message_fts SELECT \"_id\", \"content\" from Message order by \"_id\" desc limit "+totalItemsInserted);
                            }
                            GenericRawResults<String[]> vrResults =helper.getMessageDao().queryRaw("SELECT * FROM Message_fts;");
                            System.err.println("VIRTUAL TABLE ADDED = "+vrResults.getResults().size());

                        }catch (Exception e){
                            helper.getEmailDao().queryRaw("DROP TABLE IF EXISTS Message_fts ");
                            helper.getEmailDao().queryRaw("CREATE VIRTUAL TABLE Message_fts USING fts4 ( \"_id\", \"content\" )");
                            helper.getEmailDao().queryRaw("INSERT INTO Message_fts SELECT \"_id\", \"content\" from Message");
                        }


                    }
                }).start();
            }

            // convert from internal Latin-1 -> UTF-8
            public String convertToUTF8(String s) {
                String out = null;
                try {
                    out = new String(s.getBytes("ISO-8859-1"),"UTF-8" );
                } catch (java.io.UnsupportedEncodingException e) {
                    return null;
                }
                return out;
            }


            @Override
            public void onContents(@NonNull DriveContents driveContents) {
                mProgressDialog.setMessage("Parsing your messages. Please wait...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.show();
                readZipFile(driveContents);
                mProgressDialog.dismiss();

            }

            @Override
            public void onError(@NonNull Exception e) {
                // Handle error
                // [START_EXCLUDE]
                mProgressDialog.dismiss();
                Log.e(TAG, "Unable to read contents", e);
                showMessage(getString(R.string.read_failed));
                finish();
                // [END_EXCLUDE]
            }
        };

        getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY, openCallback);

    }



    private class ExtractTimeTask extends AsyncTask<Void, Void, Boolean> {

        Parser parser = new Parser();


        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return extractMessageTime();
            } catch (Exception e) {
                return false;

            }
        }

        @Override
        protected void onPostExecute(Boolean output) {

            if (output) {
                Log.d(TAG,"All dates have been extracted");
            } else {
                Log.e(TAG,"There was an error while extracting message dates");
            }
            mProgressDialog.dismiss();
        }


        private boolean extractMessageTime() {

            RuntimeExceptionDao<Message, String> messageDao = helper.getMessageDao();
            List<Message> results = null;

            QueryBuilder<Message, String> queryBuilder = messageDao.queryBuilder();
            try {
                results = queryBuilder.query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                for (Message msg : results) {
                    if (msg.getContent() != null) {
                        Date extractedDate = extractTime(msg.getContent(), new Date(msg.getTimestamp()));
                        if (extractedDate != null) {
                             msg.setContentDate(extractedDate);
                        }
                    }
                    messageDao.update(msg);

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




}
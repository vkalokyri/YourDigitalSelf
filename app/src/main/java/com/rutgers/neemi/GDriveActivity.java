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

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.maps.model.PhotoResult;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.model.Category;
import com.rutgers.neemi.model.Place;
import com.rutgers.neemi.model.TransactionHasCategory;
import com.rutgers.neemi.model.Transaction;
import com.rutgers.neemi.parser.BankDescriptionParser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An abstract activity that handles authorization and connection to the Drive
 * services.
 */
public class GDriveActivity extends Activity {

    private static final String TAG = "Google Drive Activity";
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

    private ProgressBar mProgressBar;



    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
    DatabaseHelper helper;
    RuntimeExceptionDao<Category, String> categoryDao;
    RuntimeExceptionDao<Transaction, String> transactionDao;
    RuntimeExceptionDao<Place, String> placeDao;
    RuntimeExceptionDao<TransactionHasCategory, String> transactionHasCategoriesDao;
    int items=1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdrive);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        GoogleSignInClient mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        helper=DatabaseHelper.getHelper(this);
        categoryDao = helper.getCategoryDao();
        transactionDao = helper.getTransactionDao();
        transactionHasCategoriesDao = helper.getTransactionHasCategoryRuntimeDao();
        placeDao = helper.getPlaceDao();



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
                        .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "text/csv"))
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
                Log.d(TAG, String.format("Loading progress: %d percent", progress));
                mProgressBar.setProgress(progress);
            }


            @Override
            public void onContents(@NonNull DriveContents driveContents) {
                // onProgress may not be called for files that are already
                // available on the device. Mark the progress as complete
                // when contents available to ensure status is updated.
                mProgressBar.setProgress(100);
                // Read contents
                // [START_EXCLUDE]
               // DriveContents contents = task.getResult();
                // Process contents...
                // [START_EXCLUDE]
                // [START read_as_string]
                GeoApiContext geoApiContext = new GeoApiContext.Builder()
                        .apiKey("AIzaSyC2c-DGrPjl947J-8mTE7bRZT_OO-F3Dno")
                        .build();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(driveContents.getInputStream()))) {


                    CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                            .withHeader("Date", "Description", "Amount", "Category")
                            .withFirstRecordAsHeader()
                            .withIgnoreHeaderCase()
                            .withTrim());

                    Iterable<CSVRecord> csvRecords = csvParser.getRecords();
                    items=(int)csvParser.getRecordNumber();

                    for (CSVRecord csvRecord : csvRecords){
                        //Payment payment = new Payment();
                        StringBuilder googleMapQuery = new StringBuilder();


                        BankDescriptionParser parser = new BankDescriptionParser(getApplicationContext());
                        Transaction payment = parser.parser_memo(csvRecord.get("Description"),new Date());
                        googleMapQuery.append(payment.getMerchant_name());

                        String date =csvRecord.get("Date");
                        payment.setDate(format.parse(date).getTime());
                        payment.setAmount(Double.parseDouble(csvRecord.get("Amount")));
                        payment.setPending(false);
                        payment.setTimestamp(System.currentTimeMillis() / 1000);

                        if (payment.getPlace()!=null){
                            Place placeExists=null;
                            if (payment.getPlace().getPhone_number()!=null) {
                                placeExists = helper.placeExistsByPhone(payment.getPlace().getPhone_number());
                                if (placeExists==null) {
                                    placeExists = payment.getPlace();
                                    payment.setPlace(placeExists);
                                }else {
                                    payment.setPlace(placeExists);
                                }
                                googleMapQuery.append("+");
                                googleMapQuery.append(payment.getPlace().getPhone_number());
                            }else {
                                if(payment.getPlace().getCity()!=null && payment.getPlace().getState()!=null){
                                    placeExists = helper.placeExistsByStateCity(payment.getPlace().getState(),payment.getPlace().getCity());
                                    if (placeExists == null) {
                                        Place newPlace = payment.getPlace();
                                        placeExists=newPlace;
                                        payment.setPlace(placeExists);
                                    }else{
                                        payment.setPlace(placeExists);
                                    }
                                    googleMapQuery.append("+");
                                    googleMapQuery.append(payment.getPlace().getCity());
                                    googleMapQuery.append("+");
                                    googleMapQuery.append(payment.getPlace().getState());
                                }
                            }


                            PlacesSearchResponse gmapsResponse = null;
                            try {
                                gmapsResponse = PlacesApi.textSearchQuery(geoApiContext, googleMapQuery.toString()).await();
                                if (gmapsResponse.results != null) {
                                    if (gmapsResponse.results.length > 0) {
                                        PlacesSearchResult place = gmapsResponse.results[0];
                                        if (place.photos != null) {
                                            PhotoResult photoResult = PlacesApi.photo(geoApiContext,place.photos[0].photoReference).maxWidth(755)
                                                    .await();
                                            byte[] image = photoResult.imageData;

                                            //String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + gmapsResponse.results[0].photos[0].photoReference + "&key=AIzaSyAG3EDauXS9f5BsCEPb90rl7Cdub2VvUZE";
                                            if (placeExists!=null) {
                                                placeExists.setImage(image);
                                            }else{
                                                placeExists = new Place();
                                                placeExists.setImage(image);
                                                placeExists.setName(place.name);
                                                placeExists.setStreet(place.formattedAddress);
                                            }

                                        }
                                    }
                                }
                            } catch (ApiException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            placeDao.create(placeExists);
                            payment.setPlace(placeExists);
                            transactionDao.create(payment);


                        }

                        String category =csvRecord.get("Category");
                        List<Category> categoryList = new ArrayList<>();
                        Category categoryExists = helper.categoryExists(category);
                        if (categoryExists == null) {
                            Category newCategory = new Category();
                            newCategory.setCategoryName(category);
                            categoryDao.create(newCategory);
                            categoryList.add(newCategory);
                        } else {
                            TransactionHasCategory trans_categories = new TransactionHasCategory(payment, categoryExists);
                            transactionHasCategoriesDao.create(trans_categories);
                        }

                        for (Category eachCategory : categoryList) {
                            TransactionHasCategory trans_categories = new TransactionHasCategory(payment, eachCategory);
                            transactionHasCategoriesDao.create(trans_categories);
                        }




                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // [END read_as_string]
                // [END_EXCLUDE]
                // [START discard_contents]
                Task<Void> discardTask = getDriveResourceClient().discardContents(driveContents);
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                myIntent.putExtra("key", "gdrive");
                myIntent.putExtra("items", items);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent);
                // [END_EXCLUDE]
            }

            @Override
            public void onError(@NonNull Exception e) {
                // Handle error
                // [START_EXCLUDE]
                Log.e(TAG, "Unable to read contents", e);
                showMessage(getString(R.string.read_failed));
                finish();
                // [END_EXCLUDE]
            }
        };

        getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY, openCallback);


        // [START open_file]
//       Task<DriveContents> openFileTask = getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);
//        // [END open_file]
//        // [START read_contents]
//        openFileTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
//                    @Override
//                    public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
//                        DriveContents contents = task.getResult();
//                        // Process contents...
//                        // [START_EXCLUDE]
//                        // [START read_as_string]
//                        try (BufferedReader reader = new BufferedReader(
//                                new InputStreamReader(contents.getInputStream()))) {
//
//
//                            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
//                                    .withHeader("Date", "Description", "Amount", "Category")
//                                    .withFirstRecordAsHeader()
//                                    .withIgnoreHeaderCase()
//                                    .withTrim());
//
//                            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
//                            items=(int)csvParser.getRecordNumber();
//
//                            for (CSVRecord csvRecord : csvRecords){
//                                //Payment payment = new Payment();
//
//
//                                BankDescriptionParser parser = new BankDescriptionParser(getApplicationContext());
//                                Transaction payment = parser.parser_memo(csvRecord.get("Description"),new Date());
//
//                                String date =csvRecord.get("Date");
//                                payment.setDate(format.parse(date).getTime());
//                                payment.setAmount(Double.parseDouble(csvRecord.get("Amount")));
//                                payment.setPending(false);
//                                payment.setTimestamp(System.currentTimeMillis() / 1000);
//
//                                if (payment.getPlace()!=null){
//                                    if (payment.getPlace().getPhone_number()!=null) {
//                                        Place placeExistsByPhone = helper.placeExistsByPhone(payment.getPlace().getPhone_number());
//                                        if (placeExistsByPhone==null) {
//                                            Place newPlace = payment.getPlace();
//                                            placeDao.create(newPlace);
//                                            payment.setPlace(newPlace);
//                                        }else {
//                                            payment.setPlace(placeExistsByPhone);
//                                        }
//                                    }else {
//                                        if(payment.getPlace().getCity()!=null && payment.getPlace().getState()!=null){
//                                            Place placeExists = helper.placeExistsByStateCity(payment.getPlace().getState(),payment.getPlace().getCity());
//                                            if (placeExists == null) {
//                                                Place newPlace = payment.getPlace();
//                                                placeDao.create(newPlace);
//                                                payment.setPlace(newPlace);
//                                            }else{
//                                                payment.setPlace(placeExists);
//                                            }
//                                        }
//                                    }
//                                }
//                                transactionDao.create(payment);
//
//                                String category =csvRecord.get("Category");
//                                List<Category> categoryList = new ArrayList<>();
//                                Category categoryExists = helper.categoryExists(category);
//                                if (categoryExists == null) {
//                                    Category newCategory = new Category();
//                                    newCategory.setCategoryName(category);
//                                    categoryDao.create(newCategory);
//                                    categoryList.add(newCategory);
//                                } else {
//                                    TransactionHasCategory trans_categories = new TransactionHasCategory(payment, categoryExists);
//                                    transactionHasCategoriesDao.create(trans_categories);
//                                }
//
//                                for (Category eachCategory : categoryList) {
//                                    TransactionHasCategory trans_categories = new TransactionHasCategory(payment, eachCategory);
//                                    transactionHasCategoriesDao.create(trans_categories);
//                                }
//                            }
//
//                        }
//                        // [END read_as_string]
//                        // [END_EXCLUDE]
//                        // [START discard_contents]
//                        Task<Void> discardTask = getDriveResourceClient().discardContents(contents);
//                        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
//                        myIntent.putExtra("key", "gdrive");
//                        myIntent.putExtra("items", items);
//                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(myIntent);
//                        // [END discard_contents]
//                        return discardTask;
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Handle failure
//                        // [START_EXCLUDE]
//                        Log.e(TAG, "Unable to read contents", e);
//                        showMessage(getString(R.string.read_failed));
//                        finish();
//                        // [END_EXCLUDE]
//                    }
//                });
        // [END read_contents]
    }

}
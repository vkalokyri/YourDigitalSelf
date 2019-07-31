package com.rutgers.neemi;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.library.v1.proto.ContentCategory;
import com.google.photos.library.v1.proto.ContentFilter;
import com.google.photos.library.v1.proto.Filters;
import com.google.photos.types.proto.MediaItem;
import com.rutgers.neemi.model.Photo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GPhotosActivity extends AppCompatActivity {

    private String TAG = "GPhotosActivity";
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final String PREF_ACCOUNT_NAME = "accountName";
    private String googleAuthToken;
    private String accountName;
    private static String GPHOTOS_SCOPE = "oauth2:https://www.googleapis.com/auth/photoslibrary.readonly";
    public static String HEADER_NAME_AUTH = "Authorization";
    public static String HEADER_AUTH_VAL_PRFX = "Bearer ";
    /** Google API KEY */
    public static String URL_EXTN_API_KEY = "&key=AIzaSyCwsSXnT_jN107mMLhz55vo7JKwREaflJQ";
    /** URL to the paginated next set of images */
    private String nextLink = null;
    public static String JSON_FIELD_ITEMS = "items";
    public static String JSON_FIELD_NEXT_LINK = "nextLink";
    public static String JSON_FIELD_SELF_LINK = "selfLink";
    public static String JSON_FIELD_TITLE = "title";
    public static String JSON_FIELD_THUMBNAIL = "thumbnailLink";
    public static String JSON_FIELD_MIME_TYPE = "mimeType";
    public static String JSON_FIELD_MIME_IMG = "image";
    private boolean endOfResultsDisplay = false;
    public static int currentPage = 1;


    /** URLs and extensions required for the API calls */
    public static String URL_FILES = "https://photoslibrary.googleapis.com/v1/mediaItems:search?alt:json";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sms);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        requestPermissions();


    }


    public void requestPermissions() {
        chooseGoogleAccount();
    }


    /**
     * Construct and return the search url
     *
     * @param currentPage
     * @return
     */
    private String getUrl(int currentPage) {
        String url = null;
        // If its the initial API inocation, construct the URL for Files API, else, use the 'nextLink'
        if(currentPage == 1) {
            url = URL_FILES + URL_EXTN_API_KEY;
        } else {
            url = nextLink + URL_EXTN_API_KEY;
        }
        return url;
    }


    public boolean isMoreAvailable() {
        return (nextLink == null)? false:true;
    }


    private void fetchAndShowImages() {


        if (isDeviceOnline()) {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    if(currentPage ==1 || (currentPage > 1 && isMoreAvailable())) {
                        ArrayList<Photo> photoResults = new ArrayList<>();
                        // get the photo search results
                        try {
                            photoResults = getPhotos(currentPage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (photoResults.size() > 0) {
                        }else
                            endOfResultsDisplay = true;

                        currentPage++;
                    } else {
                        endOfResultsDisplay = true;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {

                }
            }.execute();
        } else {
            Toast.makeText(this, "Device not online", Toast.LENGTH_LONG).show();

        }

    }

    public ArrayList<Photo> getPhotos(int currentPage) throws IOException {


        ArrayList<Photo> photos = new ArrayList<>();

        String serverResponse = fetchFromServer(getUrl(currentPage));

        if (!serverResponse.isEmpty()) {
            try {

                JSONObject response = new JSONObject(serverResponse);
                // If response doesnt have nextLink, then set the variable to null so that
                // we can detect that there are no more pages to come
                if(response.has(JSON_FIELD_NEXT_LINK)) {
                    nextLink = response.getString(JSON_FIELD_NEXT_LINK);
                } else {
                    nextLink = null;
                }
                // Extract the JSONArray containing objects representing each image.
                JSONArray itemsArray = response.getJSONArray(JSON_FIELD_ITEMS);
                Log.d(TAG,"Received " + itemsArray.length() + " photos");
                String mimeType;

                // Iterate the list of images, extract information and build the photo list
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject jObj = itemsArray.getJSONObject(i);

                    //Result will have images and videos. Skip if a video is encountered
                    mimeType = jObj.getString(JSON_FIELD_MIME_TYPE);
                    if(null != mimeType && mimeType.startsWith(JSON_FIELD_MIME_IMG)) {
                        Photo photo = new Photo();
                        photo.setName(jObj.getString(JSON_FIELD_TITLE));
                        photo.setLink(jObj.getString(JSON_FIELD_THUMBNAIL));
                        photos.add(photo);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return photos;
    }



    private String fetchFromServer(String strURL) {
        StringBuffer response = new StringBuffer();
        HttpURLConnection urlConnection = null;
        System.err.println(strURL);
        String line = "";

        try {
            URL url = new URL(strURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            try {
                // Add the AuthToken to header
                urlConnection.setRequestProperty (HEADER_NAME_AUTH,
                        HEADER_AUTH_VAL_PRFX + googleAuthToken);
            } catch (Exception e) {
                Log.e(TAG+"dfghjk",e.getMessage());
            }
            InputStream in;
            int status = urlConnection.getResponseCode();
            Log.d(TAG,"Server response: " + status);

            if(status >= 400)
                in = urlConnection.getErrorStream();
            else
                in = urlConnection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(in));
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            Log.d(TAG,e.getMessage());
        } finally {
            urlConnection.disconnect();
        }
        Log.d(TAG,response.toString());

        return response.toString();
    }





    private void chooseGoogleAccount() {

        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);

        Log.d(TAG,"Starting activity for Choosing Account");
        startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            // Display a dialog showing the connection error
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                            connectionStatusCode, GPhotosActivity.this,
                            REQUEST_GOOGLE_PLAY_SERVICES);
                    dialog.show();
                }
            });
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }


    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    accountName =data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    fetchAuthToken();
                }else if (resultCode == RESULT_CANCELED){
                    Log.d(TAG,"Google account unspecified");


                }
                break;
            case REQUEST_AUTHORIZATION:
                if(resultCode == RESULT_OK) {
                    // We had to sign in - now we can finish off the token request.
                    fetchAuthToken();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void fetchAuthToken() {
        if (accountName != null) {
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(PREF_ACCOUNT_NAME, accountName);
            editor.commit();

            if (isDeviceOnline()) {
                new AsyncTask(){

                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try {
                            Log.d(TAG,"Requesting token for account: " +
                                    accountName);
                            googleAuthToken = GoogleAuthUtil.getToken(getApplicationContext(),
                                    accountName, GPHOTOS_SCOPE);

                            Log.d(TAG, "Received Token: " + googleAuthToken);
                            fetchAndShowImages();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        } catch (UserRecoverableAuthException e) {
                            startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                        } catch (GoogleAuthException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute();
            } else {
                Toast.makeText(this, "Device not online", Toast.LENGTH_LONG).show();
            }
        } else {
            chooseGoogleAccount();
        }
    }


    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


}

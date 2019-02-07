package com.rutgers.neemi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.RankBy;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.rutgers.neemi.model.GPSLocation;
import com.rutgers.neemi.model.Photo;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static android.app.Notification.VISIBILITY_PUBLIC;

class LocationResultHelper {
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";

    final private static String PRIMARY_CHANNEL = "default";


    private Context mContext;
    private List<Location> mLocations;
    private NotificationManager mNotificationManager;
    private DatabaseHelper helper;
    final RuntimeExceptionDao<GPSLocation, String> gpsDao;


    LocationResultHelper(Context context, List<Location> locations) {
        mContext = context;
        mLocations = locations;
        helper=DatabaseHelper.getHelper(mContext);
        gpsDao = helper.getGpsLocationtRuntimeDao();
    }

    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     */
    private String getLocationResultTitle() {
        String numLocationsReported = mContext.getResources().getQuantityString(com.rutgers.neemi.R.plurals.num_locations_reported, mLocations.size(), mLocations.size());
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    private String getLocationResultText() {
        if (mLocations.isEmpty()) {
            return mContext.getString(com.rutgers.neemi.R.string.unknown_location);
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : mLocations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(")");
            sb.append("\n");
            //save gps in sqlite db
            GPSLocation gpsLocation = new GPSLocation(System.currentTimeMillis(),location.getLatitude(),location.getLongitude());
            try {
               String placeName = getPlaceName(location.getLatitude(),location.getLongitude());
               sb.append(placeName);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ApiException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            gpsDao.create(gpsLocation);
        }
        return sb.toString();
    }

    /**
     * Saves location result as a string to {@link android.content.SharedPreferences}.
     */
    void saveResults() {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle() + "\n" +
                        getLocationResultText())
                .apply();

    }

    /**
     * Fetches location results from {@link android.content.SharedPreferences}.
     */
    static String getSavedLocationResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    /**
     * Get the notification mNotificationManager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    /**
     * Displays a notification with the location results.
     */
    void showNotification() {

        Intent notificationIntent = new Intent(mContext, LocationActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(LocationActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder notificationBuilder = new Notification.Builder(mContext,
                    PRIMARY_CHANNEL)
                    .setContentTitle(getLocationResultTitle())
                    .setContentText(getLocationResultText())
                    .setSmallIcon(com.rutgers.neemi.R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(notificationPendingIntent);
            getNotificationManager().notify(0, notificationBuilder.build());

        }else {


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                    .setContentTitle(getLocationResultTitle())
                    .setContentText(getLocationResultText())
                    .setSmallIcon(com.rutgers.neemi.R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setVisibility(VISIBILITY_PUBLIC)
                    .setContentIntent(notificationPendingIntent);
            getNotificationManager().notify(0, notificationBuilder.build());

        }

    }


    public String getPlaceName(double lat, double lon) throws InterruptedException, ApiException, IOException {
        LatLng location =  new LatLng(lat, lon);
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyDe8nWbXFA6ESFS6GnQtYPPsXzYmLz3Lf0")
                .build();

        PlacesSearchResponse gmapsResponse = PlacesApi.nearbySearchQuery(geoApiContext, location)
                .radius(100)
                .await();
        if (gmapsResponse.results != null) {
            if (gmapsResponse.results.length > 0) {
                return gmapsResponse.results[0].name;
            }
        }
        return null;

    }


}

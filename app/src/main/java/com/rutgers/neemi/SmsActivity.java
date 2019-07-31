package com.rutgers.neemi;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class SmsActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    static final int REQUEST_PERMISSION_GET_SMS=1111;

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

    public void requestPermissions(){

        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_SMS)) {
            getAllSmsMms(getApplicationContext());
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs permission to access your device SMS",
                    REQUEST_PERMISSION_GET_SMS,
                    android.Manifest.permission.READ_SMS);
        }


    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putBoolean("sms", true).apply();


    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }


    public void getAllSmsMms(Context context) {

        ContentResolver cr = context.getContentResolver();

        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    String thread_id = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID));
                    String subject = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SUBJECT));
                    String senderOfMessage = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.CREATOR));
                    String senderOfConversation = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.PERSON));

                    System.err.println("Body = " +body);
                    Date dateFormat= new Date(Long.valueOf(smsDate));

                    System.err.println("Date = " +dateFormat.toString());
                    System.err.println("Address = " +number);
                    System.err.println("Thread_id = " +thread_id);
                    System.err.println("Subject = " +subject);
                    System.err.println("Creator = " +senderOfMessage);
                    System.err.println("Person = " +senderOfConversation);


                    String type;
                    switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                        case Telephony.Sms.MESSAGE_TYPE_INBOX:
                            type = "inbox";
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_SENT:
                            type = "sent";
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                            type = "outbox";
                            break;
                        default:
                            break;
                    }


                    c.moveToNext();
                }
            }

            c.close();

        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }

    }




}

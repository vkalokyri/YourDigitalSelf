package com.rutgers.neemi;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.rutgers.neemi.model.Message;
import com.rutgers.neemi.model.Person;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

import static pub.devrel.easypermissions.EasyPermissions.hasPermissions;

public class SmsActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    static final int REQUEST_PERMISSION_GET_SMS=1111;
    ProgressDialog mProgress;
    String frequency;
    DatabaseHelper helper;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Getting your text messages. Please wait ...");

        frequency = PreferenceManager.getDefaultSharedPreferences(this).getString("sync_frequency", "");
        helper=DatabaseHelper.getHelper(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        requestPermissions();


    }

    public void requestPermissions(){

        String[] permissions = {Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS};

        if(!hasPermissions(this, permissions)){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_GET_SMS);
        }else{
            mProgress.show();
            getAllSmsMms(getApplicationContext());

        }



//        if (hasPermissions(this, Manifest.permission.READ_SMS) && hasPermissions(this, Manifest.permission.READ_CONTACTS)) {
//            mProgress.show();
//            getAllSmsMms(getApplicationContext());
//        } else {
//            EasyPermissions.requestPermissions(
//                    this,
//                    "This app needs permission to access your device SMS",
//                    REQUEST_PERMISSION_GET_SMS,
//                    android.Manifest.permission.READ_SMS);
//            EasyPermissions.requestPermissions(this,
//                    "This app needs permission to access your device contacts",
//                    PERMISSION_REQUEST_CONTACT,
//                    Manifest.permission.READ_CONTACTS);
//
//        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putBoolean("sms", true).apply();
        mProgress.show();
        getAllSmsMms(getApplicationContext());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_GET_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mProgress.show();
                    getAllSmsMms(getApplicationContext());
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    public Calendar getCalendarDate(String period){
        Calendar cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());

        if (period.equals("7")){
            cal.add(Calendar.DATE, -7);
        }else if(period.equals("30")){
            cal.add(Calendar.MONTH, -1);
        }else if(period.equals("180")){
            cal.add(Calendar.MONTH, -3);
        }else if(period.equals("365")){
            cal.add(Calendar.MONTH, -12);
        }else if(period.equals("1")){
            cal.add(Calendar.DATE, -1);
        }
        cal.add(Calendar.MONTH, -1);
        return cal;
    }


    public void getAllSmsMms(Context context) {

        Calendar since = getCalendarDate(frequency);
        Calendar now = Calendar.getInstance(Calendar.getInstance().getTimeZone());

        ContentResolver cr = context.getContentResolver();

        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, Telephony.Sms.DATE + ">=? and "+Telephony.Sms.DATE +"<=?",
                new String[]{"" + since.getTimeInMillis(),""+now.getTimeInMillis()}, null);
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    Message sms = new Message();

                    String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String name = getContactName(number,context);
                    String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    String thread_id = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID));
                    String subject = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SUBJECT));
                    String senderOfMessage = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.CREATOR));
                    String senderOfConversation = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.PERSON));

                    sms.setContent(body);
                    sms.setTimestamp(Long.valueOf(smsDate));
                    Person p = helper.personExistsByName(name);
                    if(p==null){
                        p = new Person(number, getContactName(number,context),null);
                        helper.getPersonDao().create(p);
                    }
                    sms.setFrom(p);
                    sms.setThread(thread_id);
                    sms.setSource("sms");
                    System.err.println("Body = " +body);
                    System.err.println("Date = " +sms.getContentDate());
                    System.err.println("Address = " +number);
                    System.err.println("Name = " +name);
                    System.err.println("Thread_id = " +thread_id);
                    System.err.println("Subject = " +subject);
                    System.err.println("Creator = " +senderOfMessage);
                    System.err.println("Person = " +senderOfConversation);


//                    String type;
//                    switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
//                        case Telephony.Sms.MESSAGE_TYPE_INBOX:
//                            type = "inbox";
//                            break;
//                        case Telephony.Sms.MESSAGE_TYPE_SENT:
//                            type = "sent";
//                            break;
//                        case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
//                            type = "outbox";
//                            break;
//                        default:
//                            break;
//                    }


                    c.moveToNext();

                    helper.getMessageDao().create(sms);
                }
            }

            c.close();
            loadEmailIndex(totalSMS);

            new ExtractTimeTask().execute();
            mProgress.dismiss();
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.putExtra("key", "sms");
            myIntent.putExtra("items", totalSMS);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);

        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }

    }

    public String getContactName(final String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
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


    public class ExtractTimeTask extends AsyncTask<Void, Void, Boolean> {

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
                Log.d("SMS_ACTIVITY","All dates have been extracted");
            } else {
                Log.e("SMS_ACTIVITY","There was an error while extracting message dates");
            }
        }


        private boolean extractMessageTime() {

            RuntimeExceptionDao<Message, String> messageDao = helper.getMessageDao();
            List<Message> results = null;

            QueryBuilder<Message, String> queryBuilder = messageDao.queryBuilder();

            Where<Message, String> where = queryBuilder.where();
            try {
                where.eq("source", "sms");
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

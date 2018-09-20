package com.rutgers.neemi.rest;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.rutgers.neemi.DatabaseHelper;
import com.rutgers.neemi.GmailActivity;
import com.rutgers.neemi.MainActivity;
import com.rutgers.neemi.R;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.EmailBcc;
import com.rutgers.neemi.model.EmailCc;
import com.rutgers.neemi.model.EmailTo;
import com.rutgers.neemi.model.Person;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DownloadJobService extends JobService{

    private static final String TAG = DownloadJobService.class.getSimpleName();
    DatabaseHelper dbHelper;
    JobParameters params;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper= DatabaseHelper.getHelper(this);
        Log.i(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }



    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        params = jobParameters;

        new MakeRequestTask(GmailActivity.mCredential).execute();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }


    public class MakeRequestTask extends AsyncTask<Void, Void, Integer> {

        private com.google.api.services.gmail.Gmail gmailService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
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
                //return 0;
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
        public int getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            RuntimeExceptionDao<Email, String> emailDao = dbHelper.getEmailDao();
            RuntimeExceptionDao<EmailTo, String> emailToDao = dbHelper.getEmailToDao();
            RuntimeExceptionDao<EmailCc, String> emailCcDao = dbHelper.getEmailCcDao();
            RuntimeExceptionDao<EmailBcc, String> emailBccDao = dbHelper.getEmailBccDao();
            RuntimeExceptionDao<Person, String> personDao = dbHelper.getPersonDao();



            String user = "me";
            int totalItemsInserted = 0;
            String pageToken = null;
            Calendar cal = Calendar.getInstance(Calendar.getInstance().getTimeZone());
            //cal.add(Calendar.MONTH, -3); // substract 6 months
            cal.add(Calendar.DATE, -1); // substract 1 day

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

           // mProgress.setMax(messages.size());

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
                    //mProgress.setProgress(totalItemsInserted);
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
           // mProgress.show();
        }

        @Override
        protected void onPostExecute(Integer output) {
           // mProgress.dismiss();
            new ExtractTimeTask().execute();

            jobFinished(params,false);


            // Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
//            myIntent.putExtra("key", "gmail");
//            myIntent.putExtra("items", output);
//            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(myIntent);




            if (output == 0) {
                //Snackbar.make(findViewById(R.id.gmailCoordinatorLayout), "No emails fetched.", Snackbar.LENGTH_LONG ).show();
            } else {
               // Snackbar.make(findViewById(R.id.gmailCoordinatorLayout), output+" emails fetched.", Snackbar.LENGTH_LONG ).show();
            }


        }

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




}

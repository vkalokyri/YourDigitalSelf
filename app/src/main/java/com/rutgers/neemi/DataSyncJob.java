package com.rutgers.neemi;

import android.accounts.Account;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.rutgers.neemi.model.Email;
import com.rutgers.neemi.model.EmailBcc;
import com.rutgers.neemi.model.EmailCc;
import com.rutgers.neemi.model.EmailTo;
import com.rutgers.neemi.model.Person;
import com.rutgers.neemi.rest.GcalService;
import com.rutgers.neemi.rest.GmailService;
import com.rutgers.neemi.rest.InstagramService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.rutgers.neemi.GmailActivity.PREF_ACCOUNT_NAME;
import static com.rutgers.neemi.GmailActivity.mCredential;


public class DataSyncJob extends Job {

    public static final String TAG = "job_syncData_tag";



    @Override
    @NonNull
    protected Result onRunJob(Params params) {

        GmailService gmailService = new GmailService(getContext());
        GcalService gcalService = new GcalService(getContext());
        InstagramService instagramService = new InstagramService(getContext());



        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(DataSyncJob.TAG)
                .setExecutionWindow(30_000L, 40_000L)
                .build()
                .schedule();
    }


    public static void scheduleAdvancedJob() {
//        PersistableBundleCompat extras = new PersistableBundleCompat();
//        extras.putString("key", "Hello world");

        int jobId = new JobRequest.Builder(DataSyncJob.TAG)
                .setExecutionWindow(30_000L, 40_000L)
                .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                .setRequiresCharging(true)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
               // .setExtras(extras)
                .setRequirementsEnforced(true)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

    public static void schedulePeriodicJob() {
        int jobId = new JobRequest.Builder(DataSyncJob.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(5))
                .build()
                .schedule();
    }

    private void scheduleExactJob() {
        int jobId = new JobRequest.Builder(DataSyncJob.TAG)
                .setExact(20_000L)
                .build()
                .schedule();
    }

    private void runJobImmediately() {
        int jobId = new JobRequest.Builder(DataSyncJob.TAG)
                .startNow()
                .build()
                .schedule();
    }

    private void cancelJob(int jobId) {
        JobManager.instance().cancel(jobId);
    }


    }

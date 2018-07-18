package com.rutgers.neemi.rest;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.support.design.widget.Snackbar;

import com.rutgers.neemi.GmailActivity;
import com.rutgers.neemi.R;

public class DownloadJobService extends JobService{

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}

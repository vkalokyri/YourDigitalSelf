package com.rutgers.neemi.model;

public class Report {


    public boolean isEatingOut;
    public String who;
    public String when;
    public boolean where;
    public String what;

    public Report() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Report(boolean isEatingOut, String who, String when, boolean where, String what) {
        this.isEatingOut = isEatingOut;
        this.who = who;
        this.when = when;
        this.where = where;
        this.what = what;
    }
}
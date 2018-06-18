package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by suitcase on 6/25/17.
 */

@DatabaseTable(tableName = "Email")
public class Email implements Serializable{

        @DatabaseField(generatedId = true)
        int _id;
        @DatabaseField
        long timestamp;
        @DatabaseField
        String id;
        @DatabaseField
        String threadId;
        @DatabaseField
        BigInteger historyId;
        @DatabaseField (foreign = true,foreignAutoRefresh = true)
        Person from;
        @DatabaseField
        Date date;
        @DatabaseField
        String textContent;
        @DatabaseField
        String htmlContent;
        @DatabaseField
        String subject;
        @DatabaseField
        boolean hasAttachments;
        @DatabaseField
        Date subjectDate;
        @DatabaseField
        Date bodyDate;


        ArrayList<Person> to = new ArrayList<>();
        ArrayList<Person> cc= new ArrayList<>();
        ArrayList<Person> bcc= new ArrayList<>();




    public Email() {
            // ORMLite needs a no-arg constructor
        }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public BigInteger getHistoryId() {
        return historyId;
    }

    public void setHistoryId(BigInteger historyId) {
        this.historyId = historyId;
    }

    public Person getFrom() {
        return from;
    }

    public void setFrom(Person from) {
        this.from = from;
    }

    public ArrayList<Person> getTo() {
        return to;
    }

    public void setTo(ArrayList<Person> to) {
        this.to = to;
    }

    public ArrayList<Person> getCc() {
        return cc;
    }

    public void setCc(ArrayList<Person> cc) {
        this.cc = cc;
    }

    public ArrayList<Person> getBcc() {
        return bcc;
    }

    public void setBcc(ArrayList<Person> bcc) {
        this.bcc = bcc;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String content) {
        this.textContent = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean hasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getSubjectDate() {
        return subjectDate;
    }

    public void setSubjectDate(Date subjectDate) {
        this.subjectDate = subjectDate;
    }

    public Date getBodyDate() {
        return bodyDate;
    }

    public void setBodyDate(Date bodyDate) {
        this.bodyDate = bodyDate;
    }
}
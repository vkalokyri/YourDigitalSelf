package com.rutgers.neemi.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.math.BigInteger;
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
        @DatabaseField
        String from;
        @DatabaseField
        String to;
        @DatabaseField
        String cc;
        @DatabaseField
        String bcc;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
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
        this.textContent = textContent;
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
}
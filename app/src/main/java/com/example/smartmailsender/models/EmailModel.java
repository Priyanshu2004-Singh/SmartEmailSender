package com.example.smartmailsender.models;

public class EmailModel {

    private long id;
    private String recipient;
    private String subject;
    private String message;
    private String date;
    private String status;

    public EmailModel() {
    }

    public EmailModel(long id, String recipient, String subject, String message, String date, String status) {
        this.id = id;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.date = date;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

package com.example.smartmailsender.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartmailsender.models.EmailModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_mail_sender.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_HISTORY = "EmailHistory";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_RECIPIENT = "recipient";
    public static final String COLUMN_SUBJECT = "subject";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_STATUS = "status";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_DRAFT = "DRAFT";

    private static final String DB_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DISPLAY_DATE_PATTERN = "dd MMM yyyy, hh:mm a";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_RECIPIENT + " TEXT, "
                + COLUMN_SUBJECT + " TEXT, "
                + COLUMN_MESSAGE + " TEXT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_STATUS + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public long insertSentEmail(EmailModel model) {
        return insertEmail(model, STATUS_SENT);
    }

    public long insertDraft(EmailModel model) {
        return insertEmail(model, STATUS_DRAFT);
    }

    public int updateDraft(long id, EmailModel model) {
        return updateEmail(id, model, STATUS_DRAFT);
    }

    public int updateEmailStatus(long id, String status) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);
        return database.update(TABLE_HISTORY, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public long insertEmail(EmailModel model) {
        return insertSentEmail(model);
    }

    public List<EmailModel> getSentHistory() {
        return queryEmails(COLUMN_STATUS + "=?", new String[]{STATUS_SENT}, COLUMN_ID + " DESC");
    }

    public List<EmailModel> getDrafts() {
        return queryEmails(COLUMN_STATUS + "=?", new String[]{STATUS_DRAFT}, COLUMN_ID + " DESC");
    }

    public List<EmailModel> searchSentHistory(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getSentHistory();
        }
        String like = "%" + query.trim() + "%";
        return queryEmails(COLUMN_STATUS + "=? AND (" + COLUMN_RECIPIENT + " LIKE ? OR " + COLUMN_SUBJECT + " LIKE ? OR " + COLUMN_MESSAGE + " LIKE ?)",
                new String[]{STATUS_SENT, like, like, like}, COLUMN_ID + " DESC");
    }

    public List<EmailModel> filterSentHistoryByDate(String datePrefix) {
        if (datePrefix == null || datePrefix.trim().isEmpty()) {
            return getSentHistory();
        }
        return queryEmails(COLUMN_STATUS + "=? AND " + COLUMN_DATE + " LIKE ?", new String[]{STATUS_SENT, datePrefix.trim() + "%"}, COLUMN_ID + " DESC");
    }

    public List<String> getRecentRecipients() {
        List<EmailModel> sentHistory = getSentHistory();
        Set<String> recipients = new LinkedHashSet<>();
        for (EmailModel emailModel : sentHistory) {
            if (emailModel.getRecipient() != null && !emailModel.getRecipient().trim().isEmpty()) {
                String[] splitRecipients = emailModel.getRecipient().split(",");
                Collections.addAll(recipients, splitRecipients);
            }
            if (recipients.size() >= 8) {
                break;
            }
        }
        List<String> recentRecipients = new ArrayList<>();
        for (String recipient : recipients) {
            String trimmed = recipient.trim();
            if (!trimmed.isEmpty()) {
                recentRecipients.add(trimmed);
            }
        }
        return recentRecipients;
    }

    public EmailModel getLatestDraft() {
        List<EmailModel> drafts = getDrafts();
        return drafts.isEmpty() ? null : drafts.get(0);
    }

    public EmailModel getLastSent() {
        List<EmailModel> sentHistory = getSentHistory();
        return sentHistory.isEmpty() ? null : sentHistory.get(0);
    }

    public int getTotalSentCount() {
        return getCount(COLUMN_STATUS + "=?", new String[]{STATUS_SENT});
    }

    public int getDraftCount() {
        return getCount(COLUMN_STATUS + "=?", new String[]{STATUS_DRAFT});
    }

    public int getEmailsSentTodayCount() {
        return getCount(COLUMN_STATUS + "=? AND " + COLUMN_DATE + " LIKE ?", new String[]{STATUS_SENT, getTodayPrefix() + "%"});
    }

    public String getMostContactedRecipient() {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT " + COLUMN_RECIPIENT + ", COUNT(*) AS total FROM " + TABLE_HISTORY +
                        " WHERE " + COLUMN_STATUS + "=? AND " + COLUMN_RECIPIENT + " IS NOT NULL AND TRIM(" + COLUMN_RECIPIENT + ") <> ''" +
                        " GROUP BY " + COLUMN_RECIPIENT +
                        " ORDER BY total DESC, " + COLUMN_ID + " DESC LIMIT 1",
                new String[]{STATUS_SENT});
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        return "-";
    }

    public long insertDraftOrSent(boolean asDraft, EmailModel model) {
        return asDraft ? insertDraft(model) : insertSentEmail(model);
    }

    public int deleteEmail(long id) {
        SQLiteDatabase database = getWritableDatabase();
        return database.delete(TABLE_HISTORY, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteDraft(long id) {
        return deleteEmail(id);
    }

    public String formatDisplayDate(String storedDate) {
        if (storedDate == null || storedDate.trim().isEmpty()) {
            return "";
        }
        try {
            Date parsedDate = new SimpleDateFormat(DB_DATE_PATTERN, Locale.getDefault()).parse(storedDate);
            if (parsedDate == null) {
                return storedDate;
            }
            return new SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.getDefault()).format(parsedDate);
        } catch (ParseException exception) {
            return storedDate;
        }
    }

    public String getTodayPrefix() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public String getCurrentDateTime() {
        return new SimpleDateFormat(DB_DATE_PATTERN, Locale.getDefault()).format(new Date());
    }

    private long insertEmail(EmailModel model, String status) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RECIPIENT, model.getRecipient());
        values.put(COLUMN_SUBJECT, model.getSubject());
        values.put(COLUMN_MESSAGE, model.getMessage());
        values.put(COLUMN_DATE, normalizeDate(model.getDate()));
        values.put(COLUMN_STATUS, status);
        return database.insert(TABLE_HISTORY, null, values);
    }

    private int updateEmail(long id, EmailModel model, String status) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RECIPIENT, model.getRecipient());
        values.put(COLUMN_SUBJECT, model.getSubject());
        values.put(COLUMN_MESSAGE, model.getMessage());
        values.put(COLUMN_DATE, normalizeDate(model.getDate()));
        values.put(COLUMN_STATUS, status);
        return database.update(TABLE_HISTORY, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    private List<EmailModel> queryEmails(String selection, String[] selectionArgs, String orderBy) {
        List<EmailModel> history = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_HISTORY, null, selection, selectionArgs, null, null, orderBy);
        try {
            if (cursor.moveToFirst()) {
                do {
                    history.add(fromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return history;
    }

    private int getCount(String selection, String[] selectionArgs) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_HISTORY, new String[]{COLUMN_ID}, selection, selectionArgs, null, null, null);
        try {
            return cursor.getCount();
        } finally {
            cursor.close();
        }
    }

    private EmailModel fromCursor(Cursor cursor) {
        EmailModel model = new EmailModel();
        model.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        model.setRecipient(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPIENT)));
        model.setSubject(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT)));
        model.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
        model.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
        model.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
        return model;
    }

    private String normalizeDate(String dateValue) {
        if (dateValue == null || dateValue.trim().isEmpty()) {
            return getCurrentDateTime();
        }
        return dateValue.trim();
    }
}

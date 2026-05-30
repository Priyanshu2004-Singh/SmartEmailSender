package com.example.smartmailsender.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class EmailValidator {

    private EmailValidator() {
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static String[] splitEmails(String value) {
        if (TextUtils.isEmpty(value)) {
            return new String[0];
        }

        String[] rawItems = value.split("[,;]");
        List<String> normalizedItems = new ArrayList<>();
        for (String rawItem : rawItems) {
            String trimmed = rawItem.trim();
            if (!trimmed.isEmpty()) {
                normalizedItems.add(trimmed);
            }
        }
        return normalizedItems.toArray(new String[0]);
    }

    public static boolean areValidEmails(String value) {
        String[] items = splitEmails(value);
        if (items.length == 0) {
            return false;
        }

        for (String item : items) {
            if (!isValidEmail(item)) {
                return false;
            }
        }
        return true;
    }

    public static String normalizeEmailList(String value) {
        String[] items = splitEmails(value);
        if (items.length == 0) {
            return "";
        }
        return TextUtils.join(", ", Arrays.asList(items));
    }
}

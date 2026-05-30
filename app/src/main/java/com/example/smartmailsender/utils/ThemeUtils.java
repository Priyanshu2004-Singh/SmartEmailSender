package com.example.smartmailsender.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeUtils {

    public static final String PREFS_NAME = "smart_mail_sender_prefs";
    public static final String KEY_DARK_MODE = "key_dark_mode";
    public static final String KEY_SIGNATURE = "key_signature";
    public static final String KEY_THEME_MODE = "key_theme_mode";

    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    private ThemeUtils() {
    }

    public static void applySavedTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int themeMode = preferences.getInt(KEY_THEME_MODE, THEME_SYSTEM);
        boolean darkModeEnabled = preferences.getBoolean(KEY_DARK_MODE, false);

        if (themeMode == THEME_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            return;
        }
        if (themeMode == THEME_DARK || darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            return;
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static boolean isDarkModeEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }
}

package com.example.smartmailsender;

import android.app.Application;

import com.google.android.material.color.DynamicColors;
import com.example.smartmailsender.utils.ThemeUtils;

public class SmartMailSenderApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
        ThemeUtils.applySavedTheme(this);
    }
}

package com.example.smartmailsender.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartmailsender.R;
import com.example.smartmailsender.databinding.ActivitySplashBinding;
import com.example.smartmailsender.utils.ThemeUtils;

public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ActivitySplashBinding binding;
    private final Runnable navigateRunnable = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.splashAnimation.playAnimation();
        handler.postDelayed(navigateRunnable, 2000);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateRunnable);
        super.onDestroy();
    }
}

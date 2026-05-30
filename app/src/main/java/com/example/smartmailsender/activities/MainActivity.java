package com.example.smartmailsender.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.smartmailsender.R;
import com.example.smartmailsender.utils.ThemeUtils;
import com.example.smartmailsender.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.example.smartmailsender.fragments.AboutFragment;
import com.example.smartmailsender.fragments.ComposeFragment;
import com.example.smartmailsender.fragments.DraftsFragment;
import com.example.smartmailsender.fragments.HistoryFragment;
import com.example.smartmailsender.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_DESTINATION = "extra_destination";
    public static final String EXTRA_DRAFT_ID = "extra_draft_id";
    public static final String EXTRA_DRAFT_RECIPIENT = "extra_draft_recipient";
    public static final String EXTRA_DRAFT_SUBJECT = "extra_draft_subject";
    public static final String EXTRA_DRAFT_MESSAGE = "extra_draft_message";
    public static final String EXTRA_DRAFT_DATE = "extra_draft_date";
    public static final String EXTRA_DRAFT_STATUS = "extra_draft_status";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar.toolbar);
        setupDrawer();
        setupNavigation();

        if (savedInstanceState == null) {
            int destination = getIntent().getIntExtra(EXTRA_DESTINATION, R.id.nav_compose);
            navigateTo(destination, false);
        }
    }

    private void setupDrawer() {
        NavigationView navigationView = binding.navigationView;
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(item -> {
            navigateTo(item.getItemId(), true);
            return true;
        });
    }

    private void setupNavigation() {
        binding.topAppBar.toolbar.setNavigationOnClickListener(v -> toggleDrawer());
        binding.topAppBar.toolbar.inflateMenu(R.menu.top_app_bar_menu);
        binding.topAppBar.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                navigateTo(R.id.nav_settings, false);
                return true;
            }
            return false;
        });
    }

    public void openComposeWithDraft(long draftId, String recipient, String subject, String message, String date, String status) {
        ComposeFragment fragment = ComposeFragment.newInstance(draftId, recipient, subject, message, date, status);
        replaceFragment(fragment, ComposeFragment.TAG);
        binding.navigationView.setCheckedItem(R.id.nav_compose);
    }

    private void navigateTo(int menuId, boolean closeDrawer) {
        Fragment fragment;
        String tag;
        String title;

        if (menuId == R.id.nav_drafts) {
            fragment = new DraftsFragment();
            tag = DraftsFragment.TAG;
            title = getString(R.string.nav_drafts);
        } else if (menuId == R.id.nav_history) {
            fragment = new HistoryFragment();
            tag = HistoryFragment.TAG;
            title = getString(R.string.nav_history);
        } else if (menuId == R.id.nav_settings) {
            fragment = new SettingsFragment();
            tag = SettingsFragment.TAG;
            title = getString(R.string.nav_settings);
        } else if (menuId == R.id.nav_about) {
            fragment = new AboutFragment();
            tag = AboutFragment.TAG;
            title = getString(R.string.nav_about);
        } else {
            fragment = new ComposeFragment();
            tag = ComposeFragment.TAG;
            title = getString(R.string.nav_compose);
            menuId = R.id.nav_compose;
        }

        replaceFragment(fragment, tag);
        binding.topAppBar.toolbar.setTitle(title);
        binding.navigationView.setCheckedItem(menuId);

        if (closeDrawer) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void openDrawer() {
        toggleDrawer();
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_left);
        transaction.replace(R.id.fragmentContainer, fragment, tag);
        transaction.commit();
    }

    private void toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }
}

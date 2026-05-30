package com.example.smartmailsender.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.smartmailsender.BuildConfig;
import com.example.smartmailsender.R;
import com.example.smartmailsender.databinding.FragmentSettingsBinding;
import com.example.smartmailsender.utils.ThemeUtils;

public class SettingsFragment extends Fragment {

    public static final String TAG = "SettingsFragment";

    private FragmentSettingsBinding binding;
    private SharedPreferences preferences;
    private boolean isBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferences = requireContext().getSharedPreferences(ThemeUtils.PREFS_NAME, 0);
        loadPreferences();
        setupListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadPreferences() {
        isBinding = true;
        int themeMode = preferences.getInt(ThemeUtils.KEY_THEME_MODE, ThemeUtils.THEME_SYSTEM);
        boolean darkMode = preferences.getBoolean(ThemeUtils.KEY_DARK_MODE, false);
        binding.darkModeSwitch.setChecked(darkMode || themeMode == ThemeUtils.THEME_DARK);
        binding.signatureEditText.setText(preferences.getString(ThemeUtils.KEY_SIGNATURE, ""));
        updateThemeSelector(themeMode);
        binding.versionValue.setText(BuildConfig.VERSION_NAME);
        isBinding = false;
    }

    private void setupListeners() {
        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isBinding) {
                return;
            }
            int mode = isChecked ? ThemeUtils.THEME_DARK : ThemeUtils.THEME_LIGHT;
            saveThemeMode(mode);
        });

        binding.themeSelector.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isBinding || !isChecked) {
                return;
            }
            if (checkedId == R.id.themeSystemButton) {
                saveThemeMode(ThemeUtils.THEME_SYSTEM);
            } else if (checkedId == R.id.themeLightButton) {
                saveThemeMode(ThemeUtils.THEME_LIGHT);
            } else if (checkedId == R.id.themeDarkButton) {
                saveThemeMode(ThemeUtils.THEME_DARK);
            }
        });

        binding.signatureEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                preferences.edit().putString(ThemeUtils.KEY_SIGNATURE, s == null ? "" : s.toString().trim()).apply();
            }
        });

        binding.saveSettingsButton.setOnClickListener(v -> {
            preferences.edit()
                    .putBoolean(ThemeUtils.KEY_DARK_MODE, binding.darkModeSwitch.isChecked())
                    .putString(ThemeUtils.KEY_SIGNATURE, binding.signatureEditText.getText() == null ? "" : binding.signatureEditText.getText().toString().trim())
                    .apply();
            ThemeUtils.applySavedTheme(requireContext());
            requireActivity().recreate();
        });
    }

    private void saveThemeMode(int mode) {
        preferences.edit()
                .putInt(ThemeUtils.KEY_THEME_MODE, mode)
                .putBoolean(ThemeUtils.KEY_DARK_MODE, mode == ThemeUtils.THEME_DARK)
                .apply();
        ThemeUtils.applySavedTheme(requireContext());
        requireActivity().recreate();
    }

    private void updateThemeSelector(int mode) {
        if (mode == ThemeUtils.THEME_LIGHT) {
            binding.themeSelector.check(R.id.themeLightButton);
        } else if (mode == ThemeUtils.THEME_DARK) {
            binding.themeSelector.check(R.id.themeDarkButton);
        } else {
            binding.themeSelector.check(R.id.themeSystemButton);
        }
    }
}

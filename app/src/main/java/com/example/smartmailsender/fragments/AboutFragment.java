package com.example.smartmailsender.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartmailsender.BuildConfig;
import com.example.smartmailsender.R;
import com.example.smartmailsender.databinding.FragmentAboutBinding;

public class AboutFragment extends Fragment {

    public static final String TAG = "AboutFragment";

    private FragmentAboutBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.versionValue.setText(BuildConfig.VERSION_NAME);
        binding.aboutDescription.setText(getString(R.string.about_app_description));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

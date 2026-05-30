package com.example.smartmailsender.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmailsender.R;
import com.example.smartmailsender.activities.MainActivity;
import com.example.smartmailsender.adapters.DraftAdapter;
import com.example.smartmailsender.database.DBHelper;
import com.example.smartmailsender.databinding.FragmentDraftsBinding;
import com.example.smartmailsender.models.EmailModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class DraftsFragment extends Fragment implements DraftAdapter.DraftActionListener {

    public static final String TAG = "DraftsFragment";

    private FragmentDraftsBinding binding;
    private DBHelper dbHelper;
    private DraftAdapter adapter;
    private final List<EmailModel> draftItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDraftsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DBHelper(requireContext());
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).openDrawer();
            }
        });
        setupRecyclerView();
        loadDrafts();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDrafts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        binding.draftsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DraftAdapter(requireContext(), draftItems, this);
        binding.draftsRecyclerView.setAdapter(adapter);
    }

    private void loadDrafts() {
        draftItems.clear();
        draftItems.addAll(dbHelper.getDrafts());
        adapter.notifyDataSetChanged();

        boolean isEmpty = draftItems.isEmpty();
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.draftsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.draftCountValue.setText(String.valueOf(draftItems.size()));
    }

    @Override
    public void onEditDraft(EmailModel emailModel) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).openComposeWithDraft(emailModel.getId(), emailModel.getRecipient(), emailModel.getSubject(), emailModel.getMessage(), emailModel.getDate(), emailModel.getStatus());
        }
    }

    @Override
    public void onDeleteDraft(EmailModel emailModel) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_record_title)
                .setMessage(R.string.delete_record_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dbHelper.deleteDraft(emailModel.getId());
                    loadDrafts();
                    Snackbar.make(binding.getRoot(), R.string.record_deleted_message, Snackbar.LENGTH_LONG).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}

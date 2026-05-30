package com.example.smartmailsender.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartmailsender.R;
import com.example.smartmailsender.activities.MainActivity;
import com.example.smartmailsender.adapters.HistoryAdapter;
import com.example.smartmailsender.database.DBHelper;
import com.example.smartmailsender.databinding.FragmentHistoryBinding;
import com.example.smartmailsender.models.EmailModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment implements HistoryAdapter.OnDeleteClickListener {

    public static final String TAG = "HistoryFragment";

    private FragmentHistoryBinding binding;
    private DBHelper dbHelper;
    private HistoryAdapter adapter;
    private final List<EmailModel> historyItems = new ArrayList<>();
    private String currentQuery = "";
    private String currentDatePrefix = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
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
        setupSearchAndFilter();
        setupSwipeToDelete();
        loadHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.historyRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new HistoryAdapter(requireContext(), historyItems, this);
        binding.historyRecyclerView.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        binding.searchEditText.addTextChangedListener(new SimpleTextWatcher(text -> {
            currentQuery = text;
            loadHistory();
        }));
        binding.dateFilterChip.setOnClickListener(v -> showDatePicker());
        binding.clearFilterChip.setOnClickListener(v -> {
            currentDatePrefix = "";
            binding.dateFilterChip.setText(getString(R.string.filter_by_date));
            loadHistory();
        });
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position >= 0 && position < historyItems.size()) {
                    EmailModel emailModel = historyItems.get(position);
                    dbHelper.deleteEmail(emailModel.getId());
                    loadHistory();
                    Snackbar.make(binding.getRoot(), R.string.record_deleted_message, Snackbar.LENGTH_LONG).show();
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(binding.historyRecyclerView);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.filter_by_date))
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) {
                return;
            }
            Date selectedDate = new Date(selection);
            currentDatePrefix = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate);
            binding.dateFilterChip.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate));
            loadHistory();
        });
        picker.show(getParentFragmentManager(), "history_date_picker");
    }

    private void loadHistory() {
        historyItems.clear();
        List<EmailModel> source = dbHelper.getSentHistory();
        for (EmailModel emailModel : source) {
            boolean matchesQuery = currentQuery.isEmpty()
                    || safeContains(emailModel.getRecipient(), currentQuery)
                    || safeContains(emailModel.getSubject(), currentQuery)
                    || safeContains(emailModel.getMessage(), currentQuery);
            boolean matchesDate = currentDatePrefix.isEmpty() || (emailModel.getDate() != null && emailModel.getDate().startsWith(currentDatePrefix));
            if (matchesQuery && matchesDate) {
                historyItems.add(emailModel);
            }
        }
        adapter.notifyDataSetChanged();

        boolean isEmpty = historyItems.isEmpty();
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.historyRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.historyCountValue.setText(String.valueOf(historyItems.size()));
    }

    @Override
    public void onDeleteClick(EmailModel emailModel) {
        dbHelper.deleteEmail(emailModel.getId());
        loadHistory();
    }

    private boolean safeContains(String value, String query) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()));
    }

    private static class SimpleTextWatcher implements android.text.TextWatcher {
        interface ChangeListener {
            void onChange(String value);
        }

        private final ChangeListener listener;

        SimpleTextWatcher(ChangeListener listener) {
            this.listener = listener;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            listener.onChange(s == null ? "" : s.toString().trim());
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
        }
    }
}

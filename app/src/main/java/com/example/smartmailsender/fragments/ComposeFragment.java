package com.example.smartmailsender.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartmailsender.R;
import com.example.smartmailsender.activities.MainActivity;
import com.example.smartmailsender.database.DBHelper;
import com.example.smartmailsender.databinding.FragmentComposeBinding;
import com.example.smartmailsender.models.EmailModel;
import com.example.smartmailsender.utils.EmailValidator;
import com.example.smartmailsender.utils.ThemeUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";

    private static final String ARG_DRAFT_ID = "arg_draft_id";
    private static final String ARG_RECIPIENT = "arg_recipient";
    private static final String ARG_SUBJECT = "arg_subject";
    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_DATE = "arg_date";
    private static final String ARG_STATUS = "arg_status";

    private FragmentComposeBinding binding;
    private DBHelper dbHelper;
    private ActivityResultLauncher<Intent> attachmentPickerLauncher;
    private Uri selectedAttachmentUri;
    private long editingDraftId = -1L;

    public static ComposeFragment newInstance(long draftId, String recipient, String subject, String message, String date, String status) {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DRAFT_ID, draftId);
        args.putString(ARG_RECIPIENT, recipient);
        args.putString(ARG_SUBJECT, subject);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_DATE, date);
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentComposeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attachmentPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedAttachmentUri = result.getData().getData();
                        if (selectedAttachmentUri != null) {
                            binding.attachmentChip.setVisibility(View.VISIBLE);
                            binding.attachmentChip.setText(selectedAttachmentUri.getLastPathSegment());
                        }
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DBHelper(requireContext());
        setupToolbar();
        setupDashboard();
        setupQuickActions();
        setupRecipientField();
        setupButtons();
        setupMessageCounter();
        restoreDraftIfNeeded();
        loadRecentRecipients();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardStats();
        loadRecentRecipients();
    }

    @Override
    public void onPause() {
        super.onPause();
        autosaveIfNeeded();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupToolbar() {
        binding.collapsingToolbar.setTitle(getString(R.string.nav_compose));
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).openDrawer();
            }
        });
    }

    private void setupDashboard() {
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        binding.totalSentValue.setText(String.valueOf(dbHelper.getTotalSentCount()));
        binding.draftsSavedValue.setText(String.valueOf(dbHelper.getDraftCount()));
        binding.sentTodayValue.setText(String.valueOf(dbHelper.getEmailsSentTodayCount()));

        EmailModel lastSent = dbHelper.getLastSent();
        binding.lastSentValue.setText(lastSent == null ? getString(R.string.no_activity_yet) : safeText(lastSent.getRecipient()));
        binding.mostContactedValue.setText(TextUtils.isEmpty(dbHelper.getMostContactedRecipient()) ? getString(R.string.no_activity_yet) : dbHelper.getMostContactedRecipient());
    }

    private void setupQuickActions() {
        binding.quickSignatureChip.setOnClickListener(v -> insertSignature());
        binding.quickFollowUpChip.setOnClickListener(v -> applyTemplate(getString(R.string.template_follow_up_subject), getString(R.string.template_follow_up_body)));
        binding.quickMeetingChip.setOnClickListener(v -> applyTemplate(getString(R.string.template_meeting_subject), getString(R.string.template_meeting_body)));
        binding.quickThankYouChip.setOnClickListener(v -> applyTemplate(getString(R.string.template_thank_you_subject), getString(R.string.template_thank_you_body)));
        binding.attachButton.setOnClickListener(v -> openAttachmentPicker());
        binding.templateButton.setOnClickListener(v -> applyTemplate(getString(R.string.template_follow_up_subject), getString(R.string.template_follow_up_body)));
    }

    private void setupRecipientField() {
        binding.recipientEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderRecipientChips(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        renderRecipientChips(getRecipientText());
    }

    private void setupButtons() {
        binding.sendFab.setOnClickListener(v -> {
            animatePress(v);
            sendEmail();
        });
        binding.saveDraftButton.setOnClickListener(v -> {
            animatePress(v);
            saveDraft();
        });
        binding.clearButton.setOnClickListener(v -> {
            animatePress(v);
            clearForm();
        });
        binding.recipientSuggestionButton.setOnClickListener(v -> loadRecentRecipients());
    }

    private void setupMessageCounter() {
        binding.messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateMessageHeight(s == null ? 0 : s.length());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        updateMessageHeight(getMessageText().length());
    }

    private void restoreDraftIfNeeded() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            editingDraftId = arguments.getLong(ARG_DRAFT_ID, -1L);
            String recipient = arguments.getString(ARG_RECIPIENT, "");
            String subject = arguments.getString(ARG_SUBJECT, "");
            String message = arguments.getString(ARG_MESSAGE, "");
            binding.recipientEditText.setText(recipient);
            binding.subjectEditText.setText(subject);
            binding.messageEditText.setText(message);
            renderRecipientChips(recipient);
            return;
        }

        EmailModel latestDraft = dbHelper.getLatestDraft();
        if (latestDraft != null) {
            editingDraftId = latestDraft.getId();
            binding.recipientEditText.setText(latestDraft.getRecipient());
            binding.subjectEditText.setText(latestDraft.getSubject());
            binding.messageEditText.setText(latestDraft.getMessage());
            renderRecipientChips(latestDraft.getRecipient());
        }
    }

    private void loadRecentRecipients() {
        binding.recentRecipientsGroup.removeAllViews();
        List<String> recentRecipients = dbHelper.getRecentRecipients();
        for (String recipient : recentRecipients) {
            Chip chip = new Chip(requireContext());
            chip.setText(recipient);
            chip.setCheckable(false);
            chip.setCloseIconVisible(false);
            chip.setOnClickListener(v -> addRecipient(recipient));
            binding.recentRecipientsGroup.addView(chip);
        }
    }

    private void renderRecipientChips(String input) {
        binding.recipientChipGroup.removeAllViews();
        String[] recipients = EmailValidator.splitEmails(input);
        for (String recipient : recipients) {
            Chip chip = new Chip(requireContext());
            chip.setText(recipient);
            chip.setCloseIconVisible(true);
            chip.setChipIconVisible(false);
            chip.setOnCloseIconClickListener(v -> removeRecipient(recipient));
            binding.recipientChipGroup.addView(chip);
        }
    }

    private void addRecipient(String recipient) {
        List<String> currentRecipients = new ArrayList<>(Arrays.asList(EmailValidator.splitEmails(getRecipientText())));
        if (!currentRecipients.contains(recipient)) {
            currentRecipients.add(recipient);
            binding.recipientEditText.setText(TextUtils.join(", ", currentRecipients));
        }
    }

    private void removeRecipient(String recipient) {
        List<String> currentRecipients = new ArrayList<>(Arrays.asList(EmailValidator.splitEmails(getRecipientText())));
        currentRecipients.remove(recipient);
        binding.recipientEditText.setText(TextUtils.join(", ", currentRecipients));
    }

    private void applyTemplate(String subject, String body) {
        binding.subjectEditText.setText(subject);
        binding.messageEditText.setText(body + "\n\n" + getString(R.string.template_footer));
        updateMessageHeight(getMessageText().length());
    }

    private void insertSignature() {
        String signature = requireContext().getSharedPreferences(ThemeUtils.PREFS_NAME, 0)
                .getString(ThemeUtils.KEY_SIGNATURE, "");
        if (!TextUtils.isEmpty(signature)) {
            String existing = getMessageText();
            String appended = existing.isEmpty() ? signature : existing + "\n\n--\n" + signature;
            binding.messageEditText.setText(appended);
            updateMessageHeight(appended.length());
        }
    }

    private void openAttachmentPicker() {
        Intent pickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pickerIntent.setType("*/*");
        attachmentPickerLauncher.launch(pickerIntent);
    }

    private void sendEmail() {
        if (!validateInputs()) {
            return;
        }

        String rawMessage = getRawMessageText();
        String finalMessage = appendSignature(rawMessage);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        String mimeType = selectedAttachmentUri == null ? "message/rfc822" : requireContext().getContentResolver().getType(selectedAttachmentUri);
        emailIntent.setType(mimeType == null ? "*/*" : mimeType);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, EmailValidator.splitEmails(getRecipientText()));

        String[] ccRecipients = EmailValidator.splitEmails(getCcText());
        String[] bccRecipients = EmailValidator.splitEmails(getBccText());
        if (ccRecipients.length > 0) {
            emailIntent.putExtra(Intent.EXTRA_CC, ccRecipients);
        }
        if (bccRecipients.length > 0) {
            emailIntent.putExtra(Intent.EXTRA_BCC, bccRecipients);
        }

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getSubjectText());
    emailIntent.putExtra(Intent.EXTRA_TEXT, finalMessage);

        if (selectedAttachmentUri != null) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, selectedAttachmentUri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_email_app)));
            dbHelper.insertSentEmail(new EmailModel(0L, EmailValidator.normalizeEmailList(getRecipientText()), getSubjectText(), getMessageText(), dbHelper.getCurrentDateTime(), DBHelper.STATUS_SENT));
            loadDashboardStats();
            showMessage(getString(R.string.send_success_message));
        } catch (ActivityNotFoundException exception) {
            showMessage(getString(R.string.no_email_app_error));
        }
    }

    private void saveDraft() {
        if (TextUtils.isEmpty(getRecipientText()) && TextUtils.isEmpty(getSubjectText()) && TextUtils.isEmpty(getRawMessageText())) {
            showMessage(getString(R.string.empty_draft_error));
            return;
        }

        EmailModel draft = new EmailModel(0L, EmailValidator.normalizeEmailList(getRecipientText()), getSubjectText(), getRawMessageText(), dbHelper.getCurrentDateTime(), DBHelper.STATUS_DRAFT);
        if (editingDraftId > 0) {
            dbHelper.updateDraft(editingDraftId, draft);
        } else {
            editingDraftId = dbHelper.insertDraft(draft);
        }
        loadDashboardStats();
        showMessage(getString(R.string.draft_saved_message));
    }

    private void autosaveIfNeeded() {
        // silently save draft when user navigates away if there is content
        String recipient = getRecipientText();
        String subject = getSubjectText();
        String rawMessage = getRawMessageText();
        if (TextUtils.isEmpty(recipient) && TextUtils.isEmpty(subject) && TextUtils.isEmpty(rawMessage)) {
            return;
        }

        EmailModel draft = new EmailModel(0L, EmailValidator.normalizeEmailList(recipient), subject, rawMessage, dbHelper.getCurrentDateTime(), DBHelper.STATUS_DRAFT);
        if (editingDraftId > 0) {
            dbHelper.updateDraft(editingDraftId, draft);
        } else {
            editingDraftId = dbHelper.insertDraft(draft);
        }
        loadDashboardStats();
    }

    private boolean validateInputs() {
        binding.recipientLayout.setError(null);
        binding.ccLayout.setError(null);
        binding.bccLayout.setError(null);
        binding.messageLayout.setError(null);

        boolean valid = true;
        if (TextUtils.isEmpty(getRecipientText())) {
            binding.recipientLayout.setError(getString(R.string.recipient_required_error));
            valid = false;
        } else if (!EmailValidator.areValidEmails(getRecipientText())) {
            binding.recipientLayout.setError(getString(R.string.invalid_email_error));
            valid = false;
        }
        if (!TextUtils.isEmpty(getCcText()) && !EmailValidator.areValidEmails(getCcText())) {
            binding.ccLayout.setError(getString(R.string.invalid_email_error));
            valid = false;
        }
        if (!TextUtils.isEmpty(getBccText()) && !EmailValidator.areValidEmails(getBccText())) {
            binding.bccLayout.setError(getString(R.string.invalid_email_error));
            valid = false;
        }
        if (TextUtils.isEmpty(getMessageText())) {
            binding.messageLayout.setError(getString(R.string.message_required_error));
            valid = false;
        }
        return valid;
    }

    private void clearForm() {
        selectedAttachmentUri = null;
        editingDraftId = -1L;
        binding.recipientEditText.setText("");
        binding.ccEditText.setText("");
        binding.bccEditText.setText("");
        binding.subjectEditText.setText("");
        binding.messageEditText.setText("");
        binding.attachmentChip.setVisibility(View.GONE);
        binding.recipientChipGroup.removeAllViews();
        updateMessageHeight(0);
    }

    private void updateMessageHeight(int length) {
        int minLines = length > 200 ? 10 : 8;
        binding.messageEditText.setMinLines(minLines);
    }

    private void animatePress(View view) {
        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
    }

    private String getRecipientText() {
        return safeText(binding.recipientEditText.getText());
    }

    private String getCcText() {
        return safeText(binding.ccEditText.getText());
    }

    private String getBccText() {
        return safeText(binding.bccEditText.getText());
    }

    private String getSubjectText() {
        return safeText(binding.subjectEditText.getText());
    }

    private String getMessageText() {
        return getRawMessageText();
    }

    private String getRawMessageText() {
        String message = safeText(binding.messageEditText.getText());
        String signature = requireContext().getSharedPreferences(ThemeUtils.PREFS_NAME, 0).getString(ThemeUtils.KEY_SIGNATURE, "");
        if (TextUtils.isEmpty(signature)) {
            return message;
        }
        return message;
    }

    private String appendSignature(String message) {
        String signature = requireContext().getSharedPreferences(ThemeUtils.PREFS_NAME, 0).getString(ThemeUtils.KEY_SIGNATURE, "");
        if (TextUtils.isEmpty(signature)) {
            return message;
        }
        return TextUtils.isEmpty(message) ? signature : message + "\n\n--\n" + signature;
    }

    private String safeText(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    public static ComposeFragment createEmpty() {
        return new ComposeFragment();
    }
}

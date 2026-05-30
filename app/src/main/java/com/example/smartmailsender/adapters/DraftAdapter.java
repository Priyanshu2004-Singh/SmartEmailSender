package com.example.smartmailsender.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmailsender.R;
import com.example.smartmailsender.models.EmailModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DraftAdapter extends RecyclerView.Adapter<DraftAdapter.DraftViewHolder> {

    public interface DraftActionListener {
        void onEditDraft(EmailModel emailModel);
        void onDeleteDraft(EmailModel emailModel);
    }

    private final Context context;
    private final List<EmailModel> items;
    private final DraftActionListener listener;

    public DraftAdapter(Context context, List<EmailModel> items, DraftActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DraftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_draft, parent, false);
        return new DraftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DraftViewHolder holder, int position) {
        EmailModel item = items.get(position);
        holder.recipientValue.setText(safeText(item.getRecipient()).isEmpty() ? context.getString(R.string.no_recipient) : safeText(item.getRecipient()));
        holder.subjectValue.setText(safeText(item.getSubject()).isEmpty() ? context.getString(R.string.no_subject) : safeText(item.getSubject()));
        holder.dateValue.setText(formatDate(item.getDate()));
        holder.editButton.setOnClickListener(v -> listener.onEditDraft(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteDraft(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(value);
            if (parsed == null) {
                return value;
            }
            return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(parsed);
        } catch (ParseException exception) {
            return value;
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    static class DraftViewHolder extends RecyclerView.ViewHolder {
        final TextView recipientValue;
        final TextView subjectValue;
        final TextView dateValue;
        final MaterialButton editButton;
        final MaterialButton deleteButton;
        final MaterialCardView cardView;

        DraftViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.draftCardView);
            recipientValue = itemView.findViewById(R.id.draftRecipientValue);
            subjectValue = itemView.findViewById(R.id.draftSubjectValue);
            dateValue = itemView.findViewById(R.id.draftDateValue);
            editButton = itemView.findViewById(R.id.editDraftButton);
            deleteButton = itemView.findViewById(R.id.deleteDraftButton);
        }
    }
}

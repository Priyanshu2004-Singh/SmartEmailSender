package com.example.smartmailsender.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmailsender.R;
import com.example.smartmailsender.models.EmailModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(EmailModel emailModel);
    }

    private final Context context;
    private final List<EmailModel> historyItems;
    private final OnDeleteClickListener deleteClickListener;

    public HistoryAdapter(Context context, List<EmailModel> historyItems, OnDeleteClickListener deleteClickListener) {
        this.context = context;
        this.historyItems = historyItems;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        EmailModel item = historyItems.get(position);
        holder.recipientValue.setText(safeText(item.getRecipient()));
        holder.subjectValue.setText(safeText(item.getSubject()).isEmpty() ? context.getString(R.string.no_subject) : safeText(item.getSubject()));
        holder.dateValue.setText(formatDate(item.getDate()));
        holder.statusValue.setText(safeText(item.getStatus()).toUpperCase(Locale.getDefault()));
        holder.statusValue.setTextColor(getStatusColor(item.getStatus()));
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    private int getStatusColor(String status) {
        if (status == null) {
            return ContextCompat.getColor(context, R.color.colorOutline);
        }
        if (context.getString(R.string.status_sent).equalsIgnoreCase(status)) {
            return ContextCompat.getColor(context, R.color.status_sent_color);
        }
        if (context.getString(R.string.status_draft).equalsIgnoreCase(status)) {
            return ContextCompat.getColor(context, R.color.status_draft_color);
        }
        return ContextCompat.getColor(context, R.color.colorOutline);
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

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        final TextView recipientValue;
        final TextView subjectValue;
        final TextView dateValue;
        final TextView statusValue;
        final MaterialButton deleteButton;
        final MaterialCardView cardView;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.historyCardView);
            recipientValue = itemView.findViewById(R.id.recipientValue);
            subjectValue = itemView.findViewById(R.id.subjectValue);
            dateValue = itemView.findViewById(R.id.dateValue);
            statusValue = itemView.findViewById(R.id.statusValue);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}

package com.example.pt8firebase_rogerferr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private final List<Note> notes;
    private final NoteActionListener noteActionListener;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

    public NoteAdapter(List<Note> notes, NoteActionListener noteActionListener) {
        this.notes = notes;
        this.noteActionListener = noteActionListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.titleText.setText(note.getTitle());
        holder.contentText.setText(note.getContent());
        holder.dateText.setText(dateFormat.format(new Date(note.getCreatedAt())));
        holder.statusChip.setText(note.isImportant() ? R.string.important_label : R.string.normal_label);
        holder.statusChip.setChipBackgroundColorResource(note.isImportant() ? R.color.brand_primary : R.color.brand_accent);
        holder.statusChip.setTextColor(holder.itemView.getContext().getColor(note.isImportant() ? android.R.color.white : R.color.brand_primary_dark));
        holder.editButton.setOnClickListener(v -> noteActionListener.onEdit(note));
        holder.deleteButton.setOnClickListener(v -> noteActionListener.onDelete(note));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView contentText;
        private final TextView dateText;
        private final Chip statusChip;
        private final MaterialButton editButton;
        private final MaterialButton deleteButton;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.noteTitleText);
            contentText = itemView.findViewById(R.id.noteContentText);
            dateText = itemView.findViewById(R.id.noteDateText);
            statusChip = itemView.findViewById(R.id.noteStatusChip);
            editButton = itemView.findViewById(R.id.editNoteButton);
            deleteButton = itemView.findViewById(R.id.deleteNoteButton);
        }
    }

    public interface NoteActionListener {
        void onEdit(Note note);

        void onDelete(Note note);
    }
}
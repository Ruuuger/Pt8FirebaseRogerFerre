package com.example.pt8firebase_rogerferr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String NOTES_NODE = "notes";

    private final List<Note> notes = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private DatabaseReference notesReference;
    private ValueEventListener notesListener;

    private View emptyState;
    private View loadingIndicator;
    private TextView totalNotesText;
    private TextView importantNotesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupViews();
        setupRecyclerView();
        initializeFirebase();
        observeNotes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notesReference != null && notesListener != null) {
            notesReference.removeEventListener(notesListener);
        }
    }

    private void setupViews() {
        RecyclerView notesRecyclerView = findViewById(R.id.notesRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        totalNotesText = findViewById(R.id.totalNotesValue);
        importantNotesText = findViewById(R.id.importantNotesValue);
        FloatingActionButton addNoteButton = findViewById(R.id.addNoteButton);

        addNoteButton.setOnClickListener(v -> showNoteDialog(null));
        notesRecyclerView.setHasFixedSize(true);
    }

    private void setupRecyclerView() {
        RecyclerView notesRecyclerView = findViewById(R.id.notesRecyclerView);
        noteAdapter = new NoteAdapter(notes, new NoteAdapter.NoteActionListener() {
            @Override
            public void onEdit(Note note) {
                showNoteDialog(note);
            }

            @Override
            public void onDelete(Note note) {
                confirmDelete(note);
            }
        });
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(noteAdapter);
    }

    private void initializeFirebase() {
        String databaseUrl = getString(R.string.firebase_database_url);
        FirebaseDatabase database = FirebaseDatabase.getInstance(databaseUrl);
        notesReference = database.getReference(NOTES_NODE);
    }

    private void observeNotes() {
        showLoading(true);
        notesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                notes.clear();
                for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);
                    if (note != null) {
                        note.setId(noteSnapshot.getKey());
                        notes.add(0, note);
                    }
                }
                noteAdapter.notifyDataSetChanged();
                updateSummary();
                showLoading(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                showLoading(false);
                Toast.makeText(MainActivity.this, getString(R.string.notes_load_error, error.getMessage()), Toast.LENGTH_LONG).show();
            }
        };

        notesReference.orderByChild("createdAt").addValueEventListener(notesListener);
    }

    private void showNoteDialog(Note existingNote) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note, null, false);
        TextInputEditText titleInput = dialogView.findViewById(R.id.titleInput);
        TextInputEditText contentInput = dialogView.findViewById(R.id.contentInput);
        MaterialSwitch importantSwitch = dialogView.findViewById(R.id.importantSwitch);

        if (existingNote != null) {
            titleInput.setText(existingNote.getTitle());
            contentInput.setText(existingNote.getContent());
            importantSwitch.setChecked(existingNote.isImportant());
        }

        int dialogTitle = existingNote == null ? R.string.add_note : R.string.edit_note;
        int actionText = existingNote == null ? R.string.save_note : R.string.update_note;

        new MaterialAlertDialogBuilder(this)
                .setTitle(dialogTitle)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(actionText, (dialog, which) -> saveNote(
                        existingNote,
                        valueOf(titleInput),
                        valueOf(contentInput),
                        importantSwitch.isChecked()))
                .show();
    }

    private void saveNote(Note existingNote, String title, String content, boolean important) {
        if (title.isBlank() || content.isBlank()) {
            Toast.makeText(this, R.string.note_validation_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingNote == null) {
            String noteId = notesReference.push().getKey();
            if (noteId == null) {
                Toast.makeText(this, R.string.note_save_error, Toast.LENGTH_SHORT).show();
                return;
            }

            Note note = new Note(noteId, title, content, important, System.currentTimeMillis());
            notesReference.child(noteId)
                    .setValue(note)
                    .addOnSuccessListener(unused -> Toast.makeText(this, R.string.note_saved, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(error -> Toast.makeText(this, getString(R.string.notes_load_error, error.getMessage()), Toast.LENGTH_LONG).show());
            return;
        }

        existingNote.setTitle(title);
        existingNote.setContent(content);
        existingNote.setImportant(important);
        notesReference.child(existingNote.getId())
                .setValue(existingNote)
                .addOnSuccessListener(unused -> Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(error -> Toast.makeText(this, getString(R.string.notes_load_error, error.getMessage()), Toast.LENGTH_LONG).show());
    }

    private void confirmDelete(Note note) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_note)
                .setMessage(getString(R.string.delete_note_message, note.getTitle()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteNote(note))
                .show();
    }

    private void deleteNote(Note note) {
        notesReference.child(note.getId())
                .removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(error -> Toast.makeText(this, getString(R.string.notes_load_error, error.getMessage()), Toast.LENGTH_LONG).show());
    }

    private void updateSummary() {
        int importantCount = 0;
        for (Note note : notes) {
            if (note.isImportant()) {
                importantCount++;
            }
        }

        totalNotesText.setText(String.valueOf(notes.size()));
        importantNotesText.setText(String.valueOf(importantCount));
        emptyState.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private String valueOf(TextInputEditText input) {
        if (input.getText() == null) {
            return "";
        }
        return input.getText().toString().trim();
    }
}
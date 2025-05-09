package ca.yorku.eecs.mack.notetakingapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import android.content.SharedPreferences;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.util.Base64;

public class HomeActivity extends AppCompatActivity {

    List<Note> noteList;
    NotesAdapter adapter;
    List<Note> allNotesList;
    private BatterySyncManager batteryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        // makes status bar transparent
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setContentView(R.layout.activity_home);
        // adds a tip for the user
        Toast toast = Toast.makeText(this, "Tip: Hold a note to delete it", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 250); // optional: move it up a bit
        toast.show();
        // setup battery and syncing manager
        batteryManager = BatterySyncManager.getInstance(this);

        boolean isDarkMode = batteryManager.isDarkModeEnabled();

        // setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Setting the toolbar text color depending on mode
        if (batteryManager.isDarkModeEnabled()) {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.backgroundDark));
        }
        else {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.backgroundLight));
        }

        // setting up Navigation Drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        MenuItem darkModeItem = navView.getMenu().findItem(R.id.nav_dark_mode);

        if (batteryManager.isDarkModeLast()) {
            darkModeItem.setTitle(R.string.lightToggle); // Light mode
        }
        else {
            darkModeItem.setTitle(R.string.darkToggle); // Dark mode
        }

        navView.setCheckedItem(R.id.nav_all_notes);

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_all_notes) {
                // already in all notes
            }
            else if (id == R.id.nav_export) {
                Toast.makeText(this, "Exporting notes...", Toast.LENGTH_SHORT).show();
                showNoteSelectionDialog();
            }
            else if (id == R.id.nav_dark_mode) {
                // Switches to dark
                if (batteryManager.isDarkModeEnabled()) {
                    batteryManager.toggleLightDarkMode();
                    darkModeItem.setTitle(R.string.darkToggle);
                    getWindow().getDecorView()
                            .setBackgroundColor(ContextCompat
                                    .getColor(this, R.color.backgroundDark));
                    Toast.makeText(this, "Dark mode toggled", Toast.LENGTH_SHORT).show();
                }
                // Switches to light
                else {
                    batteryManager.toggleLightDarkMode();
                    darkModeItem.setTitle(R.string.lightToggle);
                    getWindow().getDecorView()
                            .setBackgroundColor(ContextCompat
                                    .getColor(this, R.color.backgroundLight));
                    Toast.makeText(this, "Light mode toggled", Toast.LENGTH_SHORT).show();
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // setup RecyclerView for note grid (3 columns)
        RecyclerView recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        loadNotesFromStorage(); // loads existing notes from SharedPreferences

        adapter = new NotesAdapter(this, noteList, isDarkMode); // create adapter using the loaded notes
        recyclerView.setAdapter(adapter);     // set adapter to RecyclerView

        batteryManager.setupSyncOnStartup((ArrayList<Note>) noteList);


        adapter.setOnNoteLongClickListener(position -> {
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // remove from the list
                        noteList.remove(position);

                        // save updated list to SharedPreferences
                        saveNotesToStorage();

                        // notify adapter to refresh UI
                        adapter.notifyItemRemoved(position);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // FAB: Create new note or task list
        FloatingActionButton fab = findViewById(R.id.fabAddNote);
        fab.setOnClickListener(v -> {

            View view = getLayoutInflater().inflate(R.layout.bottom_sheet_new_item, null);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
            bottomSheetDialog.setContentView(view);

            Button buttonNewNote = view.findViewById(R.id.buttonNewNote);
            Button buttonNewTaskList = view.findViewById(R.id.buttonNewTaskList);

            buttonNewNote.setOnClickListener(view1 -> {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(HomeActivity.this, NoteActivity.class);
                startActivityForResult(intent, 1);
            });

            buttonNewTaskList.setOnClickListener(view2 -> {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(HomeActivity.this, AddTaskListActivity.class);
                startActivityForResult(intent, 2);
            });

            bottomSheetDialog.show();
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String noteTitle = data.getStringExtra("title");
            String noteContent = data.getStringExtra("content");
            String drawingData = data.getStringExtra("drawing");

            if (drawingData == null) {
                drawingData = "";
            }
            
            int position = data.getIntExtra("position", -1);

            if (position != -1) {
                Note updatedNote = new Note(noteTitle, noteContent);
                updatedNote.setDrawingImageBase64(drawingData);
                noteList.set(position, updatedNote);
                adapter.notifyItemChanged(position);
            }
            else {
                Note newNote = new Note(noteTitle, noteContent);
                newNote.setDrawingImageBase64(drawingData);
                noteList.add(newNote);
                adapter.notifyItemInserted(noteList.size() - 1);
            }

            adapter.setNotes(noteList);
            saveNotesToStorage();
        }

        else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            String desc = data.getStringExtra("description");
            ArrayList<String> tasks = data.getStringArrayListExtra("tasks");
            ArrayList<Boolean> checked = (ArrayList<Boolean>) data.getSerializableExtra("checked");
            int position = data.getIntExtra("position", -1);

            Note newTaskList = new Note(desc, tasks, checked);

            if (position != -1) {
                // editing an exsiting note
                noteList.set(position, newTaskList);
                adapter.notifyItemChanged(position);
            } else {
                // adding a brand new task list
                noteList.add(newTaskList);
                adapter.notifyItemInserted(noteList.size() - 1);
            }

            adapter.setNotes(noteList);
            saveNotesToStorage();
        }
    }

    private void saveNotesToStorage() {
        SharedPreferences prefs = getSharedPreferences("NotesApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(noteList);
        editor.putString("notes", json);
        editor.apply();
    }

    private void loadNotesFromStorage() {
        SharedPreferences prefs = getSharedPreferences("NotesApp", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("notes", null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        noteList = gson.fromJson(json, type);

        if (noteList == null) {
            noteList = new ArrayList<>();
        }

        allNotesList = new ArrayList<>(noteList); // backup full list
    }

    private void showNoteSelectionDialog() {
        if (noteList == null || noteList.isEmpty()) {
            Toast.makeText(this, "No notes available to export", Toast.LENGTH_SHORT).show();
            return;
        }

        // filter notes: only include titled, non-task notes
        List<Note> exportableNotes = new ArrayList<>();
        List<String> exportTitles = new ArrayList<>();

        for (Note n : noteList) {
            boolean hasTitle = n.getTitle() != null && !n.getTitle().trim().isEmpty();
            boolean isRegularNote = n.getTasks() == null || n.getTasks().isEmpty();

            if (hasTitle && isRegularNote) {
                exportableNotes.add(n);
                exportTitles.add(n.getTitle());
            }
        }

        if (exportableNotes.isEmpty()) {
            Toast.makeText(this, "No titled notes available to export", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] titlesArray = exportTitles.toArray(new String[0]);
        Toast.makeText(this, "Notes must be titled to export", Toast.LENGTH_SHORT).show();
        new AlertDialog.Builder(this)
                .setTitle("Select a note to export")
                .setItems(titlesArray, (dialog, which) -> {
                    exportNoteByEmail(exportableNotes.get(which));
                })
                .show();
    }

    private void exportNoteByEmail(Note note) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        emailIntent.putExtra(Intent.EXTRA_TEXT, note.getContent());

        try {
            startActivity(Intent.createChooser(emailIntent, "Send note via email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }



}

package ca.yorku.eecs.mack.notetakingapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

// Worker for syncing notes locally to SharedPreferences
public class NoteSyncWorker extends androidx.work.Worker {
    public NoteSyncWorker(Context context, androidx.work.WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Loads the notes from SharedPreferences
        android.content.SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("NotesApp", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("notes", null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        ArrayList<Note> noteList = gson.fromJson(json, type);

        // Happens when there aren't any notes to save
        if (noteList == null || noteList.isEmpty()) {
            System.out.println("No notes to sync locally.");
            return Result.success();
        }

        // In a real local-sync scenario, this could check for unsaved changes or consolidate data
        // For now, it's main use is to re-save the list and ensure persistence
        try {
            // Saving the list here
            System.out.println("Saving " + noteList.size()
                    + " notes to SharedPreferences in background...");
            SharedPreferences.Editor editor = prefs.edit();
            String updatedJson = gson.toJson(noteList);
            editor.putString("notes", updatedJson);
            editor.apply();
            return Result.success();
        } catch (Exception e) { // If the local sync fails, it will retry again
            System.out.println("Local sync failed: " + e.getMessage());
            return Result.retry();
        }
    }
}

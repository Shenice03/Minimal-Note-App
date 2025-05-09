package ca.yorku.eecs.mack.notetakingapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import androidx.appcompat.app.AppCompatDelegate;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class BatterySyncManager {
    private static BatterySyncManager instance;
    private final Context appContext;
    private static final long NORMAL_SYNC_INTERVAL = 60; // Minutes
    private static final long LOW_SYNC_INTERVAL = 120; // Minutes
    private static final int LOW_BATTERY_THRESHOLD = 20; // Percent
    private static final String PREFS_NAME = "NotesApp"; // Matches HomeActivity
    private final SharedPreferences prefs;
    private BatterySyncManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("BatteryPrefs", Context.MODE_PRIVATE);
    }

    public static synchronized BatterySyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new BatterySyncManager(context);
        }
        return instance;
    }

    // One-time battery level check
    public int getBatteryLevel() {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = appContext.registerReceiver(null, iFilter);
        assert batteryStatus != null;
        int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (int) ((batteryLevel / (float) batteryScale) * 100);
    }

    // One-time charging status check
    public boolean isCharging() {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = appContext.registerReceiver(null, iFilter);

        // Assertion is made here in case of a null-pointer exception
        assert batteryStatus != null;
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    // Sets up syncing based on initial battery state
    public void setupSyncOnStartup(ArrayList<Note> noteList) {
        boolean isLowBattery = getBatteryLevel() <= LOW_BATTERY_THRESHOLD && !isCharging();
        long syncInterval = isLowBattery ? LOW_SYNC_INTERVAL : NORMAL_SYNC_INTERVAL;

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(
                NoteSyncWorker.class, syncInterval, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInitialDelay(5, TimeUnit.MINUTES) // Small delay to avoid startup rush
                .build();

        WorkManager.getInstance(appContext)
                .enqueueUniquePeriodicWork(
                        "note_sync",
                        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                        syncWorkRequest
                );
    }

    // Light/dark mode toggle
    public void toggleLightDarkMode() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        SharedPreferences.Editor editor = prefs.edit();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Light mode
            editor.putBoolean("isDarkMode", false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // Dark mode
            editor.putBoolean("isDarkMode", true);
        }
        editor.apply();
    }

    // Checks if dark mode is enabled (not used currently but can be used)
    public boolean isDarkModeEnabled() {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
    }

    public boolean isDarkModeLast() {
        return prefs.getBoolean("isDarkMode", false); // false = light, true = dark
    }
}


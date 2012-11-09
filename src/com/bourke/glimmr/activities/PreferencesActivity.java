package com.bourke.glimmr.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.R;
import com.bourke.glimmr.services.AppListener;
import com.bourke.glimmr.services.AppService;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.googlecode.flickrjandroid.oauth.OAuth;

public class PreferencesActivity extends SherlockPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "Glimmr/PreferenceManager";

    private SharedPreferences mSharedPrefs;
    private ListPreference mIntervalsListPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(PreferencesActivity.this,
                R.xml.preferences, false);
        mIntervalsListPreference = (ListPreference) getPreferenceScreen()
            .findPreference(Constants.KEY_INTERVALS_LIST_PREFERENCE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Setup the initial value
         * http://stackoverflow.com/a/531927/663370
         */
        updateIntervalSummary();

        /* Set up a listener whenever a key changes */
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);

        /* Disable notification options if not logged in */
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        OAuth oauth = BaseActivity.loadAccessToken(prefs);
        if (oauth == null || oauth.getUser() == null) {
            CheckBoxPreference enableNotificationsItem =
                (CheckBoxPreference) getPreferenceScreen()
                .findPreference(Constants.KEY_ENABLE_NOTIFICATIONS);
            enableNotificationsItem.setEnabled(false);
            enableNotificationsItem.setChecked(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (Constants.KEY_INTERVALS_LIST_PREFERENCE.equals(key)) {
            updateIntervalSummary();
            WakefulIntentService.scheduleAlarms(new AppListener(), this,
                    false);
        } else if (Constants.KEY_ENABLE_NOTIFICATIONS.equals(key)) {
            boolean enableNotifications = sharedPreferences.getBoolean(
                    Constants.KEY_ENABLE_NOTIFICATIONS, false);
            if (!enableNotifications) {
                if (Constants.DEBUG) Log.d(TAG, "Cancelling alarms");
                AppService.cancelAlarms(this);
            } else {
                WakefulIntentService.scheduleAlarms(new AppListener(), this,
                        false);
            }
        }
    }

    private void updateIntervalSummary() {
        String listPrefValue = mSharedPrefs.getString(
                Constants.KEY_INTERVALS_LIST_PREFERENCE, "");
        String summaryString = "";
        /* NOTE: ListPreference doesn't seem to allow integer values */
        if ("15".equals(listPrefValue)) {
            summaryString = getString(R.string.fifteen_mins);
        } else if ("30".equals(listPrefValue)) {
            summaryString = getString(R.string.thirty_mins);
        } else if ("60".equals(listPrefValue)) {
            summaryString = getString(R.string.one_hour);
        } else if ("240".equals(listPrefValue)) {
            summaryString = getString(R.string.four_hours);
        } else if ("1440".equals(listPrefValue)) {
            summaryString = getString(R.string.once_a_day);
        } else {
            Log.e(TAG, "updateIntervalSummary: unknown value for " +
                "ListPreference entry: " + listPrefValue);
        }
        mIntervalsListPreference.setSummary(summaryString);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /* This is called when the Home (Up) button is pressed
                 * in the Action Bar. */
                Intent parentActivityIntent = new Intent(this,
                        MainActivity.class);
                parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

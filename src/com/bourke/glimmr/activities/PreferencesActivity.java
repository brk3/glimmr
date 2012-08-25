package com.bourke.glimmr.activities;

import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.R;
import com.bourke.glimmr.services.AppListener;
import com.bourke.glimmr.services.AppService;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "Glimmr/PreferenceManager";


    private SharedPreferences mSharedPrefs;

    private ListPreference mIntervalsListPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals(Constants.KEY_INTERVALS_LIST_PREFERENCE)) {
            updateIntervalSummary();
            WakefulIntentService.scheduleAlarms(new AppListener(), this,
                    false);
        } else if (key.equals(Constants.KEY_ENABLE_NOTIFICATIONS)) {
            boolean enableNotifications = sharedPreferences.getBoolean(
                    Constants.KEY_ENABLE_NOTIFICATIONS, false);
            if (!enableNotifications) {
                Log.d(TAG, "Cancelling alarms");
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
        if (listPrefValue.equals("15")) {
            summaryString = getString(R.string.fifteen_mins);
        } else if (listPrefValue.equals("30")) {
            summaryString = getString(R.string.thirty_mins);
        } else if (listPrefValue.equals("60")) {
            summaryString = getString(R.string.one_hour);
        } else if (listPrefValue.equals("240")) {
            summaryString = getString(R.string.four_hours);
        } else if (listPrefValue.equals("1440")) {
            summaryString = getString(R.string.once_a_day);
        } else {
            Log.e(TAG, "updateIntervalSummary: unknown value for " +
                    "ListPreference entry: " + listPrefValue);
        }
        mIntervalsListPreference.setSummary(summaryString);
    }
}

package com.bourke.glimmr.activities;

import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;

import com.bourke.glimmr.R;
import android.preference.ListPreference;

public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "Glimmr/PreferenceManager";

    public static final String KEY_INTERVALS_LIST_PREFERENCE
        = "notificationIntervals";

    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mEditor;

    private ListPreference mIntervalsListPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPrefs.edit();

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(PreferencesActivity.this,
                R.xml.preferences, false);

        mIntervalsListPreference = (ListPreference) getPreferenceScreen()
            .findPreference(KEY_INTERVALS_LIST_PREFERENCE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* Setup the initial values */
        mIntervalsListPreference.setSummary(mSharedPrefs.getString(
                    KEY_INTERVALS_LIST_PREFERENCE, ""));

        /* Set up a listener whenever a key changes */
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * http://stackoverflow.com/a/531927/663370
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals(KEY_INTERVALS_LIST_PREFERENCE)) {
            mIntervalsListPreference.setSummary(mSharedPrefs.getString(
                        KEY_INTERVALS_LIST_PREFERENCE, ""));
        }
    }
}

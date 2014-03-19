package com.bourke.glimmr.fragments;

import com.bourke.glimmr.BuildConfig;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.OAuthUtils;
import com.bourke.glimmr.services.AppListener;
import com.bourke.glimmr.services.AppService;
import com.commonsware.cwac.wakeful.WakefulIntentService;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "Glimmr/SettingsFragment";

    private SharedPreferences mSharedPrefs;
    private ListPreference mIntervalsListPreference;
    private ListPreference mInitialTabListPreference;
    private EditTextPreference mSlideshowIntervalPreference;
    private CheckBoxPreference mHighQualityThumbsCbPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences,
                false);

        mSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        mIntervalsListPreference = (ListPreference) getPreferenceScreen()
            .findPreference(Constants.KEY_INTERVALS_LIST_PREFERENCE);
        mInitialTabListPreference = (ListPreference) getPreferenceScreen()
            .findPreference(Constants.KEY_INITIAL_TAB_LIST_PREFERENCE);
        mSlideshowIntervalPreference = (EditTextPreference)
            getPreferenceScreen().findPreference(
                    Constants.KEY_SLIDESHOW_INTERVAL);
        mHighQualityThumbsCbPreference = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(Constants.KEY_HIGH_QUALITY_THUMBNAILS);

        if (Constants.PRO_VERSION) {
            mHighQualityThumbsCbPreference.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        /* Setup the initial ListPreference values -
         * http://stackoverflow.com/a/531927/663370
         */
        updateIntervalSummary();
        updateInitialTabSummary();
        updateSlideshowIntervalSummary();

        /* Set up a listener whenever a key changes */
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);

        /* Disable notification options if not logged in */
        if (!OAuthUtils.isLoggedIn(getActivity())) {
            CheckBoxPreference enableNotificationsItem =
                (CheckBoxPreference) getPreferenceScreen()
                .findPreference(Constants.KEY_ENABLE_NOTIFICATIONS);
            enableNotificationsItem.setEnabled(false);
            enableNotificationsItem.setChecked(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (Constants.KEY_INTERVALS_LIST_PREFERENCE.equals(key)) {
            updateIntervalSummary();
            WakefulIntentService.scheduleAlarms(new AppListener(),
                    getActivity(), false);
        } else if (Constants.KEY_ENABLE_NOTIFICATIONS.equals(key)) {
            boolean enableNotifications = sharedPreferences.getBoolean(
                    Constants.KEY_ENABLE_NOTIFICATIONS, false);
            if (!enableNotifications) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Cancelling alarms");
                AppService.cancelAlarms(getActivity());
            } else {
                WakefulIntentService.scheduleAlarms(new AppListener(),
                        getActivity(), false);
            }
        } else if (Constants.KEY_INITIAL_TAB_LIST_PREFERENCE.equals(key)) {
            updateInitialTabSummary();
        } else if (Constants.KEY_SLIDESHOW_INTERVAL.equals(key)) {
            updateSlideshowIntervalSummary();
        } else if (Constants.KEY_HIGH_QUALITY_THUMBNAILS.equals(key)) {
            /* Restart the main activity to clear the memory cache */
            Context baseContext = getActivity().getBaseContext();
            Intent i = baseContext.getPackageManager().getLaunchIntentForPackage(
                    baseContext.getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    private void updateInitialTabSummary() {
        String listPrefValue = mSharedPrefs.getString(
                Constants.KEY_INITIAL_TAB_LIST_PREFERENCE,
                getString(R.string.contacts));
        mInitialTabListPreference.setSummary(listPrefValue);
    }

    private void updateIntervalSummary() {
        String listPrefValue = mSharedPrefs.getString(
                Constants.KEY_INTERVALS_LIST_PREFERENCE, "");
        String summaryString = "";
        /* NOTE: ListPreference doesn't seem to allow integer values */
        if (listPrefValue.equals("30")) {
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

    private void updateSlideshowIntervalSummary() {
        String val = mSharedPrefs.getString(
                Constants.KEY_SLIDESHOW_INTERVAL, "3");
        mSlideshowIntervalPreference.setSummary(val);
    }

}

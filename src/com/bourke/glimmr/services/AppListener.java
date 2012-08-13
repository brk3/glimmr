package com.bourke.glimmr.services;

import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.SystemClock;

import android.preference.PreferenceManager;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.Constants;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class AppListener implements WakefulIntentService.AlarmListener {

    private static final String TAG = "Glimmr/AppListener";

    private int mMinutes;

    public void scheduleAlarms(AlarmManager mgr, PendingIntent pendingIntent,
            Context context) {
        mMinutes = getMinutes(context);
        mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+mMinutes*60*1000,
                mMinutes*60*1000, pendingIntent);
        Log.d(TAG, String.format("Set alarms for %d intervals", mMinutes));
    }

    public void sendWakefulWork(Context context) {
        WakefulIntentService.sendWakefulWork(context, AppService.class);
    }

    public long getMaxAge() {
        return(mMinutes*60*1000*2);
    }

    private int getMinutes(Context context) {
        int minutes = 60;
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        String intervalPref = prefs.getString(
                Constants.KEY_INTERVALS_LIST_PREFERENCE, "");
        try {
            mMinutes = Integer.parseInt(intervalPref);
            Log.d(TAG, "mMinutes set to " + mMinutes);
        } catch (NumberFormatException e) {
            Log.e(TAG, String.format("scheduleAlarms: can't parse '%s' " +
                    "as intervalPref", intervalPref));
        }
        return minutes;
    }
}

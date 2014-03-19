package com.bourke.glimmr.services;

import com.bourke.glimmr.BuildConfig;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.commonsware.cwac.wakeful.WakefulIntentService;

public class AppListener implements WakefulIntentService.AlarmListener {

    private static final String TAG = "Glimmr/AppListener";

    private int mMinutes;

    @Override
    public void scheduleAlarms(AlarmManager mgr, PendingIntent pendingIntent,
            Context context) {
        /* If notifications off, ensure alarms are cancelled and return */
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        boolean enableNotifications =
            prefs.getBoolean(Constants.KEY_ENABLE_NOTIFICATIONS, false);
        if (!enableNotifications) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Cancelling alarms");
            AppService.cancelAlarms(context);
            return;
        }

        mMinutes = getMinutes(context);
        mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+mMinutes*60*1000,
                mMinutes*60*1000, pendingIntent);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("Set alarms for %d minute intervals",
                    mMinutes));
        }
    }

    @Override
    public void sendWakefulWork(Context context) {
        WakefulIntentService.sendWakefulWork(context, AppService.class);
    }

    @Override
    public long getMaxAge() {
        return(mMinutes*60*1000*2);
    }

    private int getMinutes(Context context) {
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        String intervalPref = prefs.getString(
                Constants.KEY_INTERVALS_LIST_PREFERENCE, "60");
        try {
            mMinutes = Integer.parseInt(intervalPref);
        } catch (NumberFormatException e) {
            Log.e(TAG, String.format("scheduleAlarms: can't parse '%s' " +
                "as intervalPref", intervalPref));
            e.printStackTrace();
        }
        return mMinutes;
    }
}

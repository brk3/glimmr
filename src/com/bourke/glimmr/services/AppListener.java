package com.bourke.glimmr.services;

import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.SystemClock;

import com.bourke.glimmr.common.Constants;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class AppListener implements WakefulIntentService.AlarmListener {

    private int mMinutes;

    public void scheduleAlarms(AlarmManager mgr, PendingIntent pendingIntent,
            Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        mMinutes = prefs.getInt(Constants.NEW_PHOTOS_SERVICE_INTERVAL, 60);
        mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+mMinutes*60*1000,
                mMinutes*60*1000, pendingIntent);
    }

    public void sendWakefulWork(Context context) {
        WakefulIntentService.sendWakefulWork(context, AppService.class);
    }

    public long getMaxAge() {
        return(mMinutes*60*1000*2);
    }
}

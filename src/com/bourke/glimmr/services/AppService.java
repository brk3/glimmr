package com.bourke.glimmr.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.Constants;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.googlecode.flickrjandroid.oauth.OAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Polls Flickr for various resources and notifies the user via various
 * handlers.
 */
public class AppService extends WakefulIntentService {

    private static final String TAG = "Glimmr/AppService";

    private SharedPreferences mPrefs;
    private List<GlimmrNotificationHandler> mHandlers;

    public AppService() {
        super("AppService");
    }

    /**
     * Essentially like a crontab entry, what gets executed periodically.
     */
    @Override
    protected void doWakefulWork(Intent intent) {
        if (Constants.DEBUG) Log.d(TAG, "doWakefulWork");

        mPrefs = getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);

        /* Fetch the oauth token from storage */
        OAuth oauth = BaseActivity.loadAccessToken(mPrefs);
        if (oauth == null) {
            if (Constants.DEBUG) {
                Log.d(TAG, "doWakefulWork: no stored oauth token found, " +
                        "doing nothing");
            }
            return;
        }

        /* Important: add each handler to be run here */
        mHandlers = new ArrayList<GlimmrNotificationHandler>();
        mHandlers.add(new ContactsPhotosNotificationHandler(this));
        mHandlers.add(new ActivityNotificationHandler(this));

        /* Start each handler */
        for (GlimmrNotificationHandler handler : mHandlers) {
            handler.startTask(oauth);
        }
    }
}

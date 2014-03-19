package com.bourke.glimmr.services;

import com.bourke.glimmr.BuildConfig;
import android.content.Intent;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.OAuthUtils;
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

    public AppService() {
        super("AppService");
    }

    /**
     * Essentially like a crontab entry, what gets executed periodically.
     */
    @Override
    protected void doWakefulWork(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "doWakefulWork");

        /* Fetch the oauth token from storage */
        OAuth oauth = OAuthUtils.loadAccessToken(this);
        if (oauth == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doWakefulWork: no stored oauth token found, " +
                        "doing nothing");
            }
            return;
        }

        /* Important: add each handler to be run here */
        List<GlimmrNotificationHandler> handlers =
                new ArrayList<GlimmrNotificationHandler>();
        handlers.add(new ContactsPhotosNotificationHandler(this));
        handlers.add(new ActivityNotificationHandler(this));

        /* Start each handler */
        for (GlimmrNotificationHandler handler : handlers) {
            if (handler.enabledInPreferences()) {
                handler.startTask(oauth);
            }
        }
    }
}

package com.bourke.glimmr.services;

import android.net.ConnectivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.activities.MainActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.R;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

import com.jakewharton.notificationcompat2.NotificationCompat2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import android.net.NetworkInfo;

public class AppService extends WakefulIntentService {

    private static final String TAG = "Glimmr/AppService";

    public AppService() {
        super("AppService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        Log.d(TAG, "doWakefulWork");

        SharedPreferences prefs = getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        OAuth oauth = BaseActivity.loadAccessToken(prefs);
        if (oauth == null) {
            Log.e(TAG, "doWakefulWork: oauth from intent is null");
            return;
        }

        PhotoList photos = fetchPhotos(oauth);
        List<Photo> newPhotos = checkForNewPhotos(photos);
        if (newPhotos != null && !newPhotos.isEmpty()) {
            showNotification(newPhotos);
        }
    }

    protected void showNotification(List<Photo> newPhotos) {
        final NotificationManager mgr = (NotificationManager)
            getSystemService(NOTIFICATION_SERVICE);
        String tickerText = "Your Flickr contacts have posted new photos";
        String titleText = newPhotos.size()+" New photos";
        String contentText = "from your contacts.";
        Notification newContactsPhotos = getNotification(tickerText, titleText,
                contentText);
        mgr.notify(Constants.NOTIFICATION_NEW_CONTACTS_PHOTOS,
                newContactsPhotos);
    }

    private Notification getNotification(final String tickerText,
            final String titleText, final String contentText) {
        // TODO: make notification sound/vibrate configurable in preferences
        return new NotificationCompat2.Builder(this)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setTicker(tickerText)
            .setContentTitle(titleText)
            .setContentText(contentText)
            .setContentIntent(getPendingIntent())
            .build();
    }

    private PendingIntent getPendingIntent() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 0, i, 0);
    }

    protected PhotoList fetchPhotos(OAuth oauth) {
        OAuthToken token = oauth.getToken();
        Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());
        PhotoList photos = null;

        int amountToFetch = 20;
        Set<String> extras = null;
        boolean justFriends = false;
        boolean singlePhoto = false;
        boolean includeSelf = false;
        try {
            photos = f.getPhotosInterface().getContactsPhotos(amountToFetch,
                    extras, justFriends, singlePhoto, includeSelf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return photos;
    }

    protected List<Photo> checkForNewPhotos(PhotoList photos) {
        if (photos == null || photos.isEmpty()) {
            Log.d(TAG, "checkForNewPhotos: photos null or empty");
            return null;
        }

        List<Photo> newPhotos = new ArrayList<Photo>();
        SharedPreferences prefs = getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);

        /* Check if the most recent photo id we have stored is present in the
         * list of photos passed in.  If so, new photos are the sublist from 0
         * to the found id. */
        String newestId = getNewestPhotoId();
        if (newestId != null) {
            for (int i=0; i < photos.size(); i++) {
                Photo p = photos.get(i);
                if (p.getId().equals(newestId)) {
                    newPhotos = photos.subList(0, i);
                    Log.d(TAG, String.format("Found %d new photos",
                                newPhotos.size()));
                    break;
                }
            }
        }

        /* Update the newest photo id we know about */
        if (newPhotos != null && !newPhotos.isEmpty()) {
            storeNewestPhotoId(newPhotos.get(0));
        } else {
            Log.d(TAG, "newPhotos null or empty, using most recent " +
                    "fetched photo as newest");
            storeNewestPhotoId(photos.get(0));
        }

        return newPhotos;
    }

    protected String getNewestPhotoId() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        String newestId = prefs.getString(
                Constants.NOTIFICATION_NEWEST_CONTACT_PHOTO_ID, null);
        Log.d(TAG, String.format("getNewestPhotoId: newest is %s", newestId));
        return newestId;
    }

    protected void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.NOTIFICATION_NEWEST_CONTACT_PHOTO_ID,
                photo.getId());
        editor.commit();
        Log.d(TAG, "Updated most recent contact photo id to " + photo.getId());
    }
}

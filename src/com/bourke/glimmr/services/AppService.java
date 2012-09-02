package com.bourke.glimmr.services;

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

/**
 * TODO: This Service currently deals with new contact's photos.  It should
 * eventually be abstracted with subclasses to deal with other categories of
 * photos.
 */
public class AppService extends WakefulIntentService {

    private static final String TAG = "Glimmr/AppService";

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mPrefsEditor;

    public AppService() {
        super("AppService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (Constants.DEBUG)
            Log.d(TAG, "doWakefulWork");

        mPrefs = getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        mPrefsEditor = mPrefs.edit();
        OAuth oauth = BaseActivity.loadAccessToken(mPrefs);
        if (oauth == null) {
            if (Constants.DEBUG)
                Log.e(TAG, "doWakefulWork: oauth from intent is null");
            return;
        }

        PhotoList photos = fetchPhotos(oauth);
        List<Photo> newPhotos = checkForNewPhotos(photos);
        if (newPhotos != null && !newPhotos.isEmpty()) {
            /* Avoid duplicate notifications */
            String latestIdNotifiedAbout = getNewestNotificationPhotoId();
            Photo latestPhoto = newPhotos.get(0);
            if (!latestIdNotifiedAbout.equals(latestPhoto.getId())) {
                showNotification(newPhotos);
                storeNewestNotificationPhotoId(latestPhoto);
            }
        }
    }

    // TODO: move strings to strings.xml
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

    // TODO: make notification sound/vibrate configurable in preferences
    private Notification getNotification(final String tickerText,
            final String titleText, final String contentText) {
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

    /**
     * Passed to NotificationCompat2.Builder.setContentIntent to start
     * MainActivity when the notification is pressed.
     */
    private PendingIntent getPendingIntent() {
        if (Constants.DEBUG)
            Log.d(TAG, "getPendingIntent");
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

    /**
     * Check if the most recent photo id we have stored is present in the
     * list of photos passed in.  If so, new photos are the sublist from 0
     * to the found id.
     */
    protected List<Photo> checkForNewPhotos(PhotoList photos) {
        if (photos == null || photos.isEmpty()) {
            if (Constants.DEBUG)
                Log.d(TAG, "checkForNewPhotos: photos null or empty");
            return null;
        }

        List<Photo> newPhotos = new ArrayList<Photo>();
        String newestId = getNewestViewedPhotoId();
        for (int i=0; i < photos.size(); i++) {
            Photo p = photos.get(i);
            if (p.getId().equals(newestId)) {
                newPhotos = photos.subList(0, i);
                break;
            }
        }
        if (Constants.DEBUG)
            Log.d(TAG, String.format("Found %d new photos", newPhotos.size()));
        return newPhotos;
    }

    /**
     * Returns the id the of the most recent photo the user has viewed.
     */
    protected String getNewestViewedPhotoId() {
        String newestId = mPrefs.getString(
                Constants.NEWEST_CONTACT_PHOTO_ID, "");
        if (Constants.DEBUG)
            Log.d(TAG, "getNewestViewedPhotoId: " + newestId);
        return newestId;
    }

    /**
     * Returns the id the of the most recent photo in the list of photo's we've
     * notified about.
     */
    protected String getNewestNotificationPhotoId() {
        String newestId = mPrefs.getString(
                Constants.NOTIFICATION_NEWEST_CONTACT_PHOTO_ID, "");
        if (Constants.DEBUG)
            Log.d(TAG, "getNewestNotificationPhotoId: " + newestId);
        return newestId;
    }

    protected void storeNewestNotificationPhotoId(Photo photo) {
        mPrefsEditor.putString(Constants.NOTIFICATION_NEWEST_CONTACT_PHOTO_ID,
                photo.getId());
        mPrefsEditor.commit();
        if (Constants.DEBUG)
            Log.d(TAG, "Updated most recent contact photo id to " +
                    photo.getId() + " (notification)");
    }
}

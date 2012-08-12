package com.bourke.glimmr.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
            return;
        }

        List<Photo> newPhotos = checkForNewPhotos(photos);
        // TODO: if new photos, pop notification
        // ...
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
        String newestId = prefs.getString(Constants.NEWEST_CONTACT_PHOTO_ID,
                null);
        return newestId;
    }

    protected void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.NEWEST_CONTACT_PHOTO_ID, photo.getId());
        editor.commit();
        Log.d(TAG, "Updated most recent contact photo id to " + photo.getId());
    }
}

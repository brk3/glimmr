package com.bourke.glimmr.tasks;

import android.app.Activity;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;


public class LoadPhotoInfoTask extends AsyncTask<OAuth, Void, Photo> {

    private static final String TAG = "Glimmr/LoadPhotoInfoTask";

    private IPhotoInfoReadyListener mListener;
    private Photo mPhoto;
    private Activity mActivity;

    public LoadPhotoInfoTask(Activity a, IPhotoInfoReadyListener listener,
            Photo photo) {
        mActivity = a;
        mListener = listener;
        mPhoto = photo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((BaseActivity) mActivity).showProgressIcon(true);
    }

    @Override
    protected Photo doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        OAuthToken token = oauth.getToken();
        try {
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            return f.getPhotosInterface().getInfo(mPhoto.getId(),
                    mPhoto.getSecret());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Photo result) {
        if (result != null) {
            mListener.onPhotoInfoReady(result);
        } else {
            Log.e(TAG, "Error fetching photo info, result is null");
            // TODO: alert user / recover
        }
        ((BaseActivity) mActivity).showProgressIcon(false);
    }
}

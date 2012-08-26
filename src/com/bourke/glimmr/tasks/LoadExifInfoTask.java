package com.bourke.glimmr.tasks;

import android.app.Activity;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IExifInfoReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Exif;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.ArrayList;
import java.util.Collection;

public class LoadExifInfoTask
        extends AsyncTask<OAuth, Void, Collection<Exif>> {

    private static final String TAG = "Glimmr/LoadExifInfoTask";

    private IExifInfoReadyListener mListener;
    private Photo mPhoto;
    private Activity mActivity;

    public LoadExifInfoTask(Activity a, IExifInfoReadyListener listener,
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
    protected Collection<Exif> doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        OAuthToken token = oauth.getToken();
        try {
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            return f.getPhotosInterface().getExif(mPhoto.getId(),
                    mPhoto.getSecret());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Collection<Exif> result) {
        if (result != null) {
            mListener.onExifInfoReady(new ArrayList<Exif>(result));
        } else {
            if (Constants.DEBUG)
                Log.e(TAG, "Error fetching exif info, result is null");
            // TODO: alert user / recover
        }
        ((BaseActivity) mActivity).showProgressIcon(false);
    }
}

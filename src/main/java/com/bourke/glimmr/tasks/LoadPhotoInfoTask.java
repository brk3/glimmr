package com.bourke.glimmr.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.Photo;

import static junit.framework.Assert.*;

@SuppressWarnings("EmptyMethod")
public class LoadPhotoInfoTask extends AsyncTask<OAuth, Void, Photo> {

    private static final String TAG = "Glimmr/LoadPhotoInfoTask";

    private final IPhotoInfoReadyListener mListener;
    private final String mId;
    private final String mSecret;

    public LoadPhotoInfoTask(IPhotoInfoReadyListener listener, String id,
            String secret) {
        mListener = listener;
        mId = id;
        mSecret = secret;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Photo doInBackground(OAuth... params) {
        assertTrue(params.length > 0);

        OAuth oauth = params[0];

        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            try {
                Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                        token.getOauthToken(), token.getOauthTokenSecret());
                return f.getPhotosInterface().getInfo(mId, mSecret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (Constants.DEBUG) Log.d(TAG, "Unauthenticated call");
            try {
                return FlickrHelper.getInstance().getPhotosInterface()
                    .getInfo(mId, mSecret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Photo result) {
        if (result == null) {
            Log.e(TAG, "Error fetching photo info, result is null");
        }
        mListener.onPhotoInfoReady(result);
    }

    @Override
    protected void onCancelled(final Photo result) {
        if (Constants.DEBUG) Log.d(TAG, "onCancelled");
    }
}

package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;

public class LoadPhotoInfoTask extends AsyncTask<OAuth, Void, Photo> {

    private static final String TAG = "Glimmr/LoadPhotoInfoTask";

    private final IPhotoInfoReadyListener mListener;
    private final String mId;
    private final String mSecret;
    private Exception mException;

    public LoadPhotoInfoTask(IPhotoInfoReadyListener listener, String id,
            String secret) {
        mListener = listener;
        mId = id;
        mSecret = secret;
    }

    public LoadPhotoInfoTask(IPhotoInfoReadyListener listener, String id) {
        mListener = listener;
        mId = id;
        mSecret = null;
    }

    @Override
    protected Photo doInBackground(OAuth... params) {
        OAuth oauth = null;
        if (params.length > 0) {
            oauth = params[0];
        }
        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            try {
                Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                        token.getOauthToken(), token.getOauthTokenSecret());
                if (mSecret != null) {
                    return f.getPhotosInterface().getInfo(mId, mSecret);
                }
                return f.getPhotosInterface().getPhoto(mId);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Unauthenticated call");
            try {
                if (mSecret != null) {
                    return FlickrHelper.getInstance().getPhotosInterface()
                            .getInfo(mId, mSecret);
                }
                return FlickrHelper.getInstance().getPhotosInterface()
                        .getPhoto(mId);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Photo result) {
        mListener.onPhotoInfoReady(result, mException);
    }
}

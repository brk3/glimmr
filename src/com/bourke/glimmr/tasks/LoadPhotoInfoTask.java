package com.bourke.glimmr.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.Photo;

import static junit.framework.Assert.*;

public class LoadPhotoInfoTask extends AsyncTask<OAuth, Void, Photo> {

    private static final String TAG = "Glimmr/LoadPhotoInfoTask";

    private IPhotoInfoReadyListener mListener;
    private String mId;
    private String mSecret;
    private BaseFragment mBaseFragment;

    public LoadPhotoInfoTask(BaseFragment a, IPhotoInfoReadyListener listener,
            String id, String secret) {
        mBaseFragment = a;
        mListener = listener;
        mId = id;
        mSecret = secret;
    }

    public LoadPhotoInfoTask(IPhotoInfoReadyListener listener,
            String id, String secret) {
        mListener = listener;
        mId = id;
        mSecret = secret;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mBaseFragment != null) {
            mBaseFragment.showProgressIcon(true);
        }
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
        if (mBaseFragment != null) {
            mBaseFragment.showProgressIcon(false);
        }
    }

    @Override
    protected void onCancelled(final Photo result) {
        if (Constants.DEBUG) Log.d(TAG, "onCancelled");
    }
}

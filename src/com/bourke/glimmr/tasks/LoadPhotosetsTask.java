package com.bourke.glimmr.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotosetsReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photosets;

import java.io.IOException;
import java.util.Random;

public class LoadPhotosetsTask extends AsyncTask<OAuth, Void, Photosets> {

    private static final String TAG = "Glimmr/LoadPhotosetsTask";

    private IPhotosetsReadyListener mListener;
    private User mUser;
    private BaseFragment mBaseFragment;

    public LoadPhotosetsTask(BaseFragment a, IPhotosetsReadyListener listener,
            User user) {
        mBaseFragment = a;
        mListener = listener;
        mUser = user;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBaseFragment.showProgressIcon(true);
    }

    @Override
    protected Photosets doInBackground(OAuth... arg0) {
        OAuthToken token = arg0[0].getToken();
        Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());

        /* "Currently all sets are returned, but this behaviour may change in
         * future."
         * (http://www.flickr.com/services/api/flickr.photosets.getList.html)
         */
        try {
            return f.getPhotosetsInterface().getList(mUser.getId());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Photosets result) {
        if (result == null) {
            if (Constants.DEBUG)
                Log.e(TAG, "Error fetching photosets, result is null");
        }
        mListener.onPhotosetsReady(result);
        mBaseFragment.showProgressIcon(false);
    }
}

package com.bourke.glimmr.tasks;

import android.app.Activity;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotosetsReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photosets;

import java.io.IOException;

public class LoadPhotosetsTask extends AsyncTask<OAuth, Void, Photosets> {

    private static final String TAG = "Glimmr/LoadPhotosetsTask";

    private IPhotosetsReadyListener mListener;
    private Activity mActivity;

    public LoadPhotosetsTask(Activity a, IPhotosetsReadyListener listener) {
        mActivity = a;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((BaseActivity) mActivity).showProgressIcon(true);
    }

    @Override
    protected Photosets doInBackground(OAuth... arg0) {
        OAuthToken token = arg0[0].getToken();
        Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());
        User user = arg0[0].getUser();

        try {
            return f.getPhotosetsInterface().getList(user.getId());
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
        if (result != null) {
            mListener.onPhotosetsReady(result);
        } else {
            Log.e(TAG, "Error fetching photosets, result is null");
            // TODO: alert user / recover
        }
        ((BaseActivity) mActivity).showProgressIcon(false);
    }
}

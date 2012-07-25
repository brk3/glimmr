package com.bourke.glimmr.tasks;

import android.app.Activity;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IUserReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

public class LoadUserTask extends AsyncTask<OAuth, Void, User> {

    private static final String TAG = "Glimmr/LoadUserTask";

    private IUserReadyListener mListener;
    private User mUser;
    private Activity mActivity;

    public LoadUserTask(Activity a, IUserReadyListener listener, User user) {
        mActivity = a;
        mListener = listener;
        mUser = user;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((BaseActivity) mActivity).showProgressIcon(true);
    }

    @Override
    protected User doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        User user = oauth.getUser();
        OAuthToken token = oauth.getToken();

        try {
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            return f.getPeopleInterface().getInfo(mUser.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final User result) {
        if (result != null) {
            mListener.onUserReady(result);
        } else {
            Log.e(TAG, "Error fetching user info, result is null");
            // TODO: alert user / recover
        }
        ((BaseActivity) mActivity).showProgressIcon(false);
    }
}

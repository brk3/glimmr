package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IUserReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

@SuppressWarnings("EmptyMethod")
public class LoadUserTask extends AsyncTask<OAuth, Void, User> {

    private static final String TAG = "Glimmr/LoadUserTask";

    private final IUserReadyListener mListener;
    private final String mUserId;
    private Exception mException;

    public LoadUserTask(Activity a, IUserReadyListener listener,
            String userId) {
        mListener = listener;
        mUserId = userId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected User doInBackground(OAuth... params) {
        OAuth oauth = null;
        if (params.length > 0) {
            oauth = params[0];
        }
        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            try {
                Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                        token.getOauthToken(), token.getOauthTokenSecret());
                return f.getPeopleInterface().getInfo(mUserId);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Making unauthenticated call");
            try {
                return FlickrHelper.getInstance().getPeopleInterface()
                    .getInfo(mUserId);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final User result) {
        if (result == null) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error fetching user info, result is null");
            }
        }
        mListener.onUserReady(result, mException);
    }
}

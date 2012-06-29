package com.bourke.glimmr;

import android.os.AsyncTask;

import android.util.Log;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;
import android.app.Activity;

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
	protected User doInBackground(OAuth... params) {
		OAuth oauth = params[0];
		User user = oauth.getUser();
		OAuthToken token = oauth.getToken();

        ((BaseActivity) mActivity).showProgressIcon(true);

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
            boolean cancelled = false;
            mListener.onUserReady(result, cancelled);
		} else {
            Log.e(TAG, "Error fetching user info, result is null");
            // TODO: alert user / recover
        }
        ((BaseActivity) mActivity).showProgressIcon(false);
	}
}

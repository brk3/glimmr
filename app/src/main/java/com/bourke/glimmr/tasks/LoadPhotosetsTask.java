package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotosetsReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photosets;


@SuppressWarnings("EmptyMethod")
public class LoadPhotosetsTask extends AsyncTask<OAuth, Void, Photosets> {

    private static final String TAG = "Glimmr/LoadPhotosetsTask";

    private final IPhotosetsReadyListener mListener;
    private final User mUser;
    private Exception mException;

    public LoadPhotosetsTask(IPhotosetsReadyListener listener, User user) {
        mListener = listener;
        mUser = user;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Photosets doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());

            /* "Currently all sets are returned, but this behaviour may change
             * in future."
             * (http://flickr.com/services/api/flickr.photosets.getList.html)
             */
            try {
                return f.getPhotosetsInterface().getList(mUser.getId());
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Making unauthenticated call");
            try {
                return FlickrHelper.getInstance()
                    .getPhotosetsInterface().getList(mUser.getId());
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Photosets result) {
        if (result == null) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "Error fetching photosets, result is null");
        }
        mListener.onPhotosetsReady(result, mException);
    }
}

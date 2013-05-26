package com.bourke.glimmr.tasks;

import android.os.AsyncTask;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IAccessTokenReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthInterface;

/**
 * Gets an access token from Flickr once authorised to access the user's
 * account
 */
public class GetAccessTokenTask extends AsyncTask<String, Integer, OAuth> {

    private static final String TAG = "Glimmr/GetAccessTokenTask";

    private final IAccessTokenReadyListener mListener;

    public GetAccessTokenTask(IAccessTokenReadyListener listener) {
        mListener = listener;
    }

    @Override
    protected OAuth doInBackground(String... params) {
        String oauthToken = params[0];
        String oauthTokenSecret = params[1];
        String verifier = params[2];

        Flickr f = FlickrHelper.getInstance().getFlickr();
        OAuthInterface oauthApi = f.getOAuthInterface();
        try {
            return oauthApi.getAccessToken(oauthToken, oauthTokenSecret,
                    verifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(OAuth result) {
        mListener.onAccessTokenReady(result);
    }
}

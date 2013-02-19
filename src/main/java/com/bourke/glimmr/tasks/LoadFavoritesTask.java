package com.bourke.glimmrpro.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.Date;
import java.util.List;

public class LoadFavoritesTask extends AsyncTask<OAuth, Void, List<Photo>> {

    private static final String TAG = "Glimmr/LoadFavoritesTask";

    private IPhotoListReadyListener mListener;
    private User mUser;
    private int mPage;

    public LoadFavoritesTask(IPhotoListReadyListener listener, User user,
            int page) {
        mListener = listener;
        mUser = user;
        mPage = page;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Photo> doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            Date minFavDate = null;
            Date maxFavDate = null;
            if (Constants.DEBUG) Log.d(TAG, "Fetching page " + mPage);
            try {
                return f.getFavoritesInterface().getList(
                    mUser.getId(), minFavDate, maxFavDate,
                    Constants.FETCH_PER_PAGE, mPage, Constants.EXTRAS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "LoadFavoritesTask requires authentication");
        }
        return null;
    }

    @Override
    protected void onPostExecute(final List<Photo> result) {
        if (result == null) {
            Log.e(TAG, "Error fetching photolist, result is null");
        }
        mListener.onPhotosReady(result);
    }

    @Override
    protected void onCancelled(final List<Photo> result) {
        if (Constants.DEBUG) Log.d(TAG, "onCancelled");
    }
}

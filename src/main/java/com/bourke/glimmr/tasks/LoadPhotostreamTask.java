package com.bourke.glimmr.tasks;
import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoadPhotostreamTask extends AsyncTask<OAuth, Void, List<Photo>> {

    private static final String TAG = "Glimmr/LoadPhotostreamTask";

    private IPhotoListReadyListener mListener;
    private User mUser;
    private int mPage;

    public LoadPhotostreamTask(IPhotoListReadyListener listener,
            User user, int page) {
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
            if (Constants.DEBUG) Log.d(TAG, "Fetching page " + mPage);

            try {
                return f.getPeopleInterface().getPhotos(mUser.getId(),
                        Constants.EXTRAS, Constants.FETCH_PER_PAGE, mPage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "LoadPhotostreamTask requires authentication");
        }
        return null;
    }

    @Override
    protected void onPostExecute(final List<Photo> result) {
        if (result == null) {
            if (Constants.DEBUG)
                Log.e(TAG, "Error fetching photolist, result is null");
        }
        mListener.onPhotosReady(result);
    }

    @Override
    protected void onCancelled(final List<Photo> result) {
        if (Constants.DEBUG)
            Log.d(TAG, "onCancelled");
    }
}

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

import java.util.List;

public class LoadContactsPhotosTask
        extends AsyncTask<OAuth, Void, List<Photo>> {

    private static final String TAG = "Glimmr/LoadContactsPhotosTask";

    private IPhotoListReadyListener mListener;
    private int mPage;

    public LoadContactsPhotosTask(IPhotoListReadyListener listener, int page) {
        mListener = listener;
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
            User user = oauth.getUser();

            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            try {
                boolean justFriends = false;
                boolean singlePhoto = false;
                boolean includeSelf = false;
                return f.getPhotosInterface().getContactsPhotos(
                        Constants.FETCH_PER_PAGE, Constants.EXTRAS,
                        justFriends, singlePhoto, includeSelf, mPage,
                        Constants.FETCH_PER_PAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "LoadContactsPhotosTask requires authentication");
        }
        return null;
    }

    @Override
    protected void onPostExecute(final List<Photo> result) {
        if (result == null) {
            Log.e(TAG, "Error fetching contacts photos, result is null");
        }
        mListener.onPhotosReady(result);
    }

    @Override
    protected void onCancelled(final List<Photo> result) {
        if (Constants.DEBUG) Log.d(TAG, "onCancelled");
    }
}

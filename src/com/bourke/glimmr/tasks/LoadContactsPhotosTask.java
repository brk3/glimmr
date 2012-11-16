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

    public LoadContactsPhotosTask(IPhotoListReadyListener listener) {
        mListener = listener;
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
                /* Flickr api doc says max allowed is 50, but seems to allow
                 * more... For some reason pagination doesn't seem to be an
                 * option. */
                int amountToFetch = 50;
                boolean justFriends = false;
                boolean singlePhoto = false;
                boolean includeSelf = false;
                return f.getPhotosInterface().getContactsPhotos(amountToFetch,
                        Constants.EXTRAS, justFriends, singlePhoto,
                        includeSelf);
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

package com.bourke.glimmr.tasks;

import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IFavoriteReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;

@SuppressWarnings("EmptyMethod")
public class SetFavoriteTask extends AsyncTask<OAuth, Void, Exception> {

    private static final String TAG = "Glimmr/SetFavoriteTask";

    private final IFavoriteReadyListener mListener;
    private final Photo mPhoto;

    public SetFavoriteTask(IFavoriteReadyListener listener, Photo photo) {
        mListener = listener;
        mPhoto = photo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Exception doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            try {
                if (mPhoto.isFavorite()) {
                    f.getFavoritesInterface().remove(mPhoto.getId());
                } else {
                    f.getFavoritesInterface().add(mPhoto.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
        } else {
            Log.e(TAG, "SetFavoriteTask requires authentication");
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Exception result) {
        mListener.onFavoriteComplete(result);
    }
}

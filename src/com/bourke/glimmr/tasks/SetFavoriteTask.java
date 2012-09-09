package com.bourke.glimmr.tasks;

import android.os.AsyncTask;

import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IFavoriteReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;

public class SetFavoriteTask extends AsyncTask<OAuth, Void, Exception> {

    private static final String TAG = "Glimmr/SetFavoriteTask";

    private IFavoriteReadyListener mListener;
    private BaseFragment mBaseFragment;
    private Photo mPhoto;

    public SetFavoriteTask(BaseFragment a, IFavoriteReadyListener listener,
            Photo photo) {
        mBaseFragment = a;
        mListener = listener;
        mPhoto = photo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBaseFragment.showProgressIcon(true);
    }

    @Override
    protected Exception doInBackground(OAuth... arg0) {
        OAuthToken token = arg0[0].getToken();
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
        return null;
    }

    @Override
    protected void onPostExecute(final Exception result) {
        mListener.onFavoriteComplete(result);
        mBaseFragment.showProgressIcon(false);
    }
}

package com.bourke.glimmr.tasks;

import android.app.Activity;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IFavoriteReadyListener;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.PhotoList;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.gmail.yuyang226.flickr.photos.Photo;

public class SetFavoriteTask extends AsyncTask<OAuth, Void, Exception> {

    private static final String TAG = "Glimmr/SetFavoriteTask";

    private IFavoriteReadyListener mListener;
    private Activity mActivity;
    private Photo mPhoto;

    public SetFavoriteTask(Activity a, IFavoriteReadyListener listener,
            Photo photo) {
        mActivity = a;
        mListener = listener;
        mPhoto = photo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((BaseActivity) mActivity).showProgressIcon(true);
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
        ((BaseActivity) mActivity).showProgressIcon(false);
    }
}

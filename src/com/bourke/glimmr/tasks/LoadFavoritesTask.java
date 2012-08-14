package com.bourke.glimmr.tasks;

import android.app.Activity;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class LoadFavoritesTask extends AsyncTask<OAuth, Void, PhotoList> {

    private static final String TAG = "Glimmr/LoadFavoritesTask";

    private IPhotoListReadyListener mListener;
    private User mUser;
    private Activity mActivity;
    private int mPage;

    public LoadFavoritesTask(Activity a, IPhotoListReadyListener listener,
            User user, int page) {
        mActivity = a;
        mListener = listener;
        mUser = user;
        mPage = page;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((BaseActivity) mActivity).showProgressIcon(true);
    }

    @Override
    protected PhotoList doInBackground(OAuth... arg0) {
        OAuthToken token = arg0[0].getToken();
        Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());
        Date minFavDate = null;
        Date maxFavDate = null;
        Set<String> extras = new HashSet<String>();
        extras.add("owner_name");
        extras.add("url_q");
        extras.add("url_l");
        extras.add("views");
        Log.d(TAG, "Fetching page " + mPage);

        try {
            return f.getFavoritesInterface().getList(mUser.getId(), minFavDate,
                    maxFavDate, Constants.FETCH_PER_PAGE, mPage, extras);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final PhotoList result) {
        if (result == null) {
            Log.e(TAG, "Error fetching photolist, result is null");
        }
        mListener.onPhotosReady(result);
        ((BaseActivity) mActivity).showProgressIcon(false);
    }

    @Override
    protected void onCancelled(final PhotoList result) {
        Log.d(TAG, "onCancelled");
    }
}

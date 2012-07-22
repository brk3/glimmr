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

public class SetFavoriteTask extends AsyncTask<OAuth, Void, Exception> {

    private static final String TAG = "Glimmr/SetFavoriteTask";

    private IFavoriteReadyListener mListener;
    private Activity mActivity;

    public SetFavoriteTask(Activity a, IFavoriteReadyListener listener) {
        mActivity = a;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((BaseActivity) mActivity).showProgressIcon(true);
    }

    @Override
    protected Exception doInBackground(OAuth... arg0) {
        /*
        OAuthToken token = arg0[0].getToken();
        Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());
        Date minFavDate = null;
        Date maxFavDate = null;
        int perPage = 20;
        int page = 1;
        Set<String> extras = new HashSet<String>();
        extras.add("owner_name");
        extras.add("url_q");
        extras.add("url_l");
        extras.add("views");

        try {
            return f.getFavoritesInterface().getList(mUser.getId(), minFavDate,
                    maxFavDate, perPage, page, extras);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        return null;
    }

    @Override
    protected void onPostExecute(final Exception result) {
        /*
        if (result != null) {
            final boolean cancelled = false;
            mListener.onPhotosReady(result, cancelled);
        } else {
            Log.e(TAG, "error fetching photolist, result is null");
            // TODO: alert user / recover
        }
        ((BaseActivity) mActivity).showProgressIcon(false);
        */
    }
}

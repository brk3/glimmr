package com.bourke.glimmrpro.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.event.Events.IActivityItemsReadyListener;

import com.googlecode.flickrjandroid.activity.ItemList;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;

public class LoadFlickrActivityTask extends AsyncTask<OAuth, Void, ItemList> {

    private static final String TAG = "Glimmr/LoadFlickrActivityTask";

    private IActivityItemsReadyListener mListener;

    public LoadFlickrActivityTask(IActivityItemsReadyListener listener) {
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ItemList doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            try {
                Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                        token.getOauthToken(), token.getOauthTokenSecret());
                String timeFrame = "5d";
                int page = 1;
                return f.getActivityInterface().userPhotos(
                        Constants.FETCH_PER_PAGE, page, timeFrame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "LoadFlickrActivityTask requires authentication");
        }
        return null;
    }

    @Override
    protected void onPostExecute(final ItemList result) {
        if (result == null) {
            Log.e(TAG, "Error fetching activity items, result is null");
        }
        mListener.onItemListReady(result);
    }

    @Override
    protected void onCancelled(final ItemList result) {
        if (Constants.DEBUG) Log.d(TAG, "onCancelled");
    }
}

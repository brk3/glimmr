package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IActivityItemsReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.activity.Item;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;

import java.util.List;

@SuppressWarnings("EmptyMethod")
public class LoadFlickrActivityTask
        extends AsyncTask<OAuth, Void, List<Item>> {

    private static final String TAG = "Glimmr/LoadFlickrActivityTask";

    private final IActivityItemsReadyListener mListener;
    private Exception mException;

    public LoadFlickrActivityTask(IActivityItemsReadyListener listener) {
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Item> doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            try {
                Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                        token.getOauthToken(), token.getOauthTokenSecret());
                String timeFrame = "100d";
                int page = 1;
                return f.getActivityInterface().userPhotos(
                        Constants.FETCH_PER_PAGE, page, timeFrame);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        } else {
            Log.e(TAG, "LoadFlickrActivityTask requires authentication");
        }
        return null;
    }

    @Override
    protected void onPostExecute(final List<Item> result) {
        if (result == null) {
            Log.e(TAG, "Error fetching activity items, result is null");
        }
        mListener.onItemListReady(result, mException);
    }

    @Override
    protected void onCancelled(final List<Item> result) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCancelled");
    }
}

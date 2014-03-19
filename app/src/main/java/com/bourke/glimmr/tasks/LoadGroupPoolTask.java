package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

@SuppressWarnings("EmptyMethod")
public class LoadGroupPoolTask extends AsyncTask<OAuth, Void, List<Photo>> {

    private static final String TAG = "Glimmr/LoadGroupPoolTask";

    private final IPhotoListReadyListener mListener;
    private final Group mGroup;
    private final int mPage;
    private Exception mException;

    public LoadGroupPoolTask(IPhotoListReadyListener listener, Group group,
            int page) {
        mListener = listener;
        mGroup = group;
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
            if (BuildConfig.DEBUG) Log.d(TAG, "Fetching page " + mPage);
            try {
                return f.getPoolsInterface().getPhotos(mGroup.getId(), null,
                        Constants.EXTRAS, Constants.FETCH_PER_PAGE, mPage);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Making unauthenticated call");
            if (BuildConfig.DEBUG) Log.d(TAG, "Fetching page " + mPage);
            try {
                return FlickrHelper.getInstance().getPoolsInterface()
                    .getPhotos(mGroup.getId(), null, Constants.EXTRAS,
                            Constants.FETCH_PER_PAGE, mPage);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final List<Photo> result) {
        if (result == null) {
            Log.e(TAG, "error fetching photolist, result is null");
        }
        mListener.onPhotosReady(result, mException);
    }
}

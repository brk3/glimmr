package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

@SuppressWarnings("EmptyMethod")
public class LoadPhotostreamTask extends AsyncTask<OAuth, Void, List<Photo>> {

    private static final String TAG = "Glimmr/LoadPhotostreamTask";

    private final IPhotoListReadyListener mListener;
    private final User mUser;
    private final int mPage;
    private Exception mException;

    public LoadPhotostreamTask(IPhotoListReadyListener listener,
            User user, int page) {
        mListener = listener;
        mUser = user;
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
                return f.getPeopleInterface().getPhotos(mUser.getId(),
                        Constants.EXTRAS, Constants.FETCH_PER_PAGE, mPage);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        } else {
            try {
                return FlickrHelper.getInstance().getPeopleInterface().getPublicPhotos(mUser.getId(),
                        Constants.EXTRAS, Constants.FETCH_PER_PAGE, mPage);
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
            if (BuildConfig.DEBUG)
                Log.e(TAG, "Error fetching photolist, result is null");
        }
        mListener.onPhotosReady(result, mException);
    }

    @Override
    protected void onCancelled(final List<Photo> result) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onCancelled");
    }
}

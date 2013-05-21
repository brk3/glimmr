package com.bourke.glimmr.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("EmptyMethod")
public class LoadPhotosetPhotosTask extends AsyncTask<OAuth, Void, List<Photo>> {

    private static final String TAG = "Glimmr/LoadPhotosetTask";

    private final IPhotoListReadyListener mListener;
    private final Photoset mPhotoset;
    private final int mPage;

    public LoadPhotosetPhotosTask(IPhotoListReadyListener listener,
                                  Photoset photoset, int page) {
        mListener = listener;
        mPhotoset = photoset;
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
            if (Constants.DEBUG) Log.d(TAG, "Fetching page " + mPage);
            try {
                return f.getPhotosetsInterface().getPhotos(
                        ""+mPhotoset.getId(), Constants.EXTRAS,
                        Flickr.PRIVACY_LEVEL_NO_FILTER,
                        Constants.FETCH_PER_PAGE, mPage).getPhotoList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (Constants.DEBUG) Log.d(TAG, "Making unauthenticated call");
            if (Constants.DEBUG) Log.d(TAG, "Fetching page " + mPage);
            try {
                return FlickrHelper.getInstance().getPhotosetsInterface()
                    .getPhotos(""+mPhotoset.getId(), Constants.EXTRAS,
                            Flickr.PRIVACY_LEVEL_NO_FILTER,
                            Constants.FETCH_PER_PAGE, mPage).getPhotoList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Photo> result) {
        if (result == null) {
            if (Constants.DEBUG) {
                Log.e(TAG, "Error fetching photolist, result is null");
            }
            result = Collections.EMPTY_LIST;
        }
        mListener.onPhotosReady(result);
    }

    @Override
    protected void onCancelled(final List<Photo> result) {
        if (Constants.DEBUG) Log.d(TAG, "onCancelled");
    }
}

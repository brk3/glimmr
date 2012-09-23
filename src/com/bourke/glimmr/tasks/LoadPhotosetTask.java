package com.bourke.glimmr.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.HashSet;
import java.util.Set;

public class LoadPhotosetTask extends AsyncTask<OAuth, Void, PhotoList> {

    private static final String TAG = "Glimmr/LoadPhotosetTask";

    private IPhotoListReadyListener mListener;
    private Photoset mPhotoset;
    private BaseFragment mBaseFragment;
    private int mPage;

    public LoadPhotosetTask(BaseFragment a, IPhotoListReadyListener listener,
            Photoset photoset, int page) {
        mBaseFragment = a;
        mListener = listener;
        mPhotoset = photoset;
        mPage = page;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBaseFragment.showProgressIcon(true);
    }

    @Override
    protected PhotoList doInBackground(OAuth... arg0) {
        OAuthToken token = arg0[0].getToken();
        Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());
        Set<String> extras = new HashSet<String>();
        extras.add("owner_name");
        extras.add("url_q");
        extras.add("url_l");
        extras.add("views");
        if (Constants.DEBUG) {
            Log.d(TAG, "Fetching page " + mPage);
        }

        try {
            return f.getPhotosetsInterface().getPhotos(""+mPhotoset.getId(),
                    extras, Flickr.PRIVACY_LEVEL_NO_FILTER,
                    Constants.FETCH_PER_PAGE, mPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(PhotoList result) {
        if (result == null) {
            if (Constants.DEBUG) {
                Log.e(TAG, "Error fetching photolist, result is null");
            }
            result = new PhotoList();
        }
        mListener.onPhotosReady(result);
        mBaseFragment.showProgressIcon(false);
    }

    @Override
    protected void onCancelled(final PhotoList result) {
        if (Constants.DEBUG)
            Log.d(TAG, "onCancelled");
    }
}

package com.bourke.glimmr.tasks;

import android.app.Activity;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.IPhotoListReadyListener;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.photos.PhotoList;

import java.util.HashSet;
import java.util.Set;
import com.gmail.yuyang226.flickr.photosets.Photoset;

public class LoadPhotosetTask extends AsyncTask<OAuth, Void, PhotoList> {

    private static final String TAG = "Glimmr/LoadPhotosetTask";

    private IPhotoListReadyListener mListener;
    private Photoset mPhotoset;
    private Activity mActivity;

    public LoadPhotosetTask(Activity a, IPhotoListReadyListener listener,
            Photoset photoset) {
        mActivity = a;
        mListener = listener;
        mPhotoset = photoset;
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
        Set<String> extras = new HashSet<String>();
        extras.add("owner_name");
        extras.add("url_q");
        extras.add("url_l");
        extras.add("views");

        final int perPage = 20;
        int page = 1;

        try {
            return f.getPhotosetsInterface().getPhotos(""+mPhotoset.getId(),
                    extras, Flickr.PRIVACY_LEVEL_NO_FILTER, perPage, page);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final PhotoList result) {
        if (result != null) {
            final boolean cancelled = false;
            mListener.onPhotosReady(result, cancelled);
        } else {
            Log.e(TAG, "error fetching photolist, result is null");
            // TODO: alert user / recover
        }
        ((BaseActivity) mActivity).showProgressIcon(false);
    }
}

package com.bourke.glimmr.tasks;



import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;

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

    public LoadPhotosetTask(BaseFragment a, IPhotoListReadyListener listener,
            Photoset photoset) {
        mBaseFragment = a;
        mListener = listener;
        mPhotoset = photoset;
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
        if (result == null) {
            if (Constants.DEBUG)
                Log.e(TAG, "Error fetching photolist, result is null");
        }
        mListener.onPhotosReady(result);
        mBaseFragment.showProgressIcon(false);
    }
}

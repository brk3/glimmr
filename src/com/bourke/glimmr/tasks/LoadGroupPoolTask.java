package com.bourke.glimmr.tasks;

import android.app.Activity;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.HashSet;
import java.util.Set;

public class LoadGroupPoolTask extends AsyncTask<OAuth, Void, PhotoList> {

    private static final String TAG = "Glimmr/LoadGroupPoolTask";

    private IPhotoListReadyListener mListener;
    private Group mGroup;
    private Activity mActivity;
    private int mPage;

    public LoadGroupPoolTask(Activity a, IPhotoListReadyListener listener,
            Group group, int page) {
        mActivity = a;
        mListener = listener;
        mGroup = group;
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
        Set<String> extras = new HashSet<String>();
        extras.add("owner_name");
        extras.add("url_q");
        extras.add("url_l");
        extras.add("views");
        if (Constants.DEBUG)
            Log.d(TAG, "Fetching page " + mPage);

        try {
            return f.getPoolsInterface().getPhotos(mGroup.getId(), null,
                    extras, Constants.FETCH_PER_PAGE, mPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final PhotoList result) {
        if (result == null) {
            if (Constants.DEBUG)
                Log.e(TAG, "error fetching photolist, result is null");
        }
        mListener.onPhotosReady(result);
        ((BaseActivity) mActivity).showProgressIcon(false);
    }
}

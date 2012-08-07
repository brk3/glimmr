package com.bourke.glimmr.tasks;

import android.os.AsyncTask;

import com.bourke.glimmr.common.Constants;
import android.util.Log;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.HashSet;
import java.util.Set;
import android.app.Activity;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.FlickrHelper;

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
        if (result != null) {
            mListener.onPhotosReady(result);
        } else {
            Log.e(TAG, "error fetching photolist, result is null");
            // TODO: alert user / recover
        }
        ((BaseActivity) mActivity).showProgressIcon(false);
    }
}

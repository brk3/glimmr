package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IGroupListReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("EmptyMethod")
public class LoadGroupsTask extends AsyncTask<OAuth, Void, Collection<Group>> {

    private static final String TAG = "Glimmr/LoadGroupsTask";

    private final IGroupListReadyListener mListener;
    private Exception mException;

    public LoadGroupsTask(IGroupListReadyListener listener) {
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Collection<Group> doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            try {
                return f.getPoolsInterface().getGroups();
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        } else {
            Log.e(TAG, "LoadGroupsTask requires authentication");
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Collection<Group> result) {
        List<Group> ret = new ArrayList<Group>();
        if (result == null) {
            Log.e(TAG, "Error fetching groups, result is null");
        } else {
            ret.addAll(result);
        }
        mListener.onGroupListReady(ret, mException);
    }

    @Override
    protected void onCancelled(final Collection<Group> result) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "onCancelled");
    }
}

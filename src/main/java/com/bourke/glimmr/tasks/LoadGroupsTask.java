package com.bourke.glimmr.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IGroupListReadyListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("EmptyMethod")
public class LoadGroupsTask extends AsyncTask<OAuth, Void, Collection<Group>> {

    private static final String TAG = "Glimmr/LoadGroupsTask";

    private final IGroupListReadyListener mListener;

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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FlickrException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
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
        mListener.onGroupListReady(ret);
    }

    @Override
    protected void onCancelled(final Collection<Group> result) {
        if (Constants.DEBUG)
            Log.d(TAG, "onCancelled");
    }
}

package com.bourke.glimmr;

import android.os.AsyncTask;

import android.util.Log;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.FlickrException;
import com.gmail.yuyang226.flickr.groups.Group;
import com.gmail.yuyang226.flickr.groups.GroupList;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;

import java.io.IOException;

import java.util.Collection;
import android.app.Activity;

public class LoadGroupsTask extends AsyncTask<OAuth, Void, Collection<Group>> {

    private static final String TAG = "Glimmr/LoadGroupsTask";

    private IGroupListReadyListener mListener;
    private Activity mActivity;

	public LoadGroupsTask(Activity a, IGroupListReadyListener listener) {
        mActivity = a;
        mListener = listener;
	}

	@Override
	protected Collection<Group> doInBackground(OAuth... arg0) {
		OAuthToken token = arg0[0].getToken();
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());
		User user = arg0[0].getUser();

        ((BaseActivity) mActivity).showProgressIcon(true);

		try {
			return f.getPoolsInterface().getGroups();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(final Collection<Group> result) {
		if (result != null) {
            boolean cancelled = false;
            GroupList ret = new GroupList();
            ret.addAll(result);
            mListener.onGroupListReady(ret, cancelled);
		} else {
            Log.e(TAG, "Error fetching groups, result is null");
            // TODO: alert user / recover
        }
        ((BaseActivity) mActivity).showProgressIcon(false);
	}
}

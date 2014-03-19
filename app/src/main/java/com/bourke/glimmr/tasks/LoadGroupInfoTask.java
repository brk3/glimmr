package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IGroupInfoReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;

public class LoadGroupInfoTask extends AsyncTask<OAuth, Void, Group> {

    private static final String TAG = "Glimmr/LoadGroupInfoTask";

    private final IGroupInfoReadyListener mListener;
    private final String mGroupId;
    private Exception mException;

    public LoadGroupInfoTask(String groupId,
            IGroupInfoReadyListener listener) {
        mGroupId = groupId;
        mListener = listener;
    }

    @Override
    protected Group doInBackground(OAuth... params) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Starting LoadGroupInfoTask");
        OAuth oauth = params[0];
        if (oauth != null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Authenticated call");
            OAuthToken token = oauth.getToken();
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            try {
                return f.getGroupsInterface().getInfo(mGroupId);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Unauthenticated call");
            try {
                return FlickrHelper.getInstance().getGroupsInterface()
                    .getInfo(mGroupId);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Group result) {
        mListener.onGroupInfoReady(result, mException);
    }
}

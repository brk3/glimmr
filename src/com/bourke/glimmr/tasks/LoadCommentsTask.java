package com.bourke.glimmrpro.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.event.Events.ICommentsReadyListener;
import com.bourke.glimmrpro.fragments.base.BaseFragment;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.comments.Comment;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.Date;
import java.util.List;

public class LoadCommentsTask
        extends AsyncTask<OAuth, Void, List<Comment>> {

    private static final String TAG = "Glimmr/LoadCommentsTask";

    private ICommentsReadyListener mListener;
    private Photo mPhoto;
    private BaseFragment mBaseFragment;

    public LoadCommentsTask(BaseFragment a, ICommentsReadyListener listener,
            Photo photo) {
        mBaseFragment = a;
        mListener = listener;
        mPhoto = photo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBaseFragment.showProgressIcon(true);
    }

    @Override
    protected List<Comment> doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        OAuthToken token = oauth.getToken();
        try {
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            Date minCommentDate = null;
            Date maxCommentDate = null;
            return f.getCommentsInterface().getList(mPhoto.getId(),
                    minCommentDate, maxCommentDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final List<Comment> result) {
        if (result == null) {
            if (Constants.DEBUG)
                Log.e(TAG, "Error fetching comments, result is null");
        }
        mListener.onCommentsReady(result);
        mBaseFragment.showProgressIcon(false);
    }

    @Override
    protected void onCancelled(final List<Comment> result) {
        if (Constants.DEBUG)
            Log.d(TAG, "onCancelled");
    }
}

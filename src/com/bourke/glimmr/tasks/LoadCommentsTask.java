package com.bourke.glimmr.tasks;

import android.app.Activity;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.ICommentsReadyListener;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.photos.comments.Comment;
import com.gmail.yuyang226.flickr.photos.Photo;

import java.util.Date;
import java.util.List;

public class LoadCommentsTask
        extends AsyncTask<OAuth, Void, List<Comment>> {

    private static final String TAG = "Glimmr/LoadCommentsTask";

    private ICommentsReadyListener mListener;
    private Photo mPhoto;
    private Activity mActivity;

    public LoadCommentsTask(Activity a, ICommentsReadyListener listener,
            Photo photo) {
        mActivity = a;
        mListener = listener;
        mPhoto = photo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((BaseActivity) mActivity).showProgressIcon(true);
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
        if (result != null) {
            boolean cancelled = false;
            mListener.onCommentsReady(result, cancelled);
        } else {
            Log.e(TAG, "Error fetching comments, result is null");
            // TODO: alert user / recover
        }
        ((BaseActivity) mActivity).showProgressIcon(false);
    }
}

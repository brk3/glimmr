package com.bourke.glimmr.tasks;

import android.app.Activity;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.ICommentAddedListener;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;

public class AddCommentTask extends AsyncTask<OAuth, Void, String> {

    private static final String TAG = "Glimmr/AddCommentTask";

    private ICommentAddedListener mListener;
    private Photo mPhoto;
    private String mCommentText;
    private Activity mActivity;

    public AddCommentTask(Activity a, ICommentAddedListener listener,
            Photo photo, String commentText) {
        mActivity = a;
        mListener = listener;
        mPhoto = photo;
        mCommentText = commentText;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // TODO: show progress icon
    }

    @Override
    protected String doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        OAuthToken token = oauth.getToken();
        try {
            Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                    token.getOauthToken(), token.getOauthTokenSecret());
            return f.getCommentsInterface().addComment(mPhoto.getId(),
                    mCommentText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final String result) {
        if (result == null) {
            Log.e(TAG, "Error adding comment, result is null");
        }
        mListener.onCommentAdded(result);
    }

    @Override
    protected void onCancelled(final String result) {
        Log.d(TAG, "onCancelled");
    }
}

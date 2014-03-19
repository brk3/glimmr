package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.ICommentAddedListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;

@SuppressWarnings("EmptyMethod")
public class AddCommentTask extends AsyncTask<OAuth, Void, String> {

    private static final String TAG = "Glimmr/AddCommentTask";

    private final ICommentAddedListener mListener;
    private final Photo mPhoto;
    private final String mCommentText;
    private Exception mException;

    public AddCommentTask(ICommentAddedListener listener,
            Photo photo, String commentText) {
        mListener = listener;
        mPhoto = photo;
        mCommentText = commentText;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            try {
                Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                        token.getOauthToken(), token.getOauthTokenSecret());
                return f.getCommentsInterface().addComment(mPhoto.getId(),
                        mCommentText);
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        } else {
            Log.e(TAG, "AddCommentTask requires authentication");
        }
        return null;
    }

    @Override
    protected void onPostExecute(final String result) {
        if (result == null) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error adding comment, result is null");
            }
        }
        mListener.onCommentAdded(result, mException);
    }

    @Override
    protected void onCancelled(final String result) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCancelled");
    }
}

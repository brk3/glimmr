package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IExifInfoReadyListener;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Exif;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("EmptyMethod")
public class LoadExifInfoTask
        extends AsyncTask<OAuth, Void, Collection<Exif>> {

    private static final String TAG = "Glimmr/LoadExifInfoTask";

    private final IExifInfoReadyListener mListener;
    private final Photo mPhoto;
    private Exception mException = null;

    public LoadExifInfoTask(IExifInfoReadyListener listener, Photo photo) {
        mListener = listener;
        mPhoto = photo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Collection<Exif> doInBackground(OAuth... params) {
        OAuth oauth = params[0];
        if (oauth != null) {
            OAuthToken token = oauth.getToken();
            try {
                Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                        token.getOauthToken(), token.getOauthTokenSecret());
                return f.getPhotosInterface().getExif(mPhoto.getId(),
                        mPhoto.getSecret());
            } catch (Exception e) {
                mException = e;
                e.printStackTrace();
            }
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Making unauthenticated call");
            try {
                return FlickrHelper.getInstance().getPhotosInterface()
                    .getExif(mPhoto.getId(), mPhoto.getSecret());
            } catch (Exception e) {
                mException = e;
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Collection<Exif> result) {
        if (result != null) {
            mListener.onExifInfoReady(new ArrayList<Exif>(result), mException);
        } else {
            mListener.onExifInfoReady(new ArrayList<Exif>(), mException);
        }
    }
}

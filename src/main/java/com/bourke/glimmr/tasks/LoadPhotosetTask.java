package com.bourke.glimmr.tasks;

import android.os.AsyncTask;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.photosets.Photoset;

public class LoadPhotosetTask extends AsyncTask<OAuth, Void, Photoset> {

    private static final String TAG = "Glimmr/LoadPhotosetTask";

    private final Events.IPhotosetReadyListener mListener;
    private final String mId;
    private Exception mException;

    public LoadPhotosetTask(Events.IPhotosetReadyListener listener,
                            String id) {
        mListener = listener;
        mId = id;
    }

    @Override
    protected Photoset doInBackground(OAuth... params) {
        try {
            return FlickrHelper.getInstance().getPhotosetsInterface()
                    .getInfo(mId);
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Photoset result) {
        mListener.onPhotosetReady(result, mException);
    }
}

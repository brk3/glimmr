package com.bourke.glimmrpro.tasks;

import android.os.AsyncTask;
import com.bourke.glimmrpro.event.Events;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.photosets.Photoset;
import org.json.JSONException;

import java.io.IOException;

public class LoadPhotosetTask extends AsyncTask<OAuth, Void, Photoset> {

    private static final String TAG = "Glimmr/LoadPhotosetTask";

    private final Events.IPhotosetReadyListener mListener;
    private final String mId;

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
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Photoset result) {
        mListener.onPhotosetReady(result);
    }
}

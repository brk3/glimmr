package com.bourke.glimmrpro.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.event.Events.IPhotoSizesReadyListener;

import com.googlecode.flickrjandroid.photos.Size;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.*;

public class LoadPhotoSizesTask
        extends AsyncTask<Void, Void, Collection<Size>> {

    private static final String TAG = "Glimmr/LoadPhotoSizesTask";

    private IPhotoSizesReadyListener mListener;
    private String mId;

    public LoadPhotoSizesTask(IPhotoSizesReadyListener listener, String id) {
        mListener = listener;
        mId = id;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Collection<Size> doInBackground(Void... params) {
        try {
            return FlickrHelper.getInstance().getPhotosInterface()
                .getSizes(mId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Collection<Size> result) {
        if (result == null) {
            Log.e(TAG, "Error fetching photo sizes, result is null");
        }
        List<Size> ret = new ArrayList<Size>();
        ret.addAll(result);
        mListener.onPhotoSizesReady(ret);
    }
}

package com.bourke.glimmr.tasks;

import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events.IPhotoSizesReadyListener;
import com.googlecode.flickrjandroid.photos.Size;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("EmptyMethod")
public class LoadPhotoSizesTask
        extends AsyncTask<Void, Void, Collection<Size>> {

    private static final String TAG = "Glimmr/LoadPhotoSizesTask";

    private final IPhotoSizesReadyListener mListener;
    private final String mId;
    private Exception mException;

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
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Collection<Size> result) {
        List<Size> ret = new ArrayList<Size>();
        if (result != null) {
            ret.addAll(result);
        } else {
            Log.e(TAG, "Error fetching photo sizes, result is null");
        }
        mListener.onPhotoSizesReady(ret, mException);
    }
}

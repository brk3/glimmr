package com.bourke.glimmrpro.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;

import com.googlecode.flickrjandroid.photos.Photo;

import java.util.Date;
import java.util.List;

@SuppressWarnings("EmptyMethod")
public class LoadPublicPhotosTask extends AsyncTask<Void, Void, List<Photo>> {

    private static final String TAG = "Glimmr/LoadPublicPhotosTask";

    private final IPhotoListReadyListener mListener;
    private final int mPage;

    public LoadPublicPhotosTask(IPhotoListReadyListener listener, int page) {
        mListener = listener;
        mPage = page;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Photo> doInBackground(Void... arg0) {
        if (Constants.DEBUG) Log.d(TAG, "Fetching page " + mPage);

        /* A specific date to return interesting photos for. */
        Date day = null;
        try {
            return FlickrHelper.getInstance().getInterestingInterface()
                .getList(day, Constants.EXTRAS, Constants.FETCH_PER_PAGE,
                        mPage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(final List<Photo> result) {
        if (result == null) {
            Log.e(TAG, "Error fetching photolist, result is null");
        }
        mListener.onPhotosReady(result);
    }

    @Override
    protected void onCancelled(final List<Photo> result) {
        if (Constants.DEBUG) Log.d(TAG, "onCancelled");
    }
}

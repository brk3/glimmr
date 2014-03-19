package com.bourke.glimmr.tasks;

import com.bourke.glimmr.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.Events;

public class LoadGroupIdTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "Glimmr/LoadGroupIdTask";

    private final Events.IGroupIdReadyListener mListener;
    private final String mUrl;
    private Exception mException;

    public LoadGroupIdTask(Events.IGroupIdReadyListener listener, String url) {
        mListener = listener;
        mUrl = url;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            if (BuildConfig.DEBUG) Log.d(TAG, "Fetching id for " + mUrl);
            return FlickrHelper.getInstance().getUrlsInterface()
                    .lookupGroup(mUrl).getId();
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(final String result) {
        mListener.onGroupIdReady(result, mException);
    }
}

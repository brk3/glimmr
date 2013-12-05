package com.bourke.glimmrpro.tasks;

import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.event.Events;

public class LoadProfileIdTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "Glimmr/LoadProfileIdTask";

    private final Events.IProfileIdReadyListener mListener;
    private final String mUrl;
    private Exception mException;

    public LoadProfileIdTask(Events.IProfileIdReadyListener listener,
            String url) {
        mListener = listener;
        mUrl = url;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            if (Constants.DEBUG) Log.d(TAG, "Fetching id for " + mUrl);
            return FlickrHelper.getInstance().getUrlsInterface()
                    .lookupUser(mUrl).getId();
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(final String result) {
        mListener.onProfileIdReady(result, mException);
    }
}

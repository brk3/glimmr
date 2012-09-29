package com.bourke.glimmrpro.tasks;

import android.os.AsyncTask;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;
import com.bourke.glimmrpro.fragments.base.BaseFragment;

import com.googlecode.flickrjandroid.photos.PhotoList;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class LoadPublicPhotosTask extends AsyncTask<Void, Void, PhotoList> {

    private static final String TAG = "Glimmr/LoadPublicPhotosTask";

    private IPhotoListReadyListener mListener;
    private BaseFragment mBaseFragment;
    private int mPage;

    public LoadPublicPhotosTask(BaseFragment a,
            IPhotoListReadyListener listener, int page) {
        mListener = listener;
        mBaseFragment = a;
        mPage = page;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBaseFragment.showProgressIcon(true);
    }

    @Override
    protected PhotoList doInBackground(Void... arg0) {
        if (Constants.DEBUG) Log.d(TAG, "Fetching page " + mPage);

        /* A specific date to return interesting photos for. */
        Date day = null;
        Set<String> extras = new HashSet<String>();
        extras.add("owner_name");
        extras.add("url_q");
        extras.add("url_l");
        extras.add("views");

        try {
            return FlickrHelper.getInstance().getInterestingInterface()
                .getList(day, extras, Constants.FETCH_PER_PAGE, mPage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(final PhotoList result) {
        if (result == null) {
            if (Constants.DEBUG) {
                Log.e(TAG, "Error fetching photolist, result is null");
            }
        }
        mListener.onPhotosReady(result);
        mBaseFragment.showProgressIcon(false);
    }

    @Override
    protected void onCancelled(final PhotoList result) {
        if (Constants.DEBUG) Log.d(TAG, "onCancelled");
    }
}

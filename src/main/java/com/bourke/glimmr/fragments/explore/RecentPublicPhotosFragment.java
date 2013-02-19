package com.bourke.glimmrpro.fragments.explore;

import android.os.Bundle;

import com.bourke.glimmrpro.fragments.base.PhotoGridFragment;
import com.bourke.glimmrpro.tasks.LoadPublicPhotosTask;

import com.googlecode.flickrjandroid.photos.Photo;

public class RecentPublicPhotosFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/RecentPublicPhotosFragment";

    protected LoadPublicPhotosTask mTask;

    public static RecentPublicPhotosFragment newInstance() {
        return new RecentPublicPhotosFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowDetailsOverlay = false;
    }

    /**
     * Once the parent binds the adapter it will trigger cacheInBackground
     * for us as it will be empty when first bound.
     */
    @Override
    protected boolean cacheInBackground() {
        startTask(mPage++);
        return mMorePages;
    }

    private void startTask(int page) {
        super.startTask();
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadPublicPhotosTask(this, page);
        mTask.execute();
    }

    @Override
    public String getNewestPhotoId() {
        /* Won't be notifying about new public photos */
        return null;
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

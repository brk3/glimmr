package com.bourke.glimmr.fragments.explore;

import android.os.Bundle;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPublicPhotosTask;
import com.googlecode.flickrjandroid.photos.Photo;

public class RecentPublicPhotosFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/RecentPublicPhotosFragment";

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
        mActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
        new LoadPublicPhotosTask(this, page).execute();
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

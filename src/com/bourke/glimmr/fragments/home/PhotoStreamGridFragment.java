package com.bourke.glimmr.fragments.home;

import com.bourke.glimmr.event.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPhotostreamTask;

import com.gmail.yuyang226.flickr.photos.PhotoList;

public class PhotoStreamGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotoStreamGridFragment";

    public static PhotoStreamGridFragment newInstance() {
        return new PhotoStreamGridFragment();
    }

    @Override
    protected void startTask() {
        /**
         * Once the parent binds the adapter it will trigger cacheInBackground
         * for us as it will be empty when first bound.  So we don't need to
         * respond to the Activities calls to startTask().
         */
    }

    @Override
    protected boolean cacheInBackground() {
        startTask(mPage++);
        return mMorePages;
    }

    private void startTask(int page) {
        super.startTask();
        new LoadPhotostreamTask(mActivity, this, mOAuth.getUser(), page)
            .execute(mOAuth);
    }

    @Override
    public void onPhotosReady(PhotoList photos, boolean cancelled) {
        super.onPhotosReady(photos, cancelled);
        if (photos.isEmpty()) {
            mMorePages = false;
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

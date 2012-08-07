package com.bourke.glimmr.fragments.home;

import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPhotostreamTask;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class PhotoStreamGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotoStreamGridFragment";

    private LoadPhotostreamTask mTask;

    public static PhotoStreamGridFragment newInstance(User user) {
        PhotoStreamGridFragment newFragment = new PhotoStreamGridFragment();
        newFragment.mUser = user;
        return newFragment;
    }

    /**
     * Once the parent binds the adapter it will trigger cacheInBackground
     * for us as it will be empty when first bound.  So we don't need to
     * override startTask().
     */
    @Override
    protected boolean cacheInBackground() {
        startTask(mPage++);
        return mMorePages;
    }

    private void startTask(int page) {
        super.startTask();
        mTask = new LoadPhotostreamTask(mActivity, this, mUser, page);
        mTask.execute(mOAuth);
    }

    @Override
    public void onPhotosReady(PhotoList photos) {
        super.onPhotosReady(photos);
        if (photos.isEmpty()) {
            mMorePages = false;
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

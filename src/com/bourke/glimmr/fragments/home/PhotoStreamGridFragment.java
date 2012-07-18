package com.bourke.glimmr.fragments.home;

import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.event.IPhotoListReadyListener;
import com.bourke.glimmr.tasks.LoadPhotostreamTask;
import com.gmail.yuyang226.flickr.photos.PhotoList;
import android.util.Log;

public class PhotoStreamGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotoStreamGridFragment";

    public static PhotoStreamGridFragment newInstance() {
        return new PhotoStreamGridFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadPhotostreamTask(mActivity, this, mOAuth.getUser(), mPage++)
            .execute(mOAuth);
    }

    @Override
    protected boolean cacheInBackground() {
        startTask();
        return mMorePages;
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

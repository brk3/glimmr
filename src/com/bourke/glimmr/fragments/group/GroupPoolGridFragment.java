package com.bourke.glimmr.fragments.group;

import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadGroupPoolTask;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.PhotoList;
import android.os.Bundle;

public class GroupPoolGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/GroupPoolGridFragment";

    private Group mGroup;
    private LoadGroupPoolTask mTask;

    public static GroupPoolGridFragment newInstance(Group group, User user) {
        GroupPoolGridFragment newFragment = new GroupPoolGridFragment();
        newFragment.mGroup = group;
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
        mTask = new LoadGroupPoolTask(mActivity, this, mGroup, page);
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

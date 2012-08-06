package com.bourke.glimmr.fragments.group;

import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadGroupPoolTask;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.people.User;
import android.util.Log;

public class GroupPoolGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/GroupPoolGridFragment";

    private Group mGroup;
    private LoadGroupPoolTask mTask;

    public static GroupPoolGridFragment newInstance(Group group, User user) {
        GroupPoolGridFragment newFragment = new GroupPoolGridFragment();
        newFragment.mGroup = group;
        newFragment.mUser = user;
        return newFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTask != null) {
            mTask.cancel(true);
            Log.d(TAG, "onPause: cancelling task");
        }
    }

    @Override
    protected void startTask() {
        super.startTask();
        mTask = new LoadGroupPoolTask(mActivity, this, mGroup);
        mTask.execute(mOAuth);
    }
}

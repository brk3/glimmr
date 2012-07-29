package com.bourke.glimmr.fragments.group;

import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadGroupPoolTask;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.people.User;

public class GroupPoolGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/GroupPoolGridFragment";

    private Group mGroup;

    public static GroupPoolGridFragment newInstance(Group group, User user) {
        GroupPoolGridFragment newFragment = new GroupPoolGridFragment();
        newFragment.mGroup = group;
        newFragment.mUser = user;
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadGroupPoolTask(mActivity, this, mGroup).execute(mOAuth);
    }
}

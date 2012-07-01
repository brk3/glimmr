package com.bourke.glimmr.fragments.group;

import com.bourke.glimmr.event.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadGroupPoolTask;

import com.gmail.yuyang226.flickr.groups.Group;

public class GroupPoolGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/GroupPoolGridFragment";

    private Group mGroup;

    public static GroupPoolGridFragment newInstance(Group group) {
        GroupPoolGridFragment newFragment = new GroupPoolGridFragment();
        newFragment.mGroup = group;
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadGroupPoolTask(mActivity, this, mGroup).execute(mOAuth);
    }
}

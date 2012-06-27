package com.bourke.glimmr;

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
        new LoadGroupPoolTask(this, mGroup).execute(mOAuth);
    }
}

package com.bourke.glimmr.fragments.group;

import com.bourke.glimmr.fragments.base.BaseFragment;

public class GroupAboutFragment extends BaseFragment {

    private static final String TAG = "Glimmr/GroupAboutFragment";

    public static GroupAboutFragment newInstance() {
        return new GroupAboutFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        // TODO
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

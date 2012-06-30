package com.bourke.glimmr;

import com.actionbarsherlock.app.SherlockFragment;

public class GroupAboutFragment extends BaseFragment {

    private static final String TAG = "Glimmr/GroupAboutFragment";

    public static GroupAboutFragment newInstance() {
        return new GroupAboutFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        // TODO new LoadPhotostreamTask(this, mOAuth.getUser()).execute(mOAuth);
    }
}

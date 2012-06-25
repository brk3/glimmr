package com.bourke.glimmr;

public class GroupPoolGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/GroupPoolGridFragment";

    public static GroupPoolGridFragment newInstance() {
        return new GroupPoolGridFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        // TODO: new LoadPhotostreamTask(this, mOAuth.getUser()).execute(mOAuth);
    }
}

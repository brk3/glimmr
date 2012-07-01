package com.bourke.glimmr.fragments.home;

import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPhotostreamTask;

public class PhotoStreamGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/PhotoStreamGridFragment";

    public static PhotoStreamGridFragment newInstance() {
        return new PhotoStreamGridFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadPhotostreamTask(mActivity, this, mOAuth.getUser()).execute(
                mOAuth);
    }
}

package com.bourke.glimmr.fragments.profile;

import com.googlecode.flickrjandroid.people.User;
import com.bourke.glimmr.fragments.base.ProfilePhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPhotostreamTask;
import com.bourke.glimmr.tasks.LoadUserTask;

public class ProfilePhotoStreamGridFragment extends ProfilePhotoGridFragment {

    private static final String TAG = "Glimmr/ProfilePhotoStreamGridFragment";

    private int mPage = 1;

    public static ProfilePhotoStreamGridFragment newInstance(User user) {
        ProfilePhotoStreamGridFragment newFragment =
            new ProfilePhotoStreamGridFragment();
        newFragment.mUser = user;
        return newFragment;
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

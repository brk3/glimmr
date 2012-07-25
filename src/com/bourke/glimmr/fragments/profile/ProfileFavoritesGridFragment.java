package com.bourke.glimmr.fragments.profile;

import com.bourke.glimmr.fragments.base.ProfilePhotoGridFragment;
import com.bourke.glimmr.tasks.LoadFavoritesTask;
import com.bourke.glimmr.tasks.LoadUserTask;

import com.googlecode.flickrjandroid.people.User;

public class ProfileFavoritesGridFragment extends ProfilePhotoGridFragment {

    private static final String TAG = "Glimmr/ProfileFavoritesGridFragment";

    public static ProfileFavoritesGridFragment newInstance(User user) {
        ProfileFavoritesGridFragment newFragment =
            new ProfileFavoritesGridFragment();
        newFragment.mUser = user;
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadFavoritesTask(mActivity, this, mUser).execute(mOAuth);
        new LoadUserTask(mActivity, this, mUser).execute(mOAuth);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

package com.bourke.glimmr;

import com.gmail.yuyang226.flickr.people.User;

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
        new LoadFavoritesTask(this, mUser).execute(mOAuth);
        new LoadUserTask(this, mUser).execute(mOAuth);
    }
}

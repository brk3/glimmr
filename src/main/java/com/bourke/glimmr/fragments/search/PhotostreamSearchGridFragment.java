package com.bourke.glimmrpro.fragments.search;

import android.util.Log;
import com.bourke.glimmrpro.tasks.SearchPhotosTask;
import com.googlecode.flickrjandroid.people.User;

public class PhotostreamSearchGridFragment
        extends AbstractPhotoSearchGridFragment {

    private static final String TAG = "Glimmr/PhotostreamSearchGridFragment";

    private User mUserToView;

    public static PhotostreamSearchGridFragment newInstance(String searchTerm,
            int sortType, User userToView) {
        PhotostreamSearchGridFragment fragment =
            new PhotostreamSearchGridFragment();
        fragment.mSearchQuery = searchTerm;
        fragment.mSortType = sortType;
        fragment.mUserToView = userToView;
        return fragment;
    }

    @Override
    protected boolean cacheInBackground() {
        Log.d(getLogTag(), "cacheInBackground");
        startTask(mPage++);
        return mMorePages;
    }

    private void startTask(int page) {
        super.startTask();
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new SearchPhotosTask(this, mSearchQuery, mSortType, page,
                mUserToView.getId());
        mTask.execute(mOAuth);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

package com.bourke.glimmr.fragments.search;

import com.bourke.glimmr.tasks.SearchPhotosTask;
import android.util.Log;

public class PhotostreamSearchGridFragment
        extends AbstractPhotoSearchGridFragment {

    private static final String TAG = "Glimmr/PhotostreamSearchGridFragment";

    public static PhotostreamSearchGridFragment newInstance(String searchTerm,
            int sortType) {
        PhotostreamSearchGridFragment fragment =
            new PhotostreamSearchGridFragment();
        fragment.mSearchQuery = searchTerm;
        fragment.mSortType = sortType;
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
                mActivity.getUser().getId());
        mTask.execute(mOAuth);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

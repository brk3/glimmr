package com.bourke.glimmr.fragments.search;

import android.util.Log;
import com.bourke.glimmr.tasks.SearchPhotosTask;

public class PublicPhotoSearchGridFragment
        extends AbstractPhotoSearchGridFragment {

    private static final String TAG = "Glimmr/PublicPhotoSearchGridFragment";

    public static PublicPhotoSearchGridFragment newInstance(String searchTerm,
            int sortType) {
        PublicPhotoSearchGridFragment fragment =
            new PublicPhotoSearchGridFragment();
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
        mActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new SearchPhotosTask(this, mSearchQuery, mSortType, page);
        mTask.execute(mOAuth);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

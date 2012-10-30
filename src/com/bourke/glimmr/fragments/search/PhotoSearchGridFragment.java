package com.bourke.glimmr.fragments.search;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.SearchPhotosTask;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class PhotoSearchGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotoSearchGridFragment";

    private SearchPhotosTask mTask;
    private String mSearchTerm;
    private View mNoResultsLayout;

    public static PhotoSearchGridFragment newInstance(String searchTerm) {
        PhotoSearchGridFragment fragment = new PhotoSearchGridFragment();
        fragment.mSearchTerm = searchTerm;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowDetailsOverlay = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mNoResultsLayout =
            (RelativeLayout) mLayout.findViewById(R.id.no_search_results_layout);
        return mLayout;
    }

    /**
     * Once the parent binds the adapter it will trigger cacheInBackground
     * for us as it will be empty when first bound.
     */
    @Override
    protected boolean cacheInBackground() {
        startTask(mPage++);
        return mMorePages;
    }

    private void startTask(int page) {
        super.startTask();
        mTask = new SearchPhotosTask(this, this, mSearchTerm, page);
        mTask.execute(mOAuth);
    }

    @Override
    protected void refresh() {
        super.refresh();
        mTask = new SearchPhotosTask(this, this, mSearchTerm, mPage);
        mTask.execute(mOAuth);
    }

    @Override
    public void onPhotosReady(PhotoList photos) {
        super.onPhotosReady(photos);
        if (photos != null && photos.isEmpty()) {
            mMorePages = false;

            /* If first page (2 as mPage will have already been incremented),
             * and results are empty, show no search results layout */
            if (mPage == 2) {
                mNoResultsLayout.setVisibility(View.VISIBLE);
                mGridView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public String getNewestPhotoId() {
        /* Not needed for this fragment */
        return null;
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

package com.bourke.glimmrpro.fragments.search;

import android.os.Bundle;

import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;
import com.bourke.glimmrpro.fragments.base.PhotoGridFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.tasks.SearchPhotosTask;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class PhotoSearchGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotoSearchGridFragment";

    private SearchPhotosTask mTask;
    private String mSearchTerm;

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
                mAq.id(R.id.no_search_results_layout).visible();
                mAq.id(R.id.gridview).gone();
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

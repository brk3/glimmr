package com.bourke.glimmrpro.fragments.search;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;
import com.bourke.glimmrpro.fragments.base.PhotoGridFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.tasks.SearchPhotosTask;

import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class PhotoSearchGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotoSearchGridFragment";

    private String mSearchQuery = "";
    private View mNoResultsLayout;

    protected SearchPhotosTask mTask;

    public static PhotoSearchGridFragment newInstance(String searchTerm) {
        PhotoSearchGridFragment fragment = new PhotoSearchGridFragment();
        fragment.mSearchQuery = searchTerm;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowDetailsOverlay = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = mActivity.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("mSearchQuery", mSearchQuery);
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSearchQuery.equals("")) {
            SharedPreferences prefs = mActivity.getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_PRIVATE);
            mSearchQuery = prefs.getString("mSearchQuery", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mNoResultsLayout = (RelativeLayout)
            mLayout.findViewById(R.id.no_search_results_layout);
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
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new SearchPhotosTask(this, mSearchQuery, page);
        mTask.execute(mOAuth);
    }

    @Override
    public void onPhotosReady(List<Photo> photos) {
        super.onPhotosReady(photos);
        if (photos != null && photos.isEmpty()) {
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

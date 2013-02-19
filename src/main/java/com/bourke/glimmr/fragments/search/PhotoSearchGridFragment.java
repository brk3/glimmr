package com.bourke.glimmr.fragments.search;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.fragments.search.PhotoSearchGridFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.SearchPhotosTask;

import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class PhotoSearchGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotoSearchGridFragment";

    public static final int SORT_TYPE_RELAVANCE = 0;
    public static final int SORT_TYPE_RECENT = 1;
    public static final int SORT_TYPE_INTERESTING = 2;

    private String mSearchQuery = "";
    private View mNoResultsLayout;
    private int mSortType;
    protected SearchPhotosTask mTask;

    public static PhotoSearchGridFragment newInstance(String searchTerm,
            int sortType) {
        PhotoSearchGridFragment fragment = new PhotoSearchGridFragment();
        fragment.mSearchQuery = searchTerm;
        fragment.mSortType = sortType;
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
        if ("".equals(mSearchQuery)) {
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.relevance:
                mSortType = SORT_TYPE_RELAVANCE;
                break;

            case R.id.most_recent:
                mSortType = SORT_TYPE_RECENT;
                break;

            case R.id.interestingness:
                mSortType = SORT_TYPE_INTERESTING;
                break;

            default:
                return false;
        }
        refresh();
        return true;
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
        mTask = new SearchPhotosTask(this, mSearchQuery, mSortType, page);
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

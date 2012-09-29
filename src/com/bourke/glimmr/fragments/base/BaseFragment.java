package com.bourke.glimmrpro.fragments.base;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.util.Log;

import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.activities.BaseActivity;
import com.bourke.glimmrpro.activities.PhotoViewerActivity;
import com.bourke.glimmrpro.activities.ProfileActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.R;

import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.PhotoList;

/**
 *
 */
public abstract class BaseFragment extends SherlockFragment {

    private static final String TAG = "Glimmr/BaseFragment";

    /**
     * It's useful to keep a reference to the parent activity in our fragments.
     */
    protected BaseActivity mActivity;

    /**
     * Should contain current user and valid access token for that user.
     */
    protected OAuth mOAuth;

    protected ActionBar mActionBar;
    protected AQuery mAq;
    protected ViewGroup mLayout;

    protected boolean mRefreshing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        mActivity = (BaseActivity) getSherlockActivity();
        mActionBar = mActivity.getSupportActionBar();

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Constants.DEBUG) Log.d(getLogTag(), "onResume");

        /* Update our reference to the activity as it may have changed */
        mActivity = (BaseActivity) getSherlockActivity();
        mActionBar = mActivity.getSupportActionBar();

        startTask();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.fragment_main_menu, menu);
		if (mRefreshing) {
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.action_bar_indeterminate_progress);
		}
        if (mActivity.getUser() == null) {
            menu.findItem(R.id.menu_login).setVisible(true);
        } else {
            menu.findItem(R.id.menu_login).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProgressIcon(boolean show) {
        mRefreshing = show;
        mActivity.invalidateOptionsMenu();
    }

    /**
     * Start the PhotoViewerActivity with a list of photos to view and an index
     * to start at in the list.
     */
    protected void startPhotoViewer(PhotoList photos, int pos) {
        if (photos == null) {
            if (Constants.DEBUG)
                Log.e(getLogTag(), "Cannot start PhotoViewer, photos is null");
            return;
        }
        if (Constants.DEBUG)
            Log.d(getLogTag(), "Starting photo viewer with " + photos.size()
                + " ids");
        Bundle bundle = new Bundle();
        //PhotoList subList = makeBalancedSublist(photos, pos);
        //bundle.putSerializable(Constants.KEY_PHOTOVIEWER_LIST, subList);
        bundle.putSerializable(Constants.KEY_PHOTOVIEWER_LIST, photos);
        bundle.putInt(Constants.KEY_PHOTOVIEWER_START_INDEX, pos);
        Intent photoViewer = new Intent(mActivity, PhotoViewerActivity.class);
        photoViewer.putExtras(bundle);
        mActivity.startActivity(photoViewer);
    }

    /**
     * Return a sublist that pivots around a index.  Ideally, there will be an
     * equal number of items either side of the pivot.
     * - If the min or max overruns the bounds of the list, the other side
     *   will be padded.
     * - If the isn't big enough to make up both sides, just return the list.
     */
    private PhotoList makeBalancedSublist(PhotoList photos, int pivot) {
        final int SIDE_SIZE = 10;
        if (photos.size() < SIDE_SIZE*2) {
                return photos;
        }
        int right = pivot + SIDE_SIZE;
        int left = pivot - SIDE_SIZE;
        if (right > photos.size()) {
            left -= right - photos.size();
            right = photos.size();
        }
        if (left < 0) {
            right += SIDE_SIZE-pivot;
            left = 0;
        }
        PhotoList ret = new PhotoList();
        ret.addAll(photos.subList(left, right));
        return ret;
    }

    protected void startProfileViewer(User user) {
        if (user == null) {
            if (Constants.DEBUG)
                Log.e(getLogTag(),
                        "Cannot start ProfileActivity, user is null");
            return;
        }
        if (Constants.DEBUG)
            Log.d(getLogTag(), "Starting ProfileActivity for "
                + user.getUsername());
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_PROFILEVIEWER_USER, user);
        Intent profileViewer = new Intent(mActivity, ProfileActivity.class);
        profileViewer.putExtras(bundle);
        mActivity.startActivity(profileViewer);
    }

    protected void startTask() {
        if (Constants.DEBUG)
            Log.d(getLogTag(), "startTask()");
        if (mOAuth == null || mOAuth.getUser() == null) {
            SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                    .PREFS_NAME, Context.MODE_PRIVATE);
            mOAuth = BaseActivity.loadAccessToken(prefs);
        }
    }

    protected void refresh() {
        Log.e(getLogTag(), "refresh");
    }

    protected String getLogTag() {
        return TAG;
    }
}

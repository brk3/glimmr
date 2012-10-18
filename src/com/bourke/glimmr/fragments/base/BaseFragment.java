package com.bourke.glimmrpro.fragments.base;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.text.Spannable;
import android.text.style.ForegroundColorSpan;

import android.util.Log;

import android.view.ViewGroup;

import android.widget.TextView;

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
        /* <ICS seems to crash on this call, seems benign */
        try {
            mActivity.invalidateOptionsMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the PhotoViewerActivity with a list of photos to view and an index
     * to start at in the list.
     */
    protected void startPhotoViewer(PhotoList photos, int pos) {
        if (photos == null) {
            Log.e(getLogTag(), "Cannot start PhotoViewer, photos is null");
            return;
        }
        Bundle bundle = new Bundle();
        if (photos.size() > Constants.FETCH_PER_PAGE) {
            int page = pos / Constants.FETCH_PER_PAGE;
            int pageStart = page * Constants.FETCH_PER_PAGE;
            int pageEnd = pageStart + Constants.FETCH_PER_PAGE;
            if (pageEnd > photos.size()) {
               pageEnd = photos.size();
            }
            int pagePos = pos % Constants.FETCH_PER_PAGE;
            PhotoList subList = new PhotoList();
            subList.addAll(photos.subList(pageStart, pageEnd));
            bundle.putSerializable(Constants.KEY_PHOTOVIEWER_LIST, subList);
            bundle.putInt(Constants.KEY_PHOTOVIEWER_START_INDEX, pagePos);
            if (Constants.DEBUG) {
                Log.d(getLogTag(),
                        String.format("Starting photo viewer with %d ids",
                        subList.size()));
            }
        } else {
            bundle.putSerializable(Constants.KEY_PHOTOVIEWER_LIST, photos);
            bundle.putInt(Constants.KEY_PHOTOVIEWER_START_INDEX, pos);
            if (Constants.DEBUG) {
                Log.d(getLogTag(),
                        String.format("Starting photo viewer with %d ids",
                        photos.size()));
            }
        }
        Intent photoViewer = new Intent(mActivity, PhotoViewerActivity.class);
        photoViewer.putExtras(bundle);
        mActivity.startActivity(photoViewer);
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

    protected void colorTextViewSpan(TextView view, String fulltext,
            String subtext, int color) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable) view.getText();
        int i = fulltext.indexOf(subtext);
        try {
            str.setSpan(new ForegroundColorSpan(color), i, i+subtext.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "fulltext: " + fulltext);
                Log.d(getLogTag(), "subtext: " + subtext);
            }
        }
    }

    protected String getLogTag() {
        return TAG;
    }
}

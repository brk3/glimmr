package com.bourke.glimmr.fragments.base;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.util.Log;

import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.activities.PhotoViewerActivity;
import com.bourke.glimmr.activities.ProfileActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.Constants;

import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.PhotoList;

/**
 *
 */
public abstract class BaseFragment extends SherlockFragment {

    private static final String TAG = "Glimmr/BaseFragment";

    /**
     * Avoid calling onResume when coming in through onCreate.
     */
    protected boolean mCameFromPause;

    /**
     * It's useful to keep a reference to the parent activity in our fragments.
     */
    protected Activity mActivity;

    /**
     * Most Glimmr fragments deal with a list of photos.
     */
    protected PhotoList mPhotos = new PhotoList();

    /**
     * Should contain current user and valid access token for that user.
     */
    protected OAuth mOAuth;

    protected AQuery mGridAq;
    protected ViewGroup mLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mCameFromPause = false;
        mActivity = getSherlockActivity();
        startTask();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mCameFromPause = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCameFromPause) {
            Log.d(TAG, "onResume");
            startTask();
        }
    }

    /**
     * Start the PhotoViewerActivity with a list of photos to view and an index
     * to start at in the list.
     */
    protected void startPhotoViewer(int pos) {
        if (mPhotos == null) {
            Log.e(TAG, "Cannot start PhotoViewer, mPhotos is null");
            return;
        }
        Log.d(TAG, "starting photo viewer with " + mPhotos.size() + " ids");
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_PHOTOVIEWER_LIST, mPhotos);
        bundle.putInt(Constants.KEY_PHOTOVIEWER_START_INDEX, pos);
        Intent photoViewer = new Intent(mActivity, PhotoViewerActivity.class);
        photoViewer.putExtras(bundle);
        mActivity.startActivity(photoViewer);
    }

    protected void startProfileViewer(User user) {
        if (user == null) {
            Log.e(TAG, "Cannot start ProfileActivity, user is null");
            return;
        }
        Log.d(TAG, "Starting ProfileActivity for " + user.getUsername());
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_PROFILEVIEWER_USER, user);
        Intent profileViewer = new Intent(mActivity, ProfileActivity.class);
        profileViewer.putExtras(bundle);
        mActivity.startActivity(profileViewer);
    }

    protected void startTask() {
        Log.d(TAG, "startTask()");
        if (mOAuth == null || mOAuth.getUser() == null) {
            SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                    .PREFS_NAME, Context.MODE_PRIVATE);
            mOAuth = BaseActivity.loadAccessToken(prefs);
        }
    }
}

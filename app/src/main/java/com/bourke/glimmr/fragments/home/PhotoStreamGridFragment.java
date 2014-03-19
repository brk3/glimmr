package com.bourke.glimmr.fragments.home;

import com.bourke.glimmr.BuildConfig;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPhotostreamTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

public class PhotoStreamGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/PhotoStreamGridFragment";

    private static final String KEY_NEWEST_PHOTOSTREAM_PHOTO_ID =
        "glimmr_newest_photostream_photo_id";
    private static final String KEY_USER =
        "com.bourke.glimmr.PhotoStreamGridFragment.KEY_USER";

    private User mUserToView;

    public static PhotoStreamGridFragment newInstance(User userToView) {
        PhotoStreamGridFragment f = new PhotoStreamGridFragment();
        f.mUserToView = userToView;
        return f;
    }

    public static PhotoStreamGridFragment newInstance(User userToView,
            boolean retainInstance, int gridChoiceMode) {
        PhotoStreamGridFragment newFragment = new PhotoStreamGridFragment();
        newFragment.mUserToView = userToView;
        newFragment.mRetainInstance = retainInstance;
        newFragment.mGridChoiceMode = gridChoiceMode;
        return newFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return mLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        new GsonHelper(mActivity).marshallObject(
                mUserToView, outState, KEY_USER);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && mUserToView == null) {
            String json = savedInstanceState.getString(KEY_USER);
            if (json != null) {
                mUserToView = new Gson().fromJson(json, User.class);
            } else {
                Log.e(TAG, "No stored user found in savedInstanceState");
            }
        }
    }

    @Override
    protected boolean shouldRetainInstance() {
        return mRetainInstance;
    }

    @Override
    protected int getGridChoiceMode() {
        return mGridChoiceMode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowDetailsOverlay = true;
    }

    /**
     * Once the parent binds the adapter it will trigger cacheInBackground
     * for us as it will be empty when first bound.  So we don't need to
     * override startTask().
     */
    @Override
    protected boolean cacheInBackground() {
        startTask(mPage++);
        return mMorePages;
    }

    private void startTask(int page) {
        super.startTask();
        mActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadPhotostreamTask(this, mUserToView, page)
                .execute(mOAuth);
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_NEWEST_PHOTOSTREAM_PHOTO_ID,
                null);
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NEWEST_PHOTOSTREAM_PHOTO_ID, photo.getId());
        editor.commit();
        if (BuildConfig.DEBUG)
            Log.d(getLogTag(), "Updated most recent photostream photo id to " +
                photo.getId());
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

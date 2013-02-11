package com.bourke.glimmrpro.fragments.home;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.fragments.base.PhotoGridFragment;
import com.bourke.glimmrpro.tasks.LoadPhotostreamTask;

import com.googlecode.flickrjandroid.photos.Photo;

public class PhotoStreamGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/PhotoStreamGridFragment";

    public static final String KEY_NEWEST_PHOTOSTREAM_PHOTO_ID =
        "glimmr_newest_photostream_photo_id";

    protected LoadPhotostreamTask mTask;

    public static PhotoStreamGridFragment newInstance() {
        PhotoStreamGridFragment newFragment = new PhotoStreamGridFragment();
        return newFragment;
    }

    public static PhotoStreamGridFragment newInstance(boolean retainInstance,
            int choiceMode) {
        PhotoStreamGridFragment newFragment = new PhotoStreamGridFragment();
        newFragment.mRetainInstance = retainInstance;
        newFragment.mChoiceMode = choiceMode;
        return newFragment;
    }

    @Override
    protected boolean shouldRetainInstance() {
        return mRetainInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Set to false to disable overlay */
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
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadPhotostreamTask(this, mActivity.getUser(), page);
        mTask.execute(mOAuth);
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        String newestId = prefs.getString(KEY_NEWEST_PHOTOSTREAM_PHOTO_ID,
                null);
        return newestId;
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NEWEST_PHOTOSTREAM_PHOTO_ID, photo.getId());
        editor.commit();
        if (Constants.DEBUG)
            Log.d(getLogTag(), "Updated most recent photostream photo id to " +
                photo.getId());
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

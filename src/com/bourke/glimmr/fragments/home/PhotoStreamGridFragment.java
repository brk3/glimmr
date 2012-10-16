package com.bourke.glimmr.fragments.home;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPhotostreamTask;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class PhotoStreamGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotoStreamGridFragment";

    private LoadPhotostreamTask mTask;

    public static PhotoStreamGridFragment newInstance() {
        PhotoStreamGridFragment newFragment = new PhotoStreamGridFragment();
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowDetailsOverlay = false;
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
        mTask = new LoadPhotostreamTask(this, this, mActivity.getUser(), page);
        mTask.execute(mOAuth);
    }

    @Override
    protected void refresh() {
        super.refresh();
        mTask = new LoadPhotostreamTask(this, this, mActivity.getUser(),
                mPage);
        mTask.execute(mOAuth);
    }

    @Override
    public void onPhotosReady(PhotoList photos) {
        super.onPhotosReady(photos);
        if (photos != null && photos.isEmpty()) {
            mMorePages = false;
        }
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        String newestId = prefs.getString(
                Constants.NEWEST_PHOTOSTREAM_PHOTO_ID, null);
        return newestId;
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.NEWEST_PHOTOSTREAM_PHOTO_ID, photo.getId());
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

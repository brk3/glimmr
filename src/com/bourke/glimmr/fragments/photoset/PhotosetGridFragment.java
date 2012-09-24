package com.bourke.glimmr.fragments.photoset;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPhotosetTask;

import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.people.User;

public class PhotosetGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotosetGridFragment";

    private Photoset mPhotoset;
    private LoadPhotosetTask mTask;

    public static PhotosetGridFragment newInstance(Photoset photoset) {
        PhotosetGridFragment newFragment = new PhotosetGridFragment();
        newFragment.mPhotoset = photoset;
        return newFragment;
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
        mTask = new LoadPhotosetTask(this, this, mPhotoset, page);
        mTask.execute(mOAuth);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPhotoset != null) {
            SharedPreferences sp = mActivity.getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Constants.PHOTOSET_FRAGMENT_SET_ID,
                    mPhotoset.getId());
            editor.commit();
        }
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    @Override
    public void onPhotosReady(PhotoList photos) {
        User owner = mPhotoset.getOwner();
        if (owner != null) {
            for (Photo p : photos) {
                p.setOwner(owner);
                p.setUrl(String.format("%s%s%s%s", "http://flickr.com/photos/",
                            owner.getId(), "/", p.getId()));
            }
        }
        if (photos != null && photos.isEmpty()) {
            mMorePages = false;
        }
        super.onPhotosReady(photos);
    }

    @Override
    protected void refresh() {
        super.refresh();
        mTask = new LoadPhotosetTask(this, this, mPhotoset, mPage);
        mTask.execute(mOAuth);
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        String newestId = prefs.getString(
                Constants.NEWEST_PHOTOSET_PHOTO_ID, null);
        return newestId;
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.NEWEST_PHOTOSET_PHOTO_ID, photo.getId());
        editor.commit();
        if (Constants.DEBUG)
            Log.d(getLogTag(), "Updated most recent photoset photo id to " +
                photo.getId());
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

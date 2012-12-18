package com.bourke.glimmr.fragments.photoset;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPhotosetTask;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photos.Photo;

import com.google.gson.Gson;

import java.util.List;

public class PhotosetGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/PhotosetGridFragment";

    public static final String KEY_NEWEST_PHOTOSET_PHOTO_ID =
        "glimmr_newest_photoset_photo_id";
    public static final String PHOTOSET_FILE =
        "glimmr_photosetfragment_photoset.json";

    private Photoset mPhotoset;
    private LoadPhotosetTask mTask;

    public static PhotosetGridFragment newInstance(Photoset photoset) {
        PhotosetGridFragment newFragment = new PhotosetGridFragment();
        newFragment.mPhotoset = photoset;
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
        if (mPhotoset == null) {
            loadPhotoset();
        }
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadPhotosetTask(this, mPhotoset, page);
        mTask.execute(mOAuth);
    }

    @Override
    public void onPause() {
        super.onPause();
        boolean photosetStoreResult =
            new GsonHelper(mActivity).marshallObject(mPhotoset, PHOTOSET_FILE);
        if (!photosetStoreResult) {
            Log.e(TAG, "onPause: Error marshalling photoset");
        }
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    @Override
    public void onPhotosReady(List<Photo> photos) {
        super.onPhotosReady(photos);
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
        if (mPhotoset != null) {
            User owner = mPhotoset.getOwner();
            if (owner != null) {
                for (Photo p : photos) {
                    p.setOwner(owner);
                    p.setUrl(String.format("%s%s%s%s",
                                "http://flickr.com/photos/",
                                owner.getId(), "/", p.getId()));
                }
            }
            if (photos != null && photos.isEmpty()) {
                mMorePages = false;
            }
        }
    }

    /**
     * Load the last viewed set from storage for when the fragment gets
     * destroyed.
     */
    public void loadPhotoset() {
        GsonHelper gsonHelper = new GsonHelper(mActivity);
        String json = gsonHelper.loadJson(PHOTOSET_FILE);
        if (json.length() == 0) {
            Log.e(TAG, String.format("Error reading %s", PHOTOSET_FILE));
            return;
        }
        mPhotoset = new Gson().fromJson(json.toString(), Photoset.class);
    }

    @Override
    protected void refresh() {
        super.refresh();
        mTask = new LoadPhotosetTask(this, mPhotoset, mPage);
        mTask.execute(mOAuth);
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        String newestId = prefs.getString(KEY_NEWEST_PHOTOSET_PHOTO_ID, null);
        return newestId;
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NEWEST_PHOTOSET_PHOTO_ID, photo.getId());
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

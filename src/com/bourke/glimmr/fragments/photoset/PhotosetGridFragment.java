package com.bourke.glimmrpro.fragments.photoset;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GsonHelper;
import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;
import com.bourke.glimmrpro.fragments.base.PhotoGridFragment;
import com.bourke.glimmrpro.tasks.LoadPhotosetTask;

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

    protected LoadPhotosetTask mTask;

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
        new GsonHelper(mActivity).marshallObject(mPhotoset, PHOTOSET_FILE);
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

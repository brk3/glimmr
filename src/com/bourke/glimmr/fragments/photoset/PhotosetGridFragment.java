package com.bourke.glimmrpro.fragments.photoset;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.fragments.base.PhotoGridFragment;
import com.bourke.glimmrpro.activities.BaseActivity;
import com.bourke.glimmrpro.tasks.LoadPhotosetTask;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photos.Photo;

public class PhotosetGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/PhotosetGridFragment";

    private Photoset mPhotoset;

    public static PhotosetGridFragment newInstance(Photoset photoset) {
        PhotosetGridFragment newFragment = new PhotosetGridFragment();
        newFragment.mPhotoset = photoset;
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        if (mPhotos != null && !mPhotos.isEmpty()) {
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "mPhotos occupied, not starting task");
            }
        } else {
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "mPhotos null or empty, starting task");
            }
            if (mPhotoset == null) {
                loadPhotoset();
            }
            new LoadPhotosetTask(this, this, mPhotoset).execute(mOAuth);
        }
    }

    /**
     * Load the last viewed photoset from storage for when the fragment gets
     * destroyed.
     */
    public void loadPhotoset() {
        SharedPreferences sp = mActivity.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String photosetId = sp.getString(
                Constants.PHOTOSET_FRAGMENT_SET_ID, null);
        if (photosetId != null) {
            mPhotoset = new Photoset();
            mPhotoset.setId(photosetId);
            if (Constants.DEBUG) Log.d(getLogTag(), "Restored mPhotoset");
        } else {
            Log.e(getLogTag(), "Could not restore mPhotoset");
        }
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
    }

    @Override
    protected void refresh() {
        super.refresh();
        new LoadPhotosetTask(this, this, mPhotoset).execute(mOAuth);
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

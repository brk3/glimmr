package com.bourke.glimmrpro.fragments.group;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.event.Events.IPhotoListReadyListener;
import com.bourke.glimmrpro.fragments.base.PhotoGridFragment;
import com.bourke.glimmrpro.tasks.LoadGroupPoolTask;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class GroupPoolGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/GroupPoolGridFragment";

    private Group mGroup;
    private LoadGroupPoolTask mTask;

    public static GroupPoolGridFragment newInstance(Group group) {
        GroupPoolGridFragment newFragment = new GroupPoolGridFragment();
        newFragment.mGroup = group;
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
        if (mGroup == null) {
            loadGroup();
        }
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadGroupPoolTask(this, mGroup, page);
        mTask.execute(mOAuth);
    }

    @Override
    public void onPhotosReady(List<Photo> photos) {
        super.onPhotosReady(photos);
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
        if (photos != null && photos.isEmpty()) {
            mMorePages = false;
        }
    }

    /**
     * Load the last viewed group from storage for when the fragment gets
     * destroyed.
     */
    public void loadGroup() {
        SharedPreferences sp = mActivity.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String groupId = sp.getString(
                Constants.GROUP_FRAGMENT_GROUP_ID, null);
        if (groupId != null) {
            mGroup = new Group();
            mGroup.setId(groupId);
            if (Constants.DEBUG) Log.d(getLogTag(), "Restored mGroup");
        } else {
            Log.e(getLogTag(), "Could not restore mGroup");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGroup != null) {
            SharedPreferences sp = mActivity.getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Constants.GROUP_FRAGMENT_GROUP_ID,
                    mGroup.getId());
            editor.commit();
        }
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        String newestId = prefs.getString(
                Constants.NEWEST_GROUPPOOL_PHOTO_ID, null);
        return newestId;
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.NEWEST_GROUPPOOL_PHOTO_ID, photo.getId());
        editor.commit();
        if (Constants.DEBUG)
            Log.d(getLogTag(), "Updated most recent grouppool photo id to " +
                photo.getId());
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

package com.bourke.glimmr.fragments.home;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadContactsPhotosTask;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

public class ContactsGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/ContactsGridFragment";

    public static final String KEY_NEWEST_CONTACT_PHOTO_ID =
        "glimmr_newest_contact_photo_id";

    private LoadContactsPhotosTask mTask;

    public static ContactsGridFragment newInstance() {
        ContactsGridFragment newFragment = new ContactsGridFragment();
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadContactsPhotosTask(this);
        mTask.execute(mOAuth);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTask != null) {
            mTask.cancel(true);
            if (Constants.DEBUG) Log.d(TAG, "onPause: cancelling task");
        }
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        String newestId = prefs.getString(KEY_NEWEST_CONTACT_PHOTO_ID, null);
        return newestId;
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NEWEST_CONTACT_PHOTO_ID, photo.getId());
        editor.commit();
        if (Constants.DEBUG)
            Log.d(getLogTag(), "Updated most recent contact photo id to " +
                photo.getId());
    }

    /**
     * Override as no pagination
     */
    @Override
    protected void refresh() {
        mPhotos.clear();
        getAdapter().notifyDataSetChanged();
        startTask();
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

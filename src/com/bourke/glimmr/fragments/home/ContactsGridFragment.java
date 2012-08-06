package com.bourke.glimmr.fragments.home;

import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadContactsPhotosTask;

import com.googlecode.flickrjandroid.people.User;
import android.util.Log;

public class ContactsGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/ContactsGridFragment";

    private LoadContactsPhotosTask mTask;

    public static ContactsGridFragment newInstance(User user) {
        ContactsGridFragment newFragment = new ContactsGridFragment();
        newFragment.mUser = user;
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        if (mPhotos != null && !mPhotos.isEmpty()) {
            Log.d(getLogTag(), "mPhotos occupied, not starting task");
        } else {
            Log.d(getLogTag(), "mPhotos null or empty, starting task");
            mTask = new LoadContactsPhotosTask(mActivity, this);
            mTask.execute(mOAuth);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTask != null) {
            mTask.cancel(true);
            Log.d(TAG, "onPause: cancelling task");
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

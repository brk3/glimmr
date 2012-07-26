package com.bourke.glimmr.fragments.home;

import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadContactsPhotosTask;

import com.googlecode.flickrjandroid.people.User;

public class ContactsGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/ContactsGridFragment";

    public static ContactsGridFragment newInstance(User user) {
        ContactsGridFragment newFragment = new ContactsGridFragment();
        newFragment.mUser = user;
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadContactsPhotosTask(mActivity, this).execute(mOAuth);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

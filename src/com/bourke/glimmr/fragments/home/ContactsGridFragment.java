package com.bourke.glimmr.fragments.home;

import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadContactsPhotosTask;

public class ContactsGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/ContactsGridFragment";

    public static ContactsGridFragment newInstance() {
        return new ContactsGridFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadContactsPhotosTask(mActivity, this).execute(mOAuth);
    }
}

package com.bourke.glimmr;

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

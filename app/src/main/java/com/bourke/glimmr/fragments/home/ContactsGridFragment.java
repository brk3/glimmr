package com.bourke.glimmr.fragments.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.bourke.glimmr.BuildConfig;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.model.ContactsStreamModel;
import com.bourke.glimmr.model.IDataModel;
import com.googlecode.flickrjandroid.photos.Photo;

public class ContactsGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/ContactsGridFragment";

    public static final String KEY_NEWEST_CONTACT_PHOTO_ID =
        "glimmr_newest_contact_photo_id";

    public static ContactsGridFragment newInstance() {
        return new ContactsGridFragment();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataModel = ContactsStreamModel.getInstance(mActivity, mOAuth);
    }

    /**
     * Once the parent binds the adapter it will trigger cacheInBackground
     * for us as it will be empty when first bound.
     */
    @Override
    protected boolean cacheInBackground() {
        super.startTask();
        mActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
        ContactsStreamModel.getInstance(mActivity, mOAuth).fetchNextPage(this);
        return mMorePages;
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_NEWEST_CONTACT_PHOTO_ID, null);
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NEWEST_CONTACT_PHOTO_ID, photo.getId());
        editor.commit();
        if (BuildConfig.DEBUG)
            Log.d(getLogTag(), "Updated most recent contact photo id to " +
                photo.getId());
    }

    @Override
    protected int getModelType() {
        return IDataModel.TYPE_CONTACTS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

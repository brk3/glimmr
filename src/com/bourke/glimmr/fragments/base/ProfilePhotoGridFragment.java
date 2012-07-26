package com.bourke.glimmr.fragments.base;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.event.Events.IUserReadyListener;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.PhotoList;

/**
 * Subclass of PhotoGridFragment to show a GridView of photos along with
 * a banner to differentiate what profile the photos belong to.
 */
public abstract class ProfilePhotoGridFragment extends PhotoGridFragment
        implements IPhotoListReadyListener, IUserReadyListener {

    private static final String TAG = "Glimmr/ProfilePhotoGridFragment";

    protected User mUser;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.profile_gridview_fragment, container, false);
        return mLayout;
    }

    /**
     * Once the task comes back with the list of photos, set up the GridView
     * adapter etc. to display them.
     */
    @Override
    public void onPhotosReady(PhotoList photos) {
        /* Call up the PhotoGridFragment to populate the main GridView, then
         * populate our profile specific elements. */
        super.onPhotosReady(photos);
        mAq = new AQuery(mActivity, mLayout);
        mAq.id(R.id.text_screenname).text(mUser.getUsername());
    }

    @Override
    public void onUserReady(User user) {
        Log.d(TAG, "onUserReady");

        /* Replace the bare bones user object we were passed with a more
         * complete one containing the buddy icon url. */
        mUser = user;
        mAq.id(R.id.image_profile).image(mUser.getBuddyIconUrl(),
                Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,  0, 0,
                null, AQuery.FADE_IN_NETWORK);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

package com.bourke.glimmr;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.PhotoList;

/**
 * Subclass of PhotoGridFragment to show a GridView of photos along with
 * a banner to differentiate what profile the photos belong to.
 */
public class ProfilePhotoGridFragment extends PhotoGridFragment
        implements IPhotoGridReadyListener, IUserReadyListener {

    private static final String TAG = "Glimmr/ProfilePhotoGridFragment";

    private User mUser;
    private AQuery mAq;

    public static ProfilePhotoGridFragment newInstance(OAuth oauth, int type,
            User user) {
        ProfilePhotoGridFragment newFragment = new ProfilePhotoGridFragment();
        newFragment.mOAuth = oauth;
        newFragment.mType = type;
        newFragment.mUser = user;
        return newFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.profile_gridview_fragment, container, false);
        switch (mType) {
            case TYPE_PHOTO_STREAM:
                new LoadPhotostreamTask(this, mUser).execute(mOAuth);
                break;
            case TYPE_FAVORITES_STREAM:
                new LoadFavoritesTask(this, mUser).execute(mOAuth);
                break;
            default:
                Log.e(TAG, "Unknown ProfilePhotoGridFragment type: " + mType);
        }
        new LoadUserTask(this, mUser).execute(mOAuth);
        return mLayout;
    }

    /**
     * Once the task comes back with the list of photos, set up the GridView
     * adapter etc. to display them.
     */
    @Override
    public void onPhotosReady(PhotoList photos, boolean cancelled) {
        /* Call up the PhotoGridFragment to populate the main GridView, then
         * populate our profile specific elements. */
        super.onPhotosReady(photos, cancelled);

		mAq = new AQuery(mActivity, mLayout);
        mAq.id(R.id.text_screenname).text(mUser.getUsername());
    }

    @Override
    public void onUserReady(User user, boolean cancelled) {
        /* Replace the bare bones user object we were passed with a more
         * complete one containing the buddy icon url. */
        mUser = user;

        Log.d(TAG, "onUserReady");
        boolean useMemCache = false;
        boolean useFileCache = false;
        mAq.id(R.id.image_profile).image(mUser.getBuddyIconUrl(),
                useMemCache, useFileCache,  0, 0, null,
                AQuery.FADE_IN_NETWORK);
    }
}

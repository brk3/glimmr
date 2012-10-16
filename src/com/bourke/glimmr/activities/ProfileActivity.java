package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.event.Events.IUserReadyListener;
import com.bourke.glimmr.fragments.home.FavoritesGridFragment;
import com.bourke.glimmr.fragments.home.PhotosetsFragment;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadUserTask;

import com.googlecode.flickrjandroid.people.User;

/**
 * Requires a User object to be passed in via an intent.
 */
public class ProfileActivity extends BottomOverlayActivity
        implements IUserReadyListener {

    private static final String TAG = "Glimmr/ProfileActivity";

    public static final int PHOTO_STREAM_PAGE = 0;
    public static final int FAVORITES_STREAM_PAGE = 1;
    public static final int SETS_PAGE = 2;
    public static final int CONTACTS_PAGE = 3;

    private LoadUserTask mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CONTENT = new String[] { getString(R.string.photos),
            getString(R.string.favorites),
            getString(R.string.sets) };//, getString(R.string.contacts) };
        super.onCreate(savedInstanceState);
    }

    /*
     * Default user info doesn't include the buddy icon url, so we need to
     * fetch extra info about the user
     */
    protected void startTask() {
        if (mUser == null) {
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "Cannot start LoadUserTask, mUser is null");
            }
            return;
        }
        mTask = new LoadUserTask(this, this, mUser.getId());
        mTask.execute(mOAuth);
    }

    @Override
    public void onUserReady(User user) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onUserReady");
        if (user == null) {
            Log.e(getLogTag(), "onUserReady: user is null");
            return;
        }
        updateUserOverlay(user);
    }

    private void updateUserOverlay(User user) {
        if (Constants.DEBUG) Log.d(getLogTag(), "updateUserOverlay");
        mAq.id(R.id.bottomOverlay).visible();
        mAq.id(R.id.overlayImage).image(user.getBuddyIconUrl(),
                Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,  0, 0,
                null, AQuery.FADE_IN_NETWORK);
        mAq.id(R.id.overlayPrimaryText).text(user.getUsername());
    }

    @Override
    protected void updateBottomOverlay() {
    }

    @Override
    protected void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mUser = (User) bundle.getSerializable(
                    Constants.KEY_PROFILEVIEWER_USER);
            if (mUser != null) {
                if (Constants.DEBUG) {
                    Log.d(TAG, "Got user to view: " + mUser.getUsername());
                }
                initViewPager();
                startTask();
            } else {
                if (Constants.DEBUG) Log.e(TAG, "User from intent is null");
            }
        } else {
            Log.e(TAG, "Bundle is null, ProfileActivity requires an " +
                "intent containing a User");
        }
    }

    @Override
    protected void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new GlimmrPagerAdapter(
                getSupportFragmentManager(), mViewPager, mActionBar, CONTENT) {
            @Override
            public SherlockFragment getItemImpl(int position) {
                switch (position) {
                    case PHOTO_STREAM_PAGE:
                        return PhotoStreamGridFragment.newInstance();

                    case FAVORITES_STREAM_PAGE:
                        return FavoritesGridFragment.newInstance();

                    case SETS_PAGE:
                        return PhotosetsFragment.newInstance();

                    case CONTACTS_PAGE:
                        // TODO
                        return PhotoStreamGridFragment.newInstance();
                }
                return null;
            }
        };
        super.initViewPager();
    }
}

package com.bourke.glimmr.activities;

import com.bourke.glimmr.BuildConfig;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.event.Events;
import com.bourke.glimmr.event.Events.IUserReadyListener;
import com.bourke.glimmr.fragments.home.FavoritesGridFragment;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.fragments.home.PhotosetsFragment;
import com.bourke.glimmr.tasks.LoadProfileIdTask;
import com.bourke.glimmr.tasks.LoadUserTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.people.User;
import com.squareup.picasso.Picasso;

public class ProfileViewerActivity extends BottomOverlayActivity
        implements IUserReadyListener {

    private static final String TAG = "Glimmr/ProfileViewerActivity";

    private static final String KEY_USER =
            "com.bourke.glimmr.ProfileViewerActivity.KEY_USER";

    public static final String ACTION_VIEW_USER_BY_ID =
            "com.bourke.glimmr.ACTION_VIEW_USER_BY_ID";
    public static final String ACTION_VIEW_USER_BY_URL =
            "com.bourke.glimmr.ACTION_VIEW_USER_BY_URL";

    public static final String KEY_PROFILE_ID =
            "com.bourke.glimmr.KEY_PROFILE_ID";
    public static final String KEY_PROFILE_URL =
            "com.bourke.glimmr.KEY_PROFILE_URL";

    private static final int PHOTO_STREAM_PAGE = 0;
    private static final int FAVORITES_STREAM_PAGE = 1;
    private static final int SETS_PAGE = 2;
    private static final int CONTACTS_PAGE = 3;

    private User mUser;
    private LoadUserTask mUserTask;
    private LoadProfileIdTask mProfileIdTask;

    @Override
    protected void handleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_VIEW_USER_BY_ID)) {
            String userId = intent.getStringExtra(KEY_PROFILE_ID);
            mUserTask = new LoadUserTask(this, this, userId);
            mUserTask.execute(mOAuth);
        } else if (intent.getAction().equals(ACTION_VIEW_USER_BY_URL)) {
            final String profileUrl = intent.getStringExtra(KEY_PROFILE_URL);
            mProfileIdTask = new LoadProfileIdTask(new Events.IProfileIdReadyListener() {
                @Override
                public void onProfileIdReady(String profileId, Exception e) {
                    if (FlickrHelper.getInstance().handleFlickrUnavailable(
                            ProfileViewerActivity.this, e)) {
                        return;
                    }
                    if (profileId != null) {
                        new LoadUserTask(ProfileViewerActivity.this,
                                ProfileViewerActivity.this, profileId).execute();
                    } else {
                        Log.e(TAG, "Couldn't fetch profileId");
                    }
                }
            }, profileUrl);
            mProfileIdTask.execute();
        } else {
            Log.e(TAG, "Unknown intent action: " + intent.getAction());
        }
    }

    @Override
    protected void onDestroy() {
        if (mUserTask != null) {
            mUserTask.cancel(true);
        }
        if (mProfileIdTask != null) {
            mProfileIdTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        new GsonHelper(this).marshallObject(mUser, bundle, KEY_USER);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        Gson gson = new Gson();
        if (mUser == null) {
            String json = bundle.getString(KEY_USER);
            if (json != null) {
                mUser = gson.fromJson(json, User.class);
                initViewPager();
                updateBottomOverlay();
            } else {
                Log.e(TAG, "No user found in savedInstanceState");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CONTENT = new String[] { getString(R.string.photos),
            getString(R.string.favorites),
            getString(R.string.sets) };//, getString(R.string.contacts) };
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new GlimmrPagerAdapter(getSupportFragmentManager(),
                mViewPager, mActionBar, CONTENT) {
            @Override
            public Fragment getItemImpl(int position) {
                switch (position) {
                    case PHOTO_STREAM_PAGE:
                        return PhotoStreamGridFragment.newInstance(mUser);

                    case FAVORITES_STREAM_PAGE:
                        return FavoritesGridFragment.newInstance(mUser);

                    case SETS_PAGE:
                        return PhotosetsFragment.newInstance(mUser);

                    case CONTACTS_PAGE:
                        // TODO
                        return PhotoStreamGridFragment.newInstance(mUser);
                }
                return null;
            }
        };
        super.initViewPager();
    }

    @Override
    protected void updateBottomOverlay() {
        mBottomOverlayView.setVisibility(View.VISIBLE);
        Picasso.with(this).load(mUser.getBuddyIconUrl()).into(mOverlayImage);
        mBottomOverlayPrimaryText.setText(mUser.getUsername());
    }

    @Override
    public void onUserReady(User user, Exception e) {
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onUserReady");
        if (FlickrHelper.getInstance().handleFlickrUnavailable(this, e)) {
            return;
        }
        if (user != null) {
            mUser = user;
            initViewPager();
            updateBottomOverlay();
        } else {
            Log.e(getLogTag(), "onUserReady: user is null");
        }
    }
}

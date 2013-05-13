package com.bourke.glimmrpro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import com.actionbarsherlock.app.SherlockFragment;
import com.androidquery.AQuery;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GlimmrPagerAdapter;
import com.bourke.glimmrpro.common.GsonHelper;
import com.bourke.glimmrpro.event.Events;
import com.bourke.glimmrpro.event.Events.IUserReadyListener;
import com.bourke.glimmrpro.fragments.home.FavoritesGridFragment;
import com.bourke.glimmrpro.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmrpro.fragments.home.PhotosetsFragment;
import com.bourke.glimmrpro.tasks.LoadProfileIdTask;
import com.bourke.glimmrpro.tasks.LoadUserTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.people.User;

public class ProfileActivity extends BottomOverlayActivity
        implements IUserReadyListener {

    private static final String TAG = "Glimmr/ProfileActivity";

    private static final String PROFILEVIEWER_USER_FILE =
            "glimmr_profilevieweractivity_user.json";

    public static final String ACTION_VIEW_USER_BY_ID =
            "com.bourke.glimmr.ACTION_VIEW_USER_BY_ID";
    public static final String ACTION_VIEW_USER_BY_URL =
            "com.bourke.glimmr.ACTION_VIEW_USER_BY_URL";

    public static final String KEY_PROFILE_ID =
            "com.bourke.glimmr.KEY_PROFILE_ID";
    public static final String KEY_PROFILE_URL =
            "com.bourke.glimmr.KEY_PROFILE_URL";

    public static final int PHOTO_STREAM_PAGE = 0;
    public static final int FAVORITES_STREAM_PAGE = 1;
    public static final int SETS_PAGE = 2;
    public static final int CONTACTS_PAGE = 3;

    private User mUser;

    @Override
    protected void handleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_VIEW_USER_BY_ID)) {
            String userId = intent.getStringExtra(KEY_PROFILE_ID);
            new LoadUserTask(this, this, userId).execute(mOAuth);
        } else if (intent.getAction().equals(ACTION_VIEW_USER_BY_URL)) {
            final String profileUrl = intent.getStringExtra(KEY_PROFILE_URL);
            new LoadProfileIdTask(new Events.IProfileIdReadyListener() {
                @Override
                public void onProfileIdReady(String profileId) {
                    if (profileId != null) {
                        new LoadUserTask(ProfileActivity.this,
                                ProfileActivity.this, profileId).execute();
                    } else {
                        Log.e(TAG, "Couldn't fetch profileId");
                    }
                }
            }, profileUrl).execute();
        } else {
            Log.e(TAG, "Unknown intent action: " + intent.getAction());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (!new GsonHelper(this)
                .marshallObject(mUser, PROFILEVIEWER_USER_FILE)) {
            Log.e(TAG, "Error marshalling user, cannot start viewer");
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        if (mUser == null) {
            String json = new GsonHelper(this)
                    .loadJson(PROFILEVIEWER_USER_FILE);
            if (json.length() == 0) {
                Log.e(TAG, String.format("Error reading %s",
                        PROFILEVIEWER_USER_FILE));
                return;
            }
            mUser = new Gson().fromJson(json, User.class);
            initViewPager();
            updateBottomOverlay();
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
            public SherlockFragment getItemImpl(int position) {
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
        mAq.id(R.id.overlayImage).image(mUser.getBuddyIconUrl(),
                Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,  0, 0,
                null, AQuery.FADE_IN_NETWORK);
        mBottomOverlayPrimaryText.setText(mUser.getUsername());
    }

    @Override
    public void onUserReady(User user) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onUserReady");
        if (user != null) {
            mUser = user;
            initViewPager();
            updateBottomOverlay();
        } else {
            Log.e(getLogTag(), "onUserReady: user is null");
        }
    }
}

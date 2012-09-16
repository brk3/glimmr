package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IUserReadyListener;
import com.bourke.glimmr.fragments.home.FavoritesGridFragment;
import com.bourke.glimmr.fragments.home.PhotosetsFragment;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadUserTask;

import com.googlecode.flickrjandroid.people.User;

import com.viewpagerindicator.TitlePageIndicator;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Requires a User object to be passed in via an intent.
 */
public class ProfileActivity extends BaseActivity
        implements IUserReadyListener {

    private static final String TAG = "Glimmr/ProfileActivity";

    public static final int PHOTO_STREAM_PAGE = 0;
    public static final int FAVORITES_STREAM_PAGE = 1;
    public static final int SETS_PAGE = 2;
    public static final int CONTACTS_PAGE = 3;

    protected LoadUserTask mTask;
    protected AQuery mAq;

    public static String[] CONTENT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CONTENT = new String[] { getString(R.string.photos),
            getString(R.string.favorites),
            getString(R.string.sets) };//, getString(R.string.contacts) };

        if (mOAuth == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            setContentView(R.layout.main);
            mAq = new AQuery(this);
            handleIntent(getIntent());
        }
    }

    @Override
    public User getUser() {
        return mUser;
    }

    protected void startTask() {
        /* Default user info doesn't include the buddy icon url, so we need to
         * fetch extra info about the user */
        if (mUser == null) {
            if (Constants.DEBUG)
                Log.d(getLogTag(), "Cannot start LoadUserTask, mUser is null");
            return;
        }
        mTask = new LoadUserTask(this, this, mUser.getId());
        mTask.execute(mOAuth);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTask != null) {
            mTask.cancel(true);
        }
        if (mUser != null) {
            SharedPreferences sp = getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Constants.KEY_PROFILE_USER_NAME,
                    mUser.getUsername());
            editor.putString(Constants.KEY_PROFILE_USER_ID, mUser.getId());
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mUser == null) {
            SharedPreferences prefs = getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_PRIVATE);
            String userName = prefs.getString(
                    Constants.KEY_PROFILE_USER_NAME, null);
            String userId = prefs.getString(
                    Constants.KEY_PROFILE_USER_ID, null);
            if (userName != null && userId != null) {
                mUser = new User();
                mUser.setUsername(userName);
                mUser.setId(userId);
                if (Constants.DEBUG) {
                    Log.d(getLogTag(), "Restored mUser to " + userName);
                }
            } else {
                Log.e(getLogTag(), "Could not restore mUser");
            }
        }
    }

    @Override
    public void onUserReady(User user) {
        if (Constants.DEBUG)
            Log.d(getLogTag(), "onUserReady");
        if (user == null) {
            if (Constants.DEBUG)
                Log.e(getLogTag(), "onUserReady: user is null");
            return;
        }
        updateUserOverlay(user);
    }

    public void updateUserOverlay(User user) {
        if (Constants.DEBUG)
            Log.d(getLogTag(), "updateUserOverlay");
        mAq.id(R.id.profile_banner).visible();
        mAq.id(R.id.image_profile).image(user.getBuddyIconUrl(),
                Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,  0, 0,
                null, AQuery.FADE_IN_NETWORK);
        mAq.id(R.id.text_screenname).text(user.getUsername());
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mUser = (User) bundle.getSerializable(
                    Constants.KEY_PROFILEVIEWER_USER);
            if (mUser != null) {
                if (Constants.DEBUG)
                    Log.d(TAG, "Got user to view: " + mUser.getUsername());
                ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
                ProfilePagerAdapter adapter = new ProfilePagerAdapter(
                        getSupportFragmentManager());
                viewPager.setAdapter(adapter);
                TitlePageIndicator indicator = (TitlePageIndicator)
                    findViewById(R.id.indicator);
                indicator.setOnPageChangeListener(this);
                indicator.setViewPager(viewPager);

                startTask();
            } else {
                if (Constants.DEBUG)
                    Log.e(TAG, "User from intent is null");
                // TODO: show error / recovery
            }
        } else {
            if (Constants.DEBUG)
                Log.e(TAG, "Bundle is null, ProfileActivity requires an " +
                    "intent containing a User");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     * Should only be bound once we have a valid userId
     */
    class ProfilePagerAdapter extends FragmentPagerAdapter {
        public ProfilePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
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

        @Override
        public int getCount() {
            return ProfileActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ProfileActivity.CONTENT[
                position % ProfileActivity.CONTENT.length].toUpperCase();
        }
    }
}

package com.bourke.glimmrpro.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GlimmrTabsAdapter;
import com.bourke.glimmrpro.event.Events.IUserReadyListener;
import com.bourke.glimmrpro.fragments.home.FavoritesGridFragment;
import com.bourke.glimmrpro.fragments.home.PhotosetsFragment;
import com.bourke.glimmrpro.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.tasks.LoadUserTask;

import com.googlecode.flickrjandroid.people.User;

import com.viewpagerindicator.TitlePageIndicator;

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
            startActivity(new Intent(this, ExploreActivity.class));
        } else {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            setContentView(R.layout.main_activity);
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
                if (Constants.DEBUG) {
                    Log.d(TAG, "Got user to view: " + mUser.getUsername());
                }
                initViewPager();
                startTask();
            } else {
                if (Constants.DEBUG) Log.e(TAG, "User from intent is null");
                // TODO: show error / recovery
            }
        } else {
            Log.e(TAG, "Bundle is null, ProfileActivity requires an " +
                "intent containing a User");
        }
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        TitlePageIndicator indicator =
            (TitlePageIndicator) findViewById(R.id.indicator);

        /* Bind the ViewPager to a TitlePageIndicator, or if on larger screens,
         * set up actionbar tabs. */
        if (indicator != null) {
            ProfilePagerAdapter adapter =
                new ProfilePagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(adapter);
            indicator.setOnPageChangeListener(this);
            indicator.setViewPager(viewPager);
        } else {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            GlimmrTabsAdapter tabsAdapter =
                new GlimmrTabsAdapter(this, viewPager);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[PHOTO_STREAM_PAGE]),
                    PhotoStreamGridFragment.class, null);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(
                        CONTENT[FAVORITES_STREAM_PAGE]),
                    FavoritesGridFragment.class, null);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[SETS_PAGE]),
                    PhotosetsFragment.class, null);
            viewPager.setAdapter(tabsAdapter);
            viewPager.setOnPageChangeListener(tabsAdapter);
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

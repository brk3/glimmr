package com.bourke.glimmr;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import com.androidquery.AQuery;

import com.gmail.yuyang226.flickr.people.User;

import com.viewpagerindicator.TabPageIndicator;

/**
 * This activity is similar to MainActivity, but contains some more detailed
 * info on a User profile.
 *
 * Requires a User object to be passed in via an intent.
 */
public class ProfileActivity extends SherlockFragmentActivity
        implements ViewPager.OnPageChangeListener {

    private static final String TAG = "Glimmr/ProfileActivity";

    public static final int PHOTO_STREAM_PAGE = 0;
    public static final int FAVORITES_STREAM_PAGE = 1;
    public static final int SETS_PAGE = 2;
    public static final int CONTACTS_PAGE = 3;

    private AQuery mAq;

    private int mStackLevel = 0;

    private User mUser;

    //TODO: add to R.strings
    public static final String[] CONTENT =
        new String[] { "Photos", "Favorites", "Sets", "Contacts" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        handleIntent(getIntent());

		mAq = new AQuery(this);

        /* Hide the home icon */
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Log.e(TAG, "Bundle is null, ProfileActivity requires an intent " +
                    "containing a User");
        } else {
            mUser = (User) bundle.getSerializable(
                    Constants.KEY_PROFILEVIEWER_USER);
            if (mUser != null) {
                Log.d(TAG, "Got user to view: " + mUser.getUsername());
                ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
                ProfilePagerAdapter adapter = new ProfilePagerAdapter(
                        getSupportFragmentManager());
                viewPager.setAdapter(adapter);
                TabPageIndicator indicator = (TabPageIndicator) findViewById(
                        R.id.indicator);
                indicator.setOnPageChangeListener(this);
                indicator.setViewPager(viewPager);
            } else {
                Log.e(TAG, "User from intent is null");
                // TODO: show error / recovery
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageScrolled(int pos, float posOffset, int posOffsetPx) {}

    @Override
    public void onPageSelected(int pos) {}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
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
                    return ProfilePhotoGridFragment.newInstance(
                            ProfilePhotoGridFragment.TYPE_PHOTO_STREAM,
                            mUser);

                case FAVORITES_STREAM_PAGE:
                    return ProfilePhotoGridFragment.newInstance(
                            ProfilePhotoGridFragment.TYPE_FAVORITES_STREAM,
                            mUser);

                case SETS_PAGE:
                    return ProfilePhotoGridFragment.newInstance(
                            ProfilePhotoGridFragment.TYPE_FAVORITES_STREAM,
                            mUser);

                case CONTACTS_PAGE:
                    return ProfilePhotoGridFragment.newInstance(
                            ProfilePhotoGridFragment.TYPE_FAVORITES_STREAM,
                            mUser);
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

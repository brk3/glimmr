package com.bourke.glimmr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;
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
public class ProfileActivity extends BaseActivity {

    private static final String TAG = "Glimmr/ProfileActivity";

    public static final int PHOTO_STREAM_PAGE = 0;
    public static final int FAVORITES_STREAM_PAGE = 1;
    public static final int SETS_PAGE = 2;
    public static final int CONTACTS_PAGE = 3;

    //TODO: add to R.strings
    public static final String[] CONTENT =
        new String[] { "Photos", "Favorites", "Sets", "Contacts" };

    /**
     * User who's profile we're displaying, as distinct from the authorized
     * user.
     */
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        mOAuth = loadAccessToken(prefs);
		mAq = new AQuery(this);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        handleIntent(getIntent());

        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
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
        } else {
            Log.e(TAG, "Bundle is null, ProfileActivity requires an intent " +
                    "containing a User");
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
                    if (mOAuth == null) Log.d(TAG, "sdfjklsdf");
                    return ProfilePhotoGridFragment.newInstance(mOAuth,
                            ProfilePhotoGridFragment.TYPE_PHOTO_STREAM,
                            mUser);

                case FAVORITES_STREAM_PAGE:
                    return ProfilePhotoGridFragment.newInstance(mOAuth,
                            ProfilePhotoGridFragment.TYPE_FAVORITES_STREAM,
                            mUser);

                case SETS_PAGE:
                    return ProfilePhotoGridFragment.newInstance(mOAuth,
                            ProfilePhotoGridFragment.TYPE_FAVORITES_STREAM,
                            mUser);

                case CONTACTS_PAGE:
                    return ProfilePhotoGridFragment.newInstance(mOAuth,
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

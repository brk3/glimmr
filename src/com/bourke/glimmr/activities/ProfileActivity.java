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
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.people.User;

import com.viewpagerindicator.TitlePageIndicator;

/**
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
        //new String[] { "Photos", "Favorites", "Sets", "Contacts" };
        new String[] { "Photos" };

    /**
     * User who's profile we're displaying, as distinct from the authorized
     * user.
     */
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mOAuth == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            setContentView(R.layout.main);
            mAq = new AQuery(this);

            handleIntent(getIntent());
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
                TitlePageIndicator indicator = (TitlePageIndicator)
                    findViewById(R.id.indicator);
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
                    return PhotoStreamGridFragment.newInstance(mUser, true);

                case FAVORITES_STREAM_PAGE:
                    // TODO
                    return PhotoStreamGridFragment.newInstance(mUser, true);

                case SETS_PAGE:
                    // TODO
                    return PhotoStreamGridFragment.newInstance(mUser, true);

                case CONTACTS_PAGE:
                    // TODO
                    return PhotoStreamGridFragment.newInstance(mUser, true);
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

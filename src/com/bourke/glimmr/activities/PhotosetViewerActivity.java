package com.bourke.glimmr.activities;

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

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrAbCustomSubTitle;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.fragments.photoset.PhotosetGridFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photoset;

import com.viewpagerindicator.TitlePageIndicator;

public class PhotosetViewerActivity extends BaseActivity {

    private static final String TAG = "Glimmr/PhotosetViewerActivity";

    public static final int PHOTOSET_PAGE = 0;

    public static String[] CONTENT;

    private GlimmrAbCustomSubTitle mActionbarSubTitle;

    /**
     * The Photoset this activity is concerned with
     */
    private Photoset mPhotoset = new Photoset();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CONTENT = new String[] { getString(R.string.photoset) };

        if (mOAuth == null) {
            startActivity(new Intent(this, ExploreActivity.class));
        } else {
            setContentView(R.layout.main_activity);

            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionbarSubTitle = new GlimmrAbCustomSubTitle(getBaseContext());
            mActionbarSubTitle.init(mActionBar);

            mAq = new AQuery(this);

            handleIntent(getIntent());
        }
    }

    @Override
    public User getUser() {
        return mUser;
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mPhotoset = (Photoset) bundle.getSerializable(Constants.
                    KEY_PHOTOSETVIEWER_PHOTOSET);
            mUser = (User) bundle.getSerializable(
                    Constants.KEY_PHOTOSETVIEWER_USER);
            if (mPhotoset != null && mUser != null) {
                if (Constants.DEBUG) {
                    Log.d(TAG, "Got photoset to view: " +
                            mPhotoset.getTitle());
                }
                mPhotoset.setOwner(mUser);
                initViewPager();
                mActionbarSubTitle.setActionBarSubtitle(mPhotoset.getTitle());
            } else {
                Log.e(TAG, "Photoset/User from intent is null");
                // TODO: show error / recovery
            }
        } else {
            if (Constants.DEBUG)
                Log.e(TAG, "Bundle is null, PhotosetViewerActivity requires " +
                    "an intent containing a Photoset and a User");
        }
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        GlimmrPagerAdapter adapter = new GlimmrPagerAdapter(
                getSupportFragmentManager(), viewPager, mActionBar, CONTENT) {
            @Override
            public SherlockFragment getItemImpl(int position) {
                switch (position) {
                    case PHOTOSET_PAGE:
                        return PhotosetGridFragment.newInstance(mPhotoset);
                }
                return null;
            }
        };
        viewPager.setAdapter(adapter);

        TitlePageIndicator indicator =
            (TitlePageIndicator) findViewById(R.id.indicator);
        if (indicator != null) {
            indicator.setViewPager(viewPager);
        } else {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            viewPager.setOnPageChangeListener(adapter);
            for (String title : CONTENT) {
                ActionBar.Tab newTab = mActionBar.newTab().setText(title);
                newTab.setTabListener(adapter);
                mActionBar.addTab(newTab);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mUser != null) {
            SharedPreferences sp = getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Constants.KEY_USER_NAME, mUser.getUsername());
            editor.putString(Constants.KEY_USER_ID, mUser.getId());
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String userName = prefs.getString(Constants.KEY_USER_NAME, null);
        String userId = prefs.getString(Constants.KEY_USER_ID, null);
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

    class GroupPagerAdapter extends FragmentPagerAdapter {
        public GroupPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case PHOTOSET_PAGE:
                    return PhotosetGridFragment.newInstance(mPhotoset);
            }
            return null;
        }

        @Override
        public int getCount() {
            return PhotosetViewerActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return PhotosetViewerActivity.CONTENT[position %
                PhotosetViewerActivity.CONTENT.length].toUpperCase();
        }
    }
}

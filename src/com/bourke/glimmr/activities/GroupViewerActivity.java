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
import com.bourke.glimmr.fragments.group.GroupAboutFragment;
import com.bourke.glimmr.fragments.group.GroupPoolGridFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.people.User;

import com.viewpagerindicator.TitlePageIndicator;

public class GroupViewerActivity extends BaseActivity {

    private static final String TAG = "Glimmr/GroupViewerActivity";

    public static final int GROUP_POOL_PAGE = 0;
    public static final int GROUP_ABOUT_PAGE = 1;

    public static String[] CONTENT;

    /**
     * The Group this activity is concerned with
     */
    private Group mGroup = new Group();

    /**
     * User who's profile we're displaying, as distinct from the authorized
     * user.
     */
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CONTENT = new String[] { getString(R.string.pool),
            getString(R.string.about) };

        if (mOAuth == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            setContentView(R.layout.main);

            mActionBar.setDisplayHomeAsUpEnabled(true);
            mAq = new AQuery(this);

            handleIntent(getIntent());
        }
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mGroup = (Group) bundle.getSerializable(Constants.
                    KEY_GROUPVIEWER_GROUP);
            mUser = (User) bundle.getSerializable(
                    Constants.KEY_GROUPVIEWER_USER);
            if (mGroup != null && mUser != null) {
                Log.d(TAG, "Got group to view: " + mGroup.getName());
                ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
                GroupPagerAdapter adapter = new GroupPagerAdapter(
                        getSupportFragmentManager());
                viewPager.setAdapter(adapter);
                TitlePageIndicator indicator = (TitlePageIndicator)
                    findViewById(R.id.indicator);
                indicator.setOnPageChangeListener(this);
                indicator.setViewPager(viewPager);

                //setActionBarSubtitle(mGroup.getName());
            } else {
                Log.e(TAG, "Group/User from intent is null");
                // TODO: show error / recovery
            }
        } else {
            Log.e(TAG, "Bundle is null, GroupViewerActivity requires an " +
                    "intent containing a Group and a User");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    class GroupPagerAdapter extends FragmentPagerAdapter {
        public GroupPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case GROUP_POOL_PAGE:
                    return GroupPoolGridFragment.newInstance(mGroup, mUser);
                case GROUP_ABOUT_PAGE:
                    return GroupAboutFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return GroupViewerActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return GroupViewerActivity.CONTENT[position %
                GroupViewerActivity.CONTENT.length].toUpperCase();
        }
    }
}

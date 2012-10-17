package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.fragments.group.GroupAboutFragment;
import com.bourke.glimmr.fragments.group.GroupPoolGridFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.people.User;

public class GroupViewerActivity extends BottomOverlayActivity {

    private static final String TAG = "Glimmr/GroupViewerActivity";

    public static final int GROUP_POOL_PAGE = 0;
    public static final int GROUP_ABOUT_PAGE = 1;

    private Group mGroup = new Group();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CONTENT = new String[] { getString(R.string.pool) };
            //,getString(R.string.about) };
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mGroup = (Group) bundle.getSerializable(Constants.
                    KEY_GROUPVIEWER_GROUP);
            mUser = (User) bundle.getSerializable(
                    Constants.KEY_GROUPVIEWER_USER);
            if (mGroup != null && mUser != null) {
                if (Constants.DEBUG) {
                    Log.d(TAG, "Got group to view: " + mGroup.getName());
                }
                initViewPager();
                updateBottomOverlay();
            } else {
                Log.e(TAG, "Group/User from intent is null");
            }
        } else {
            Log.e(TAG, "Bundle is null, GroupViewerActivity requires an " +
                "intent containing a Group and a User");
        }
    }

    @Override
    protected void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new GlimmrPagerAdapter(getSupportFragmentManager(),
                mViewPager, mActionBar, CONTENT) {
            @Override
            public SherlockFragment getItemImpl(int position) {
                switch (position) {
                    case GROUP_POOL_PAGE:
                        return GroupPoolGridFragment.newInstance(mGroup);
                    case GROUP_ABOUT_PAGE:
                        return GroupAboutFragment.newInstance();
                }
                return null;
            }
        };
        super.initViewPager();
    }

    @Override
    protected void updateBottomOverlay() {
        mAq.id(R.id.bottomOverlay).visible();
        mAq.id(R.id.overlayPrimaryText).text(mGroup.getName());
        mAq.id(R.id.overlayImage).image(
                mGroup.getBuddyIconUrl(), Constants.USE_MEMORY_CACHE,
                Constants.USE_FILE_CACHE, 0, 0, null, AQuery.FADE_IN_NETWORK);
    }
}

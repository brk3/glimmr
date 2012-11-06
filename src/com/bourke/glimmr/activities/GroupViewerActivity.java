package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.fragments.group.GroupAboutFragment;
import com.bourke.glimmr.fragments.group.GroupPoolGridFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.people.User;

import com.google.gson.Gson;

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

    public static void startGroupViewer(BaseActivity activity, Group group) {
        if (group == null) {
            Log.e(TAG, "Cannot start GroupViewer, group is null");
            return;
        }
        if (Constants.DEBUG) {
            Log.d(TAG, "Starting GroupViewerActivity for " + group.getName());
        }

        GsonHelper gson = new GsonHelper(activity);

        boolean groupStoreResult =
            gson.marshallObject(group, Constants.GROUPVIEWER_GROUP_FILE);
        if (!groupStoreResult) {
            Log.e(TAG, "Error marshalling group, cannot start viewer");
            return;
        }

        boolean userStoreResult = gson.marshallObject(activity.getUser(),
                Constants.GROUPVIEWER_USER_FILE);
        if (!userStoreResult) {
            Log.e(TAG, "Error marshalling user, cannot start viewer");
            return;
        }

        Intent groupViewer = new Intent(activity, GroupViewerActivity.class);
        activity.startActivity(groupViewer);
    }

    @Override
    protected void handleIntent(Intent intent) {
        GsonHelper gsonHelper = new GsonHelper(this);
        Gson gson = new Gson();

        String json = gsonHelper.loadJson(Constants.GROUPVIEWER_GROUP_FILE);
        if (json.length() == 0) {
            Log.e(TAG, String.format("Error reading %s",
                        Constants.GROUPVIEWER_GROUP_FILE));
            return;
        }
        mGroup = gson.fromJson(json.toString(), Group.class);

        json = gsonHelper.loadJson(Constants.GROUPVIEWER_USER_FILE);
        if (json.length() == 0) {
            Log.e(TAG, String.format("Error reading %s",
                        Constants.GROUPVIEWER_USER_FILE));
            return;
        }
        mUser = gson.fromJson(json.toString(), User.class);

        if (Constants.DEBUG) {
            Log.d(TAG, "Got group to view: " + mGroup.getName());
        }
        initViewPager();
        updateBottomOverlay();
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
        mBottomOverlayView.setVisibility(View.VISIBLE);
        mBottomOverlayPrimaryText.setText(mGroup.getName());
        mAq.id(R.id.overlayImage).image(
                mGroup.getBuddyIconUrl(), Constants.USE_MEMORY_CACHE,
                Constants.USE_FILE_CACHE, 0, 0, null, AQuery.FADE_IN_NETWORK);
    }
}

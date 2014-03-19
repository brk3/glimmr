package com.bourke.glimmr.activities;

import com.bourke.glimmr.BuildConfig;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.event.Events;
import com.bourke.glimmr.fragments.group.GroupAboutFragment;
import com.bourke.glimmr.fragments.group.GroupPoolGridFragment;
import com.bourke.glimmr.tasks.LoadGroupIdTask;
import com.bourke.glimmr.tasks.LoadGroupInfoTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.groups.Group;
import com.squareup.picasso.Picasso;

public class GroupViewerActivity extends BottomOverlayActivity
        implements Events.IGroupInfoReadyListener {

    private static final String TAG = "Glimmr/GroupViewerActivity";

    private static final String KEY_GROUP =
            "com.bourke.glimmr.GroupViewerActivity.KEY_GROUP";

    /* Intent actions */
    public static final String ACTION_VIEW_GROUP_BY_ID =
            "com.bourke.glimmr.ACTION_VIEW_GROUP_BY_ID";
    public static final String ACTION_VIEW_GROUP_BY_NAME =
            "com.bourke.glimmr.ACTION_VIEW_GROUP_BY_NAME";

    public static final String KEY_GROUP_ID =
            "com.bourke.glimmr.GROUP_ID_NAME";
    public static final String KEY_GROUP_URL =
            "com.bourke.glimmr.GROUP_ID_URL";

    /* View pager page ids */
    private static final int GROUP_POOL_PAGE = 0;
    private static final int GROUP_ABOUT_PAGE = 1;

    private Group mGroup;

    @Override
    protected void handleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_VIEW_GROUP_BY_ID)) {
            String groupId = intent.getStringExtra(KEY_GROUP_ID);
            new LoadGroupInfoTask(groupId, this).execute(mOAuth);
        } else if (intent.getAction().equals(ACTION_VIEW_GROUP_BY_NAME)) {
            String groupUrl = intent.getStringExtra(KEY_GROUP_URL);
            new LoadGroupIdTask(new Events.IGroupIdReadyListener() {
                @Override
                public void onGroupIdReady(String groupId, Exception e) {
                    if (FlickrHelper.getInstance().handleFlickrUnavailable(
                            GroupViewerActivity.this, e)) {
                        return;
                    }
                    if (groupId != null) {
                        new LoadGroupInfoTask(groupId,
                                GroupViewerActivity.this).execute(mOAuth);
                    } else {
                        Log.e(TAG, "Couldn't fetch groupId");
                    }
                }
            }, groupUrl).execute();
        } else {
            Log.e(TAG, "Unknown intent action: " + intent.getAction());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        GsonHelper gson = new GsonHelper(this);
        gson.marshallObject(mGroup, bundle, KEY_GROUP);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mGroup == null) {
            String json = savedInstanceState.getString(KEY_GROUP);
            if (json != null) {
                mGroup = new Gson().fromJson(json, Group.class);
            } else {
                Log.e(TAG, "No stored group found in savedInstanceState");
            }
        }
        initViewPager();
        updateBottomOverlay();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CONTENT = new String[] { getString(R.string.pool) };
            //,getString(R.string.about) };
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new GlimmrPagerAdapter(getSupportFragmentManager(),
                mViewPager, mActionBar, CONTENT) {
            @Override
            public Fragment getItemImpl(int position) {
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
        Picasso.with(this).load(mGroup.getBuddyIconUrl()).into(mOverlayImage);
    }

    @Override
    public void onGroupInfoReady(Group group, Exception e) {
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onGroupInfoReady");
        if (FlickrHelper.getInstance().handleFlickrUnavailable(this, e)) {
            return;
        }
        if (group != null) {
            mGroup = group;
            initViewPager();
            updateBottomOverlay();
        } else {
            Log.e(getLogTag(), "onGroupInfoReady: group is null");
        }
    }
}

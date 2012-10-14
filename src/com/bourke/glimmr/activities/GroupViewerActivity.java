package com.bourke.glimmrpro.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GlimmrAbCustomSubTitle;
import com.bourke.glimmrpro.common.GlimmrPagerAdapter;
import com.bourke.glimmrpro.fragments.group.GroupAboutFragment;
import com.bourke.glimmrpro.fragments.group.GroupPoolGridFragment;
import com.bourke.glimmrpro.R;

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

    private GlimmrAbCustomSubTitle mActionbarSubTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CONTENT = new String[] { getString(R.string.pool) };
            //,getString(R.string.about) };

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

    private void handleIntent(Intent intent) {
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
                mActionbarSubTitle.setActionBarSubtitle(mGroup.getName());
            } else {
                Log.e(TAG, "Group/User from intent is null");
            }
        } else {
            Log.e(TAG, "Bundle is null, GroupViewerActivity requires an " +
                "intent containing a Group and a User");
        }
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        GlimmrPagerAdapter adapter = new GlimmrPagerAdapter(
                getSupportFragmentManager(), viewPager, mActionBar, CONTENT) {
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
    public User getUser() {
        return mUser;
    }
}

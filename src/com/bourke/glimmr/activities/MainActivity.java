package com.bourke.glimmr.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.explore.RecentPublicPhotosFragment;
import com.bourke.glimmr.fragments.home.ContactsGridFragment;
import com.bourke.glimmr.fragments.home.GroupListFragment;
import com.bourke.glimmr.fragments.home.PhotosetsFragment;
import com.bourke.glimmr.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.services.AppListener;
import com.bourke.glimmr.services.AppService;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.googlecode.flickrjandroid.people.User;

import com.sbstrm.appirater.Appirater;

import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private static final String TAG = "Glimmr/MainActivity";

    public static final int CONTACTS_PAGE = 0;
    public static final int PHOTOSTREAM_PAGE = 1;
    public static final int SETS_PAGE = 2;
    public static final int GROUPS_PAGE = 3;
    public static final int EXPLORE_PAGE = 4;

    public static String[] CONTENT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        CONTENT = new String[] { getString(R.string.contacts),
            getString(R.string.you), getString(R.string.sets),
            getString(R.string.groups), getString(R.string.explore)
        };

        if (mOAuth == null) {
            startActivity(new Intent(this, ExploreActivity.class));
        } else {
            setContentView(R.layout.main_activity);
            mAq = new AQuery(this);
            initViewPager();

            /* Enable alarms */
            SharedPreferences defaultSharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);
            boolean enableNotifications = defaultSharedPrefs.getBoolean(
                    Constants.KEY_ENABLE_NOTIFICATIONS, false);
            if (enableNotifications) {
                if (Constants.DEBUG) Log.d(TAG, "Scheduling alarms");
                WakefulIntentService.scheduleAlarms(
                        new AppListener(), this, false);
            } else {
                if (Constants.DEBUG) Log.d(TAG, "Cancelling alarms");
                AppService.cancelAlarms(this);
            }
            Appirater.appLaunched(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        mOAuth = loadAccessToken(prefs);
        if (mOAuth != null) {
            mUser = mOAuth.getUser();
        }
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(
                R.id.indicator);

        /* Bind the ViewPager to a TitlePageIndicator, or if on larger screens,
         * set up actionbar tabs. */
        if (indicator != null) {
            GlimmrPagerAdapter adapter = new GlimmrPagerAdapter(
                    getSupportFragmentManager());
            viewPager.setAdapter(adapter);
            indicator.setOnPageChangeListener(this);
            indicator.setViewPager(viewPager);
        } else {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            GlimmrTabsAdapter tabsAdapter =
                new GlimmrTabsAdapter(this, viewPager);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[CONTACTS_PAGE]),
                    ContactsGridFragment.class, null);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[PHOTOSTREAM_PAGE]),
                    PhotoStreamGridFragment.class, null);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[GROUPS_PAGE]),
                    GroupListFragment.class, null);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[SETS_PAGE]),
                    PhotosetsFragment.class, null);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[EXPLORE_PAGE]),
                    RecentPublicPhotosFragment.class, null);
            viewPager.setAdapter(tabsAdapter);
            viewPager.setOnPageChangeListener(tabsAdapter);
        }
    }

    @Override
    public User getUser() {
        return mUser;
    }

    class GlimmrPagerAdapter extends FragmentPagerAdapter {
        public GlimmrPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case PHOTOSTREAM_PAGE:
                    return PhotoStreamGridFragment.newInstance();

                case CONTACTS_PAGE:
                    return ContactsGridFragment.newInstance();

                case GROUPS_PAGE:
                    return GroupListFragment.newInstance();

                case SETS_PAGE:
                    return PhotosetsFragment.newInstance();

                case EXPLORE_PAGE:
                    return RecentPublicPhotosFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return MainActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return MainActivity.CONTENT[position % MainActivity.CONTENT.length]
                .toUpperCase();
        }
    }

    class GlimmrTabsAdapter extends FragmentPagerAdapter implements
            ActionBar.TabListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        public GlimmrTabsAdapter(SherlockFragmentActivity activity,
                ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mActionBar = activity.getSupportActionBar();
            mViewPager = pager;
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(),
                    info.args);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            for (int i = 0; i < mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
                }
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }

        final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }
    }
}

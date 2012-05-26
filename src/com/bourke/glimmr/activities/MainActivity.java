package com.bourke.glimmr;

import android.app.Activity;

import android.content.Context;

import android.graphics.Color;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.View;

import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends SherlockFragmentActivity
        implements ViewPager.OnPageChangeListener {

    private static final String TAG = "Glimmr/MainActivity";

    public static final int PHOTOSTREAM_PAGE = 0;
    public static final int FRIENDS_PAGE = 1;
    public static final int GROUPS_PAGE = 2;

    private PhotoStreamFragment mPhotoStreamFragment =
        new PhotoStreamFragment();

    private FriendsFragment mFriendsFragment = new FriendsFragment();

    private GroupsFragment mGroupsFragment = new GroupsFragment();

    private int mCurrentPage = PHOTOSTREAM_PAGE;

    private Context mContext;

    private int mStackLevel = 0;

    //TODO: add to R.strings
    public static final String[] CONTENT =
        new String[] { "Photostream", "Friends", "Groups" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");
        mContext = getApplicationContext();
        setContentView(R.layout.main);
        initViewPager();

        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageScrolled(int pos, float posOffset, int posOffsetPx) {}

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected");
        mCurrentPage = position;
        switch (position) {
            case PHOTOSTREAM_PAGE:
                //mPhotoStreamFragment.refresh();
                break;
            case FRIENDS_PAGE:
                //mFriendsFragment.refresh();
                break;
            case GROUPS_PAGE:
                //mGroupsFragment.refresh();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        GlimmrPagerAdapter adapter = new GlimmrPagerAdapter(
                getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        TabPageIndicator indicator =
            (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setOnPageChangeListener(this);
        indicator.setViewPager(viewPager);
    }

    class GlimmrPagerAdapter extends FragmentPagerAdapter {

        public GlimmrPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case PHOTOSTREAM_PAGE:
                    return mPhotoStreamFragment;
                case FRIENDS_PAGE:
                    return mFriendsFragment;
                case GROUPS_PAGE:
                    return mGroupsFragment;
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
}

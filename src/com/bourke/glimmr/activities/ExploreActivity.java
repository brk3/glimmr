package com.bourke.glimmr.activities;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.explore.RecentPublicPhotosFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.people.User;

import com.viewpagerindicator.TitlePageIndicator;

/**
 * Hosts fragments that don't require log in.
 */
public class ExploreActivity extends BaseActivity {

    private static final String TAG = "Glimmr/ExploreActivity";

    public static final int INTERESTING_PAGE = 0;
    //public static final int TAGS_PAGE = 1;

    public static String[] CONTENT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        CONTENT = new String[] { "Last 7 Days" };

        setContentView(R.layout.explore_activity);
        mAq = new AQuery(this);
        initViewPager();

        //Appirater.appLaunched(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        ExplorePagerAdapter adapter = new ExplorePagerAdapter(
                getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(
                R.id.indicator);
        indicator.setOnPageChangeListener(this);
        indicator.setViewPager(viewPager);
    }

    @Override
    public User getUser() {
        return null;
    }

    class ExplorePagerAdapter extends FragmentPagerAdapter {
        public ExplorePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case INTERESTING_PAGE:
                    return RecentPublicPhotosFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return ExploreActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ExploreActivity.CONTENT[
                position % ExploreActivity.CONTENT.length].toUpperCase();
        }
    }
}

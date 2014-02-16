package com.bourke.glimmr.common;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.Locale;

/**
 * Generic FragmentPagerAdapter to be used with Glimmr activities.
 * Can handle tabs or PageIndicators.
 */
public abstract class GlimmrPagerAdapter extends FragmentPagerAdapter
        implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "Glimmr/GlimmrPagerAdapter";

    private final String[] mContent;
    private final ViewPager mViewPager;
    private final ActionBar mActionBar;

    public abstract Fragment getItemImpl(int position);

    public GlimmrPagerAdapter(FragmentManager fm, final ViewPager viewPager,
            final ActionBar actionbar, final String[] content) {
        super(fm);
        mContent = content;
        mViewPager = viewPager;
        mActionBar = actionbar;
    }

    @Override
    public Fragment getItem(int position) {
        return getItemImpl(position);
    }

    @Override
    public int getCount() {
        return mContent.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContent[position % mContent.length].toUpperCase(
                Locale.getDefault());
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
        String tabText = tab.getText().toString();
        for (int i=0; i<mContent.length; i++) {
            if (tabText.equalsIgnoreCase(mContent[i])) {
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

}

package com.bourke.glimmrpro.common;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * Generic FragmentPagerAdapter to be used with Glimmr activities.
 * Can handle tabs or PageIndicators.
 */
public abstract class GlimmrPagerAdapter extends FragmentPagerAdapter
        implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "Glimmr/GlimmrPagerAdapter";

    private String[] mContent;
    private ViewPager mViewPager;
    private ActionBar mActionBar;

    public abstract SherlockFragment getItemImpl(int position);

    public GlimmrPagerAdapter(FragmentManager fm, final ViewPager viewPager,
            final ActionBar actionbar, final String[] content) {
        super(fm);
        mContent = content;
        mViewPager = viewPager;
        mActionBar = actionbar;
    }

    @Override
    public SherlockFragment getItem(int position) {
        return getItemImpl(position);
    }

    @Override
    public int getCount() {
        return mContent.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContent[position % mContent.length].toUpperCase();
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

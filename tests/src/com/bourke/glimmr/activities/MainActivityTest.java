package com.bourke.glimmr;

import android.app.Activity;

import android.support.v4.view.ViewPager;

import android.test.ActivityInstrumentationTestCase2;

import com.bourke.glimmr.activities.MainActivity;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.R;

public class MainActivityTest
        extends ActivityInstrumentationTestCase2<MainActivity> {

    private Activity mActivity;
    private ViewPager mViewPager;
    private GlimmrPagerAdapter mAdapter;

    public MainActivityTest() {
        super("com.bourke.glimmr", MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);
        mActivity = getActivity();
        mViewPager = (ViewPager) mActivity.findViewById(R.id.viewPager);
        mAdapter = (GlimmrPagerAdapter) mViewPager.getAdapter();
    }

    public void testPreConditions() {
        assertTrue(mActivity != null);
        assertTrue(mViewPager != null);
        assertEquals(mAdapter.getCount(), MainActivity.CONTENT.length);
    }
}

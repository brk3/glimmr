package com.bourke.glimmr;

import android.app.Instrumentation;

import android.content.res.Configuration;

import android.support.v4.view.ViewPager;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import com.actionbarsherlock.app.ActionBar;

import com.bourke.glimmr.activities.BaseActivity;
import com.bourke.glimmr.activities.MainActivity;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.R;

public class MainActivityTest
        extends ActivityInstrumentationTestCase2<MainActivity> {

    private ActionBar mActionBar;
    private BaseActivity mActivity;
    private Configuration mConfiguration;
    private GlimmrPagerAdapter mAdapter;
    private ViewPager mViewPager;

    private int TEST_STATE_DESTROY_POSITION = 1;
    private int TEST_STATE_PAUSE_POSITION = 1;

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
        mConfiguration = mActivity.getResources().getConfiguration();
        mActionBar = mActivity.getSupportActionBar();
    }

    /**
     * Test that MainActivity came up in a sane state.
     */
    public void testPreConditions() {
        assertTrue(mActivity != null);
        assertTrue(mViewPager != null);
        assertTrue(mAdapter != null);
        assertTrue(mActivity.getUser() != null);
        assertTrue(mActionBar != null);

        /* Check the number of pages */
        assertEquals(mAdapter.getCount(), MainActivity.CONTENT.length);

        /* Check each page is initialised */
        for (int i=0; i < MainActivity.CONTENT.length; i++) {
            assertTrue(mAdapter.getItem(i) != null);
        }

        /* Check for correct number of tabs */
        if (mConfiguration.smallestScreenWidthDp >= 600) {
            assertEquals(mActionBar.getTabCount(),
                    MainActivity.CONTENT.length);
        }

        /* Check the actionbar navigation mode */
        if (mConfiguration.smallestScreenWidthDp >= 600) {
            assertEquals(ActionBar.NAVIGATION_MODE_TABS,
                    mActionBar.getNavigationMode());
        } else {
            assertEquals(ActionBar.NAVIGATION_MODE_STANDARD,
                    mActionBar.getNavigationMode());
        }
    }

    /**
     * Test that MainActivity restores state after been destroyed.
     */
    @UiThreadTest
    public void testStateDestroy() {
        /* Set the current pager position */
        mViewPager.setCurrentItem(TEST_STATE_DESTROY_POSITION, false);

        /* Destroy and re-create the activity */
        mActivity.finish();
        mActivity = getActivity();

        /* Check the viewpager position has been restored */
        int currentPosition = mViewPager.getCurrentItem();
        assertEquals(TEST_STATE_DESTROY_POSITION, currentPosition);

        /* Check the correct tab is also selected */
        if (mConfiguration.smallestScreenWidthDp >= 600) {
            assertEquals(TEST_STATE_DESTROY_POSITION,
                    mActionBar.getSelectedNavigationIndex());
        }

        testPreConditions();
    }

    /**
     * Test that MainActivity restores state after been paused/resumed.
     */
    @UiThreadTest
    public void testStatePause() {
        Instrumentation mInstr = getInstrumentation();
        mViewPager.setCurrentItem(TEST_STATE_PAUSE_POSITION, false);

        /* Pause and resume the activity */
        mInstr.callActivityOnPause(mActivity);
        mInstr.callActivityOnResume(mActivity);

        /* Check the viewpager position has been restored */
        int currentPosition = mViewPager.getCurrentItem();
        assertEquals(TEST_STATE_DESTROY_POSITION, currentPosition);

        /* Check the correct tab is also selected */
        if (mConfiguration.smallestScreenWidthDp >= 600) {
            assertEquals(TEST_STATE_DESTROY_POSITION,
                    mActionBar.getSelectedNavigationIndex());
        }

        testPreConditions();
    }
}

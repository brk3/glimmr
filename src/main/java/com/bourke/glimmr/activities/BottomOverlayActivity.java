package com.bourke.glimmr.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.common.TextUtils;
import com.viewpagerindicator.TitlePageIndicator;

public abstract class BottomOverlayActivity extends BaseActivity {

    private static final String TAG = "Glimmr/BottomOverlayActivity";

    public static String[] CONTENT;

    protected ViewPager mViewPager;
    protected GlimmrPagerAdapter mAdapter;

    protected View mBottomOverlayView;
    protected TextView mBottomOverlayPrimaryText;
    protected ImageView mOverlayImage;

    protected abstract void handleIntent(Intent intent);
    protected abstract void updateBottomOverlay();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mBottomOverlayView =
                findViewById(R.id.bottomOverlay);
        mBottomOverlayPrimaryText =
            (TextView) findViewById(R.id.overlayPrimaryText);
        mOverlayImage = (ImageView) findViewById(R.id.overlayImage);
        initBottomOverlayFont();
        handleIntent(getIntent());
    }

    private void initBottomOverlayFont() {
        TextView overlayPrimaryText =
            (TextView) findViewById(R.id.overlayPrimaryText);
        mTextUtils.setFont(overlayPrimaryText, TextUtils.FONT_ROBOTOLIGHT);
    }

    protected void initViewPager() {
        if (mViewPager == null || mAdapter == null) {
            Log.e(TAG, "mViewPager and mAdapter must be initialised before " +
                    "calling BottomOverlayActivity.initViewPager");
            return;
        }
        mViewPager.setAdapter(mAdapter);
        TitlePageIndicator indicator =
            (TitlePageIndicator) findViewById(R.id.indicator);
        if (indicator != null) {
            indicator.setViewPager(mViewPager);
        } else {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            mViewPager.setOnPageChangeListener(mAdapter);
            for (String title : CONTENT) {
                ActionBar.Tab newTab = mActionBar.newTab().setText(title);
                newTab.setTabListener(mAdapter);
                mActionBar.addTab(newTab);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
}

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

import butterknife.ButterKnife;
import butterknife.InjectView;

public abstract class BottomOverlayActivity extends BaseActivity {

    private static final String TAG = "Glimmr/BottomOverlayActivity";

    public static String[] CONTENT;

    @InjectView(R.id.viewPager) public ViewPager mViewPager;
    protected GlimmrPagerAdapter mAdapter;

    @InjectView(R.id.bottomOverlay) View mBottomOverlayView;
    @InjectView(R.id.overlayPrimaryText) TextView mBottomOverlayPrimaryText;
    @InjectView(R.id.overlayImage) ImageView mOverlayImage;

    protected abstract void handleIntent(Intent intent);
    protected abstract void updateBottomOverlay();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        initBottomOverlayFont();
        handleIntent(getIntent());
    }

    private void initBottomOverlayFont() {
        TextView overlayPrimaryText = ButterKnife.findById(this, R.id.overlayPrimaryText);
        mTextUtils.setFont(overlayPrimaryText, TextUtils.FONT_ROBOTOLIGHT);
    }

    protected void initViewPager() {
        if (mViewPager == null || mAdapter == null) {
            Log.e(TAG, "mViewPager and mAdapter must be initialised before " +
                    "calling BottomOverlayActivity.initViewPager");
            return;
        }
        mViewPager.setAdapter(mAdapter);
        TitlePageIndicator indicator = ButterKnife.findById(this, R.id.indicator);
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

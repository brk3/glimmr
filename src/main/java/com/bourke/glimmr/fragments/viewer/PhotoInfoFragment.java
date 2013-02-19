package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;

import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.fragments.base.BaseDialogFragment;
import com.bourke.glimmr.fragments.viewer.ExifInfoFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.photos.Photo;

import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import android.util.Log;

public class PhotoInfoFragment extends BaseDialogFragment {

    public static final String TAG = "Glimmr/PhotoInfoFragment";

    public static final int OVERVIEW_PAGE = 0;
    public static final int MORE_PAGE = 1;

    private static String[] CONTENT;

    private GlimmrPagerAdapter mAdapter;
    private ViewPager mViewPager;

    private Photo mPhoto;

    public static PhotoInfoFragment newInstance(Photo photo) {
        PhotoInfoFragment newFragment = new PhotoInfoFragment();
        newFragment.mPhoto = photo;
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CONTENT = new String[] { "Overview", "More" };
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (LinearLayout) inflater.inflate(
                R.layout.photo_info_fragment, container, false);
        initViewPager();
        return mLayout;
    }

    private void initViewPager() {
        mViewPager = (ViewPager) mLayout.findViewById(R.id.viewPager);
        /* http://stackoverflow.com/a/13684139/663370 */
        mAdapter = new GlimmrPagerAdapter(
                getChildFragmentManager(), mViewPager,
                mActivity.getSupportActionBar(), CONTENT) {
            @Override
            public SherlockFragment getItemImpl(int position) {
                switch (position) {
                    case OVERVIEW_PAGE:
                        return PhotoOverviewFragment.newInstance(
                                PhotoInfoFragment.this.mPhoto);

                    case MORE_PAGE:
                        return ExifInfoFragment.newInstance(
                                PhotoInfoFragment.this.mPhoto);
                }
                return null;
            }
        };
        mViewPager.setAdapter(mAdapter);
        PageIndicator indicator =
            (TabPageIndicator) mLayout.findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
    }
}

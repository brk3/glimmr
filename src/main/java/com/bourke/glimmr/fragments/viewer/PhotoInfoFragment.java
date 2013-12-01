package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.bourke.glimmr.R;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.fragments.base.BaseDialogFragment;
import com.googlecode.flickrjandroid.photos.Photo;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;

public class PhotoInfoFragment extends BaseDialogFragment {

    public static final String TAG = "Glimmr/PhotoInfoFragment";

    private static final int OVERVIEW_PAGE = 0;
    private static final int MORE_PAGE = 1;

    private static String[] CONTENT;

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
        ViewPager viewPager = (ViewPager) mLayout.findViewById(R.id.viewPager);
        /* http://stackoverflow.com/a/13684139/663370 */
        GlimmrPagerAdapter adapter = new GlimmrPagerAdapter(
                getChildFragmentManager(), viewPager,
                mActivity.getActionBar(), CONTENT) {
            @Override
            public Fragment getItemImpl(int position) {
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
        viewPager.setAdapter(adapter);
        PageIndicator indicator =
            (TabPageIndicator) mLayout.findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
    }
}

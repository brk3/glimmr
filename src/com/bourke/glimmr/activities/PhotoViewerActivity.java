package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.bourke.glimmr.common.ViewPagerDisable;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.photos.Photo;

import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.List;
import com.googlecode.flickrjandroid.people.User;
import android.support.v4.view.ViewPager;

/**
 * Activity for viewing photos.
 *
 * Receives a list of photos via an intent and shows the first one specified by
 * a startIndex in a zoomable ImageView.
 */
public class PhotoViewerActivity extends BaseActivity
        implements ViewPager.OnPageChangeListener,
                   PhotoViewerFragment.IPhotoViewerCallbacks {

    private static final String TAG = "Glimmr/PhotoViewerActivity";

    private List<Photo> mPhotos = new ArrayList<Photo>();

    private PhotoViewerPagerAdapter mAdapter;
    private ViewPagerDisable mPager;
    private List<WeakReference<Fragment>> mFragList =
        new ArrayList<WeakReference<Fragment>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.photoviewer);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        handleIntent(getIntent());
    }

    /* Ignore the unchecked cast warning-
     * http://stackoverflow.com/a/262417/663370 */
    @SuppressWarnings("unchecked")
    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();

        mPhotos = (ArrayList<Photo>) bundle.getSerializable(Constants
                .KEY_PHOTOVIEWER_LIST);
        int startIndex = bundle.getInt(Constants.KEY_PHOTOVIEWER_START_INDEX);

        if (mPhotos != null) {
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "Got list of photo urls, size: "
                    + mPhotos.size());
            }
            mAdapter =
                new PhotoViewerPagerAdapter(getSupportFragmentManager());
            mPager = (ViewPagerDisable) findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);
            /* Don't show the PageIndicator if there's a lot of items */
            if (mPhotos.size() <= Constants.LINE_PAGE_INDICATOR_LIMIT) {
                PageIndicator indicator =
                    (LinePageIndicator) findViewById(R.id.indicator);
                indicator.setOnPageChangeListener(this);
                indicator.setViewPager(mPager);
                indicator.setCurrentItem(startIndex);
            } else {
                mPager.setCurrentItem(startIndex);
            }
        } else {
            if (Constants.DEBUG) {
                Log.e(getLogTag(), "Photos from intent are null");
            }
            // TODO: show error / recovery
        }
    }

    @Override
    public User getUser() {
        return mUser;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
    }

    @Override
    public void onAttachFragment (Fragment fragment) {
        mFragList.add(new WeakReference(fragment));
    }

    @Override
    public void onVisibilityChanged() {
        for (WeakReference<Fragment> ref : mFragList) {
            PhotoViewerFragment f = (PhotoViewerFragment) ref.get();
            if (f != null) {
                f.refreshOverlayVisibility();
            }
        }
    }

    @Override
    public void onZoomed(boolean isZoomed) {
        mPager.setPagingEnabled(!isZoomed);
    }

    class PhotoViewerPagerAdapter extends FragmentStatePagerAdapter {
        public PhotoViewerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PhotoViewerFragment.newInstance(
                    mPhotos.get(position), PhotoViewerActivity.this);
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }
    }
}

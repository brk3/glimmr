package com.bourke.glimmr;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import com.gmail.yuyang226.flickr.photos.Photo;

import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;
import java.util.List;

public class PhotoViewerActivity extends SherlockFragmentActivity {

    private static final String TAG = "Glimmr/PhotoViewerActivity";

    private PhotoViewerPagerAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.photoviewer);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        List<String> photoUrls = (ArrayList<String>) bundle.getSerializable(
                Constants.KEY_PHOTOVIEWER_LIST);
        if (photoUrls != null) {
            Log.d(TAG, "got list of photo urls, size: " + photoUrls.size());
            mAdapter = new PhotoViewerPagerAdapter(getSupportFragmentManager(),
                    photoUrls);
            mPager = (ViewPager)findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);
            mIndicator = (LinePageIndicator)findViewById(R.id.indicator);
            mIndicator.setViewPager(mPager);
        } else {
            Log.e(TAG, "photos from intent are null");
            // TODO: show error / recovery
        }
    }

    class PhotoViewerPagerAdapter extends FragmentPagerAdapter {
        protected List<String> mPhotoUrls;
        private int mCount;

        public PhotoViewerPagerAdapter(FragmentManager fm,
                List<String> photos) {
            super(fm);
            mPhotoUrls = photos;
            mCount = mPhotoUrls.size();
        }

        @Override
        public Fragment getItem(int position) {
            Photo photo = new Photo();
            photo.setUrl(mPhotoUrls.get(position));
            return PhotoFragment.newInstance(photo);
        }

        @Override
        public int getCount() {
            return mCount;
        }
    }
}

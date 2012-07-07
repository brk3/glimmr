package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.View;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmr.R;

import com.gmail.yuyang226.flickr.photos.Photo;

import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for viewing photos.
 *
 * Receives a list of photos via an intent and shows the first one specified by
 * a startIndex in a zoomable WebView.
 */
public class PhotoViewerActivity extends BaseActivity {

    private static final String TAG = "Glimmr/PhotoViewerActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoviewer);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    public void onExifButtonClick(View view) {
        Intent exifActivity = new Intent(this, ExifInfoActivity.class);
        startActivity(exifActivity);
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        List<String> photoUrls = (ArrayList<String>) bundle.getSerializable(
                Constants.KEY_PHOTOVIEWER_LIST);
        int startIndex = (Integer) bundle.getInt(Constants
                .KEY_PHOTOVIEWER_START_INDEX);

        if (photoUrls != null) {
            Log.d(TAG, "Got list of photo urls, size: " + photoUrls.size());
            PhotoViewerPagerAdapter adapter = new PhotoViewerPagerAdapter(
                    getSupportFragmentManager(), photoUrls);
            ViewPager pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(adapter);
            PageIndicator indicator = (LinePageIndicator) findViewById(
                    R.id.indicator);
            indicator.setViewPager(pager);
            indicator.setCurrentItem(startIndex);
        } else {
            Log.e(TAG, "Photos from intent are null");
            // TODO: show error / recovery
        }
    }

    class PhotoViewerPagerAdapter extends FragmentPagerAdapter {
        private List<String> mPhotoUrls;

        public PhotoViewerPagerAdapter(FragmentManager fm,
                List<String> photos) {
            super(fm);
            mPhotoUrls = photos;
        }

        @Override
        public Fragment getItem(int position) {
            Photo photo = new Photo();
            photo.setUrl(mPhotoUrls.get(position));
            return PhotoViewerFragment.newInstance(photo);
        }

        @Override
        public int getCount() {
            return mPhotoUrls.size();
        }
    }
}

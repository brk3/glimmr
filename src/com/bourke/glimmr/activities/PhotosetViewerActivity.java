package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;

import com.androidquery.AQuery;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.group.GroupPoolGridFragment;
import com.bourke.glimmr.R;

import com.viewpagerindicator.TitlePageIndicator;
import com.gmail.yuyang226.flickr.photosets.Photoset;
import com.bourke.glimmr.fragments.photoset.PhotosetGridFragment;

public class PhotosetViewerActivity extends BaseActivity {

    private static final String TAG = "Glimmr/PhotosetViewerActivity";

    public static final int PHOTOSET_PAGE = 0;

    // TODO: Set this to the title of the current set being viewed
    public static final String[] CONTENT = new String[] { "Set" };

    /**
     * The Photoset this activity is concerned with
     */
    private Photoset mPhotoset = new Photoset();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mOAuth == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            setContentView(R.layout.main);
            mAq = new AQuery(this);

            handleIntent(getIntent());

            if (savedInstanceState != null) {
                mStackLevel = savedInstanceState.getInt("level");
            }
        }
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mPhotoset = (Photoset) bundle.getSerializable(Constants.
                    KEY_PHOTOSETVIEWER_PHOTOSET);
            if (mPhotoset != null) {
                Log.d(TAG, "Got photoset to view: " + mPhotoset.getTitle());
                ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
                GroupPagerAdapter adapter = new GroupPagerAdapter(
                        getSupportFragmentManager());
                viewPager.setAdapter(adapter);
                TitlePageIndicator indicator = (TitlePageIndicator)
                    findViewById(R.id.indicator);
                indicator.setOnPageChangeListener(this);
                indicator.setViewPager(viewPager);
            } else {
                Log.e(TAG, "Photoset from intent is null");
                // TODO: show error / recovery
            }
        } else {
            Log.e(TAG, "Bundle is null, PhotosetViewerActivity requires an " +
                    "intent containing a Photoset");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    class GroupPagerAdapter extends FragmentPagerAdapter {
        public GroupPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case PHOTOSET_PAGE:
                    return PhotosetGridFragment.newInstance(mPhotoset);
            }
            return null;
        }

        @Override
        public int getCount() {
            return PhotosetViewerActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return PhotosetViewerActivity.CONTENT[position %
                PhotosetViewerActivity.CONTENT.length].toUpperCase();
        }
    }
}

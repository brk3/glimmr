package com.bourke.glimmrpro.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;
import com.androidquery.AQuery;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GlimmrPagerAdapter;
import com.bourke.glimmrpro.fragments.photoset.PhotosetGridFragment;
import com.bourke.glimmrpro.R;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photoset;

public class PhotosetViewerActivity extends BottomOverlayActivity {

    public static final String TAG = "Glimmr/PhotosetViewerActivity";

    public static final int PHOTOSET_PAGE = 0;

    private Photoset mPhotoset = new Photoset();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CONTENT = new String[] { getString(R.string.photoset) };
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mPhotoset = (Photoset) bundle.getSerializable(Constants.
                    KEY_PHOTOSETVIEWER_PHOTOSET);
            mUser = (User) bundle.getSerializable(
                    Constants.KEY_PHOTOSETVIEWER_USER);
            if (mPhotoset != null && mUser != null) {
                if (Constants.DEBUG) {
                    Log.d(TAG, "Got photoset to view: " +mPhotoset.getTitle());
                }
                mPhotoset.setOwner(mUser);
                initViewPager();
                updateBottomOverlay();
            } else {
                Log.e(TAG, "Photoset/User from intent is null");
            }
        } else {
            Log.e(TAG, "Bundle is null, PhotosetViewerActivity requires " +
                "an intent containing a Photoset and a User");
        }
    }

    @Override
    protected void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new GlimmrPagerAdapter(getSupportFragmentManager(),
                mViewPager, mActionBar, CONTENT) {
            @Override
            public SherlockFragment getItemImpl(int position) {
                switch (position) {
                    case PHOTOSET_PAGE:
                        return PhotosetGridFragment.newInstance(mPhotoset);
                }
                return null;
            }
        };
        super.initViewPager();
    }

    @Override
    protected void updateBottomOverlay() {
        mAq.id(R.id.bottomOverlay).visible();
        String overlayText = String.format("%s %s %s",
                mPhotoset.getTitle(), getString(R.string.by),
                mUser.getUsername());
        mAq.id(R.id.overlayPrimaryText).text(overlayText);
        mAq.id(R.id.overlayImage).image(
                mPhotoset.getPrimaryPhoto().getSmallSquareUrl(),
                Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,
                0, 0, null, AQuery.FADE_IN_NETWORK);
    }
}

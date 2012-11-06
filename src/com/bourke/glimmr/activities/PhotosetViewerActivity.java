package com.bourke.glimmrpro.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.activities.BaseActivity;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GlimmrPagerAdapter;
import com.bourke.glimmrpro.common.GsonHelper;
import com.bourke.glimmrpro.fragments.photoset.PhotosetGridFragment;
import com.bourke.glimmrpro.R;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photoset;

import com.google.gson.Gson;

public class PhotosetViewerActivity extends BottomOverlayActivity {

    public static final String TAG = "Glimmr/PhotosetViewerActivity";

    public static final int PHOTOSET_PAGE = 0;

    private Photoset mPhotoset = new Photoset();

    public static void startPhotosetViewer(BaseActivity activity,
            Photoset photoset) {
        if (photoset == null) {
            Log.e(TAG, "Cannot start SetViewerActivity, photoset is null");
            return;
        }
        if (Constants.DEBUG) {
            Log.d(TAG, "Starting SetViewerActivity for "+ photoset.getTitle());
        }

        GsonHelper gson = new GsonHelper(activity);

        boolean photosetStoreResult =
            gson.marshallObject(photoset, Constants.PHOTOSETVIEWER_SET_FILE);
        if (!photosetStoreResult) {
            Log.e(TAG, "Error marshalling photoset, cannot start viewer");
            return;
        }

        boolean userStoreResult = gson.marshallObject(activity.getUser(),
                    Constants.PHOTOSETVIEWER_USER_FILE);
        if (!userStoreResult) {
            Log.e(TAG, "Error marshalling user, cannot start viewer");
            return;
        }

        Intent photosetViewer = new Intent(activity, PhotosetViewerActivity
                .class);
        activity.startActivity(photosetViewer);
    }

    @Override
    protected void handleIntent(Intent intent) {
        GsonHelper gsonHelper = new GsonHelper(this);
        Gson gson = new Gson();

        String json = gsonHelper.loadJson(Constants.PHOTOSETVIEWER_SET_FILE);
        if (json.length() == 0) {
            Log.e(TAG, String.format("Error reading %s",
                        Constants.PHOTOSETVIEWER_SET_FILE));
            return;
        }
        mPhotoset = gson.fromJson(json.toString(), Photoset.class);

        json = gsonHelper.loadJson(Constants.PHOTOSETVIEWER_USER_FILE);
        if (json.length() == 0) {
            Log.e(TAG, String.format("Error reading %s",
                        Constants.PHOTOSETVIEWER_USER_FILE));
            return;
        }
        mUser = gson.fromJson(json.toString(), User.class);

        if (Constants.DEBUG) {
            Log.d(TAG, "Got photoset to view: " +mPhotoset.getTitle());
        }
        mPhotoset.setOwner(mUser);
        initViewPager();
        updateBottomOverlay();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CONTENT = new String[] { getString(R.string.photoset) };
        super.onCreate(savedInstanceState);
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
        mBottomOverlayView.setVisibility(View.VISIBLE);
        String overlayText = String.format("%s %s %s",
                mPhotoset.getTitle(), getString(R.string.by),
                mUser.getUsername());
        mBottomOverlayPrimaryText.setText(overlayText);
        mAq.id(R.id.overlayImage).image(
                mPhotoset.getPrimaryPhoto().getSmallSquareUrl(),
                Constants.USE_MEMORY_CACHE, Constants.USE_FILE_CACHE,
                0, 0, null, AQuery.FADE_IN_NETWORK);
    }
}

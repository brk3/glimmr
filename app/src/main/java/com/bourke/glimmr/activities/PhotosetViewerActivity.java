package com.bourke.glimmr.activities;

import com.bourke.glimmr.BuildConfig;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.event.Events;
import com.bourke.glimmr.fragments.photoset.PhotosetGridFragment;
import com.bourke.glimmr.tasks.LoadPhotosetTask;
import com.bourke.glimmr.tasks.LoadUserTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.squareup.picasso.Picasso;

public class PhotosetViewerActivity extends BottomOverlayActivity
        implements Events.IPhotosetReadyListener, Events.IUserReadyListener {

    private static final String TAG = "Glimmr/PhotosetViewerActivity";

    private static final String KEY_PHOTOSET =
            "com.bourke.glimmr.PhotosetViewerActivity.KEY_PHOTOSET";
    private static final String KEY_USER =
            "com.bourke.glimmr.PhotosetViewerActivity.KEY_USER";
    private static final String KEY_PHOTOSET_ID =
            "com.bourke.glimmr.PhotosetViewerActivity.KEY_PHOTOSET_ID";

    private static final String ACTION_VIEW_SET_BY_ID =
            "com.bourke.glimmr.ACTION_VIEW_SET_BY_ID";

    private static final int PHOTOSET_PAGE = 0;

    private Photoset mPhotoset;

    public static void startPhotosetViewer(Activity activity, String id) {
        Intent photosetViewer =
                new Intent(activity, PhotosetViewerActivity.class);
        photosetViewer.putExtra(KEY_PHOTOSET_ID, id);
        photosetViewer.setAction(ACTION_VIEW_SET_BY_ID);
        activity.startActivity(photosetViewer);
    }

    @Override
    protected void handleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_VIEW_SET_BY_ID)) {
            String setId = intent.getStringExtra(KEY_PHOTOSET_ID);
            new LoadPhotosetTask(this, setId).execute(mOAuth);
        } else {
            Log.e(TAG, "Unknown intent action: " + intent.getAction());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        GsonHelper gson = new GsonHelper(this);
        gson.marshallObject(mPhotoset, bundle, KEY_PHOTOSET);
        gson.marshallObject(mPhotoset, bundle, KEY_USER);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        Gson gson = new Gson();
        if (mPhotoset == null) {
            String json = bundle.getString(KEY_PHOTOSET);
            if (json != null) {
                mPhotoset = gson.fromJson(json, Photoset.class);
            } else {
                Log.e(TAG, "No photoset found in savedInstanceState");
            }
        }
        if (mUser == null) {
            String json  = bundle.getString(KEY_USER);
            if (json != null) {
                mUser = new Gson().fromJson(json, User.class);
            } else {
                Log.e(TAG, "No user found in savedInstanceState");
            }
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
            public Fragment getItemImpl(int position) {
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
        Picasso.with(this).load(mPhotoset.getPrimaryPhoto().getSmallSquareUrl())
                .into(mOverlayImage);
    }

    @Override
    public void onPhotosetReady(Photoset photoset, Exception e) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPhotosetReady");
        if (FlickrHelper.getInstance().handleFlickrUnavailable(this, e)) {
            return;
        }
        if (photoset != null) {
            mPhotoset = photoset;
            initViewPager();
            new LoadUserTask(this, this, photoset.getOwner().getId())
                    .execute();
        } else {
            Log.e(TAG, "null result received");
            // TODO: alert user of error
        }
    }

    @Override
    public void onUserReady(User user, Exception e) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onUserReady");
        if (FlickrHelper.getInstance().handleFlickrUnavailable(this, e)) {
            return;
        }
        if (user != null) {
            mUser = user;
            mPhotoset.setOwner(mUser);
            updateBottomOverlay();
        } else {
            Log.e(TAG, "onUserReady, null result received");
        }
    }
}

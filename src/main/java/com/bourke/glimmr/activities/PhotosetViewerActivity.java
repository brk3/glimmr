package com.bourke.glimmr.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import com.actionbarsherlock.app.SherlockFragment;
import com.androidquery.AQuery;
import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GlimmrPagerAdapter;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.event.Events;
import com.bourke.glimmr.fragments.photoset.PhotosetGridFragment;
import com.bourke.glimmr.tasks.LoadPhotosetTask;
import com.bourke.glimmr.tasks.LoadUserTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photosets.Photoset;

public class PhotosetViewerActivity extends BottomOverlayActivity
        implements Events.IPhotosetReadyListener, Events.IUserReadyListener {

    private static final String TAG = "Glimmr/PhotosetViewerActivity";

    private static final String PHOTOSETVIEWER_SET_FILE =
            "glimmr_photosetvieweractivity_set.json";
    private static final String PHOTOSETVIEWER_USER_FILE =
            "glimmr_photosetvieweractivity_user.json";
    private static final String KEY_PHOTOSET_ID =
            "com.bourke.glimmr.KEY_PHOTOSET_ID";

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
        if (!gson.marshallObject(mPhotoset, PHOTOSETVIEWER_SET_FILE)) {
            Log.e(TAG, "onSaveInstanceState: Error marshalling photoset");
        }
        if (!gson.marshallObject(mUser, PHOTOSETVIEWER_USER_FILE)) {
            Log.e(TAG, "onSaveInstanceSTate: Error marshalling user");
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        if (mPhotoset == null) {
            GsonHelper gsonHelper = new GsonHelper(this);
            Gson gson = new Gson();
            String json = gsonHelper.loadJson(PHOTOSETVIEWER_SET_FILE);
            if (json.length() == 0) {
                Log.e(TAG, String.format("Error reading %s",
                        PHOTOSETVIEWER_SET_FILE));
                return;
            }
            mPhotoset = gson.fromJson(json, Photoset.class);

            json = gsonHelper.loadJson(PHOTOSETVIEWER_USER_FILE);
            if (json.length() == 0) {
                Log.e(TAG, String.format("Error reading %s",
                        PHOTOSETVIEWER_USER_FILE));
                return;
            }
            mUser = new Gson().fromJson(json, User.class);
            mPhotoset.setOwner(mUser);
            initViewPager();
            updateBottomOverlay();
        }
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

    @Override
    public void onPhotosetReady(Photoset photoset) {
        if (Constants.DEBUG) Log.d(TAG, "onPhotosetReady");
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
    public void onUserReady(User user) {
        if (Constants.DEBUG) Log.d(TAG, "onUserReady");
        if (user != null) {
            mUser = user;
            mPhotoset.setOwner(mUser);
            updateBottomOverlay();
        } else {
            Log.e(TAG, "onUserReady, null result received");
        }
    }
}

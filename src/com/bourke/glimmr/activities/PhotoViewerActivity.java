package com.bourke.glimmr.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import android.graphics.Typeface;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.ViewPagerDisable;
import com.bourke.glimmr.fragments.viewer.CommentsFragment;
import com.bourke.glimmr.fragments.viewer.ExifInfoFragment;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for viewing photos.
 *
 * Receives a list of photos via an intent and shows the first one specified by
 * a startIndex in a zoomable ImageView.
 */
public class PhotoViewerActivity extends BaseActivity
        implements PhotoViewerFragment.IPhotoViewerCallbacks {

    private static final String TAG = "Glimmr/PhotoViewerActivity";

    private List<Photo> mPhotos = new ArrayList<Photo>();

    private PhotoViewerPagerAdapter mAdapter;
    private ViewPagerDisable mPager;
    private List<WeakReference<Fragment>> mFragList =
        new ArrayList<WeakReference<Fragment>>();
    private int mCurrentAdapterIndex = 0;

    private CommentsFragment mCommentsFragment;
    private ExifInfoFragment mExifFragment;
    private boolean mCommentsFragmentShowing = false;
    private boolean mExifFragmentShowing = false;

    private ActionBarTitle mActionbarTitle;
    private Configuration mConfiguration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        /* Must be called before adding content */
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoviewer_activity);

        /* Configure the actionbar.  Set custom layout to show photo
         * author/title in actionbar for large screens */
        mActionBar.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.ab_bg_black));
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionbarTitle = new ActionBarTitle(this);
        mConfiguration = getResources().getConfiguration();
        if (mConfiguration.smallestScreenWidthDp >= 600) {
            mActionbarTitle.init(mActionBar);
        }

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
            mPager.setOnPageChangeListener(mAdapter);
            mPager.setCurrentItem(startIndex);
        } else {
            Log.e(getLogTag(), "Photos from intent are null");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(Constants.KEY_PHOTOVIEWER_ACTIONBAR_SHOW,
                mActionBar.isShowing());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean overlayOn = savedInstanceState.getBoolean(
                Constants.KEY_PHOTOVIEWER_ACTIONBAR_SHOW, true);
        if (overlayOn) {
            mActionBar.show();
        } else {
            mActionBar.hide();
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

    public void onCommentsButtonClick(Photo photo) {
        boolean animateTransition = true;
        if (mExifFragmentShowing) {
            setExifFragmentVisibility(photo, false, animateTransition);
        }
        setCommentsFragmentVisibility(photo, true, animateTransition);
    }

    public void onExifButtonClick(Photo photo) {
        boolean animateTransition = true;
        if (mCommentsFragmentShowing) {
            setCommentsFragmentVisibility(photo, false, animateTransition);
        }
        setExifFragmentVisibility(photo, true, animateTransition);
    }

    /**
     * Overlay fragments are hidden/dismissed automatically onBackPressed, so
     * just need to update the state variables.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mCommentsFragmentShowing = false;
        mExifFragmentShowing = false;
    }

    private void setCommentsFragmentVisibility(Photo photo, boolean show,
            boolean animate) {
        FragmentTransaction ft =
            getSupportFragmentManager().beginTransaction();
        if (animate) {
            ft.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out);
        }
        if (show) {
            if (photo != null) {
                mCommentsFragment = CommentsFragment.newInstance(photo);
                ft.replace(R.id.commentsFragment, mCommentsFragment);
                ft.addToBackStack(null);
            } else {
                Log.e(TAG, "setCommentsFragmentVisibility: photo is null");
            }
        } else {
            ft.hide(mCommentsFragment);
            getSupportFragmentManager().popBackStack();
        }
        mCommentsFragmentShowing = show;
        ft.commit();
    }

    private void setExifFragmentVisibility(Photo photo, boolean show,
            boolean animate) {
        FragmentTransaction ft =
            getSupportFragmentManager().beginTransaction();
        if (animate) {
            ft.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out);
        }
        if (show) {
            if (photo != null) {
                mExifFragment = ExifInfoFragment.newInstance(photo);
                ft.replace(R.id.exifFragment, mExifFragment);
                ft.addToBackStack(null);
            } else {
                Log.e(TAG, "setExifFragmentVisibility: photo is null");
            }
        } else {
            ft.hide(mExifFragment);
            getSupportFragmentManager().popBackStack();
        }
        mExifFragmentShowing = show;
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.photoviewer_activity_menu,
                menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Photo currentlyShowing = mPhotos.get(mCurrentAdapterIndex);
        switch (item.getItemId()) {
            case R.id.menu_view_comments:
                onCommentsButtonClick(currentlyShowing);
                return true;
            case R.id.menu_view_exif:
                onExifButtonClick(currentlyShowing);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof PhotoViewerFragment) {
            mFragList.add(new WeakReference(fragment));
        }
    }

    @Override
    public void onVisibilityChanged(final boolean on) {
        /* If overlay is being switched off and exif/comments fragments are
         * showing, dismiss(hide) these and return */
        if (!on) {
            boolean animateTransition = true;
            if (mExifFragmentShowing) {
                setExifFragmentVisibility(null, false, true);
                return;
            }
            if (mCommentsFragmentShowing) {
                setCommentsFragmentVisibility(null, false, true);
                return;
            }
        }

        for (WeakReference<Fragment> ref : mFragList) {
            PhotoViewerFragment f = (PhotoViewerFragment) ref.get();
            if (f != null) {
                f.setOverlayVisibility(on);
            }
        }
    }

    @Override
    public void onZoomed(final boolean isZoomed) {
        mPager.setPagingEnabled(!isZoomed);
    }

    class PhotoViewerPagerAdapter extends FragmentStatePagerAdapter
            implements ViewPager.OnPageChangeListener {
        public PhotoViewerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PhotoViewerFragment.newInstance(
                    mPhotos.get(position), PhotoViewerActivity.this);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            /*
             * If comments fragment is showing update it for the current photo
             */
            if (mCommentsFragment != null && mCommentsFragmentShowing) {
                getSupportFragmentManager().popBackStack();
                boolean animateTransition = false;
                boolean show = true;
                setCommentsFragmentVisibility(mPhotos.get(position), show,
                        animateTransition);
            /* Likewise for exif */
            } else if (mExifFragment != null && mExifFragmentShowing) {
                getSupportFragmentManager().popBackStack();
                boolean animateTransition = false;
                boolean show = true;
                setExifFragmentVisibility(mPhotos.get(position), show,
                        animateTransition);
            }
            mCurrentAdapterIndex = position;

            /* If sw600dp then show the title/author in the actionbar,
             * otherwise the fragment will overlay them on the photo */
            Photo currentlyShowing = mPhotos.get(mCurrentAdapterIndex);
            if (mConfiguration.smallestScreenWidthDp >= 600) {
                String photoTitle = currentlyShowing.getTitle();
                if (photoTitle == null || photoTitle.length() == 0) {
                    photoTitle = getString(R.string.untitled);
                }
                String authorText = String.format("%s %s",
                        getString(R.string.by),
                        currentlyShowing.getOwner().getUsername());
                mActionbarTitle.setPhotoTitle(photoTitle);
                mActionbarTitle.setAuthorText(authorText);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }
    }

    class ActionBarTitle {
        private TextView mPhotoTitle;
        private TextView mPhotoAuthor;
        private Context mContext;

        public ActionBarTitle(Context context) {
            mContext = context;
        }

        public void init(ActionBar actionbar) {
            LayoutInflater inflator = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflator.inflate(R.layout.photoviewer_action_bar, null);
            mPhotoTitle = (TextView) v.findViewById(R.id.photoTitle);
            mPhotoAuthor = (TextView) v.findViewById(R.id.photoAuthor);
            Typeface robotoThin = Typeface.createFromAsset(
                    mContext.getAssets(), Constants.FONT_ROBOTOTHIN);
            Typeface robotoLight = Typeface.createFromAsset(
                    mContext.getAssets(), Constants.FONT_ROBOTOLIGHT);
            mPhotoTitle.setTypeface(robotoLight);
            mPhotoAuthor.setTypeface(robotoThin);
            actionbar.setDisplayShowCustomEnabled(true);
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setCustomView(v);
        }

        public void setPhotoTitle(String title) {
            mPhotoTitle.setText(title);
        }

        public void setAuthorText(String author) {
            mPhotoAuthor.setText(author);
        }
    }
}

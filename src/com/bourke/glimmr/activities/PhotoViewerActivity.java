package com.bourke.glimmr.activities;

import android.content.Context;
import android.content.Intent;

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
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.common.ViewPagerDisable;
import com.bourke.glimmr.fragments.viewer.CommentsFragment;
import com.bourke.glimmr.fragments.viewer.ExifInfoFragment;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

import com.google.gson.Gson;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.Collection;
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

    /**
     * Start the PhotoViewerActivity with a list of photos to view and an index
     * to start at in the list.
     */
    public static void startPhotoViewer(BaseActivity activity,
            List<Photo> photos, int pos) {
        if (photos == null) {
            Log.e(TAG, "Cannot start PhotoViewer, photos is null");
            return;
        }
        /* Code to paginate large photo lists, currently disabled as gson seems
         * reasonably efficient.
        if (photos.size() > Constants.FETCH_PER_PAGE) {
            int page = pos / Constants.FETCH_PER_PAGE;
            int pageStart = page * Constants.FETCH_PER_PAGE;
            int pageEnd = pageStart + Constants.FETCH_PER_PAGE;
            if (pageEnd > photos.size()) {
               pageEnd = photos.size();
            }
            PhotoList subList = new PhotoList();
            subList.addAll(photos.subList(pageStart, pageEnd));
            int pagePos = pos % Constants.FETCH_PER_PAGE;
        }
        */
        GsonHelper gson = new GsonHelper(activity);
        boolean photolistStoreResult =
            gson.marshallObject(photos, Constants.PHOTOVIEWER_LIST_FILE);
        if (!photolistStoreResult) {
            Log.e(TAG, "Error marshalling photos, cannot start viewer");
            return;
        }
        Intent photoViewer = new Intent(activity, PhotoViewerActivity.class);
        photoViewer.putExtra(Constants.KEY_PHOTOVIEWER_START_INDEX, pos);
        activity.startActivity(photoViewer);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "Error: null intent received in handleIntent");
            return;
        }

        GsonHelper gson = new GsonHelper(this);
        String json = gson.loadJson(Constants.PHOTOVIEWER_LIST_FILE);
        if (json.length() == 0) {
            Log.e(TAG, String.format("Error reading %s",
                        Constants.PHOTOVIEWER_LIST_FILE));
            return;
        }
        Type collectionType = new TypeToken<Collection<Photo>>(){}.getType();
        mPhotos = new Gson().fromJson(json.toString(), collectionType);

        Bundle bundle = intent.getExtras();
        int startIndex;
        if (bundle != null) {
            startIndex = bundle.getInt(Constants.KEY_PHOTOVIEWER_START_INDEX);
        } else {
            Log.e(TAG, "handleIntent: bundle is null, cannot get startIndex");
            startIndex = 0;
        }

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
        if (getResources().getBoolean(R.bool.sw600dp)) {
            mActionbarTitle.init(mActionBar);
        }

        handleIntent(getIntent());
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
        if (getResources().getBoolean(R.bool.sw600dp)) {
            boolean animateTransition = true;
            if (mExifFragmentShowing) {
                setExifFragmentVisibility(photo, false, animateTransition);
            }
            setCommentsFragmentVisibility(photo, true, animateTransition);
        } else {
            CommentsFragment commentsDialogFrag =
                CommentsFragment.newInstance(photo);
            commentsDialogFrag.show(getSupportFragmentManager(),
                    "CommentsDialogFragment");
        }
    }

    public void onExifButtonClick(Photo photo) {
        if (getResources().getBoolean(R.bool.sw600dp)) {
            boolean animateTransition = true;
            if (mCommentsFragmentShowing) {
                setCommentsFragmentVisibility(photo, false, animateTransition);
            }
            setExifFragmentVisibility(photo, true, animateTransition);
        } else {
            ExifInfoFragment exifInfoDialogFrag =
                ExifInfoFragment.newInstance(photo);
            exifInfoDialogFrag.show(getSupportFragmentManager(),
                    "ExifInfoDialogFragment");
        }
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
            if (getResources().getBoolean(R.bool.sw600dp)) {
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
            setFont(mPhotoTitle, Constants.FONT_ROBOTOLIGHT);
            setFont(mPhotoAuthor, Constants.FONT_ROBOTOTHIN);
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

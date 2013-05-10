package com.bourke.glimmr.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.Bundle;
import android.os.Handler;

import android.preference.PreferenceManager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.common.HackyViewPager;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.event.BusProvider;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmr.fragments.viewer.CommentsFragment;
import com.bourke.glimmr.fragments.viewer.PhotoInfoFragment;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment.PhotoViewerVisibilityChangeEvent;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadPhotoInfoTask;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.squareup.otto.Subscribe;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity for viewing photos.
 *
 * Receives a list of photos via an intent and shows the first one specified by
 * a startIndex in a zoomable ImageView.
 */
public class PhotoViewerActivity extends BaseActivity {

    private static final String TAG = "Glimmr/PhotoViewerActivity";

    public static final String KEY_PHOTOVIEWER_START_INDEX =
        "glimmr_photovieweractivity_start_index";
    public static final String KEY_PHOTOVIEWER_CURRENT_INDEX =
        "glimmr_photovieweractivity_current_index";
    public static final String KEY_PHOTOVIEWER_COMMENTS_SHOWING =
        "glimmr_photovieweractivity_comments_showing";
    public static final String KEY_PHOTOVIEWER_INFO_SHOWING =
        "glimmr_photovieweractivity_info_showing";
    public static final String KEY_PHOTOVIEWER_ACTIONBAR_SHOW =
        "glimmr_photovieweractivity_actionbar_show";
    public static final String KEY_PHOTOVIEWER_SLIDESHOW_RUNNING =
        "glimmr_photovieweractivity_slideshow_running";
    public static final String KEY_PHOTO_LIST_FILE =
        "com.bourke.glimmr.PHOTO_LIST_FILE";
    public static final String PHOTO_LIST_FILE =
        "glimmr_photovieweractivity_photolist.json";

    private List<Photo> mPhotos = new ArrayList<Photo>();
    private PhotoViewerPagerAdapter mAdapter;
    private HackyViewPager mPager;
    private int mCurrentAdapterIndex = 0;
    private CommentsFragment mCommentsFragment;
    private PhotoInfoFragment mPhotoInfoFragment;
    private boolean mCommentsFragmentShowing = false;
    private boolean mPhotoInfoFragmentShowing = false;
    private ActionBarTitle mActionbarTitle;
    private Timer mTimer;

    /**
     * Start the PhotoViewerActivity with a list of photos to view and an index
     * to start at in the list.
     */
    public static void startPhotoViewer(Context context, List<Photo> photos,
            int pos) {
        if (photos == null) {
            Log.e(TAG, "Cannot start PhotoViewer, photos is null");
            return;
        }
        GsonHelper gsonHelper = new GsonHelper(context);
        boolean photolistStoreResult =
            gsonHelper.marshallObject(photos, PHOTO_LIST_FILE);
        if (!photolistStoreResult) {
            Log.e(TAG, "Error marshalling photos, cannot start viewer");
            return;
        }
        Intent photoViewer = new Intent(context, PhotoViewerActivity.class);
        photoViewer.putExtra(KEY_PHOTOVIEWER_START_INDEX, pos);
        photoViewer.putExtra(KEY_PHOTO_LIST_FILE, PHOTO_LIST_FILE);
        context.startActivity(photoViewer);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "Error: null intent received in handleIntent");
            return;
        }

        final int startIndex =
            intent.getIntExtra(KEY_PHOTOVIEWER_START_INDEX, 0);

        /* If we receive a link e.g.
         * http://www.flickr.com/photos/brk3/8441328569 */
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            List<String> params = uri.getPathSegments();
            if (params.size() < 3) {
                Log.e(TAG, "Got uri '%s' but can't parse, params < 2");
                return;
            }
            String photoId = params.get(2);
            new LoadPhotoInfoTask(new IPhotoInfoReadyListener() {
                @Override
                public void onPhotoInfoReady(Photo photo) {
                    mPhotos.add(photo);
                    initViewPager(startIndex, false);
                }
            }, photoId, null).execute(mOAuth);
        /* If we receive a gson file containing a list of photos */
        } else {
            String photoListFile = intent.getStringExtra(KEY_PHOTO_LIST_FILE);
            GsonHelper gsonHelper = new GsonHelper(this);
            String json = gsonHelper.loadJson(photoListFile);
            if (json.length() == 0) {
                Log.e(TAG, String.format("Error reading '%s'", photoListFile));
                return;
            }
            Type collectionType =
                new TypeToken<Collection<Photo>>(){}.getType();
            mPhotos = new Gson().fromJson(json.toString(), collectionType);
            initViewPager(startIndex, true);
        }
    }

    private void initViewPager(int startIndex, boolean fetchExtraInfo) {
        mAdapter = new PhotoViewerPagerAdapter(getSupportFragmentManager(),
                fetchExtraInfo);
        mAdapter.onPageSelected(startIndex);
        mPager = (HackyViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(mAdapter);
        mPager.setCurrentItem(startIndex);
        mPager.setOffscreenPageLimit(2);
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
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    private void startSlideshow() {
        final Handler handler = new Handler();
        SharedPreferences defaultSharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(this);
        final int delay_m = Integer.parseInt(defaultSharedPrefs.getString(
                Constants.KEY_SLIDESHOW_INTERVAL, "3")) * 1000;
        if (Constants.DEBUG) {
            Log.d(TAG, "slideshow delay: " + delay_m);
        }
        mTimer = new Timer();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int currentPosition = mPager.getCurrentItem();
                        currentPosition++;
                        if (currentPosition >= mAdapter.getCount()) {
                            currentPosition = 0;
                        }
                        mPager.setCurrentItem(currentPosition);
                    }
                });
            }
        }, delay_m, delay_m);
        BusProvider.getInstance().post(new PhotoViewerVisibilityChangeEvent(
                !mActionBar.isShowing(), this));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // FIXME: unbelievably annoying bug that causes FragmentTransactions to
        // throw an IllegalStateException after rotate.
        // commitAllowingStateLoss doesn't help... Hence have to store pieces
        // of state manually that would otherwise be handled automatically.
        //super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(KEY_PHOTOVIEWER_ACTIONBAR_SHOW,
                mActionBar.isShowing());
        savedInstanceState.putInt(KEY_PHOTOVIEWER_CURRENT_INDEX,
                mPager.getCurrentItem());
        savedInstanceState.putBoolean(KEY_PHOTOVIEWER_COMMENTS_SHOWING,
                mCommentsFragmentShowing);
        savedInstanceState.putBoolean(KEY_PHOTOVIEWER_INFO_SHOWING,
                mPhotoInfoFragmentShowing);

        savedInstanceState.putBoolean(KEY_PHOTOVIEWER_SLIDESHOW_RUNNING,
                (mTimer != null));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean overlayOn = savedInstanceState.getBoolean(
                KEY_PHOTOVIEWER_ACTIONBAR_SHOW, true);
        if (overlayOn) {
            mActionBar.show();
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            mActionBar.hide();
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        int pagerIndex = savedInstanceState.getInt(
                KEY_PHOTOVIEWER_CURRENT_INDEX, 0);
        mPager.setCurrentItem(pagerIndex);
        mCommentsFragmentShowing = savedInstanceState.getBoolean(
                KEY_PHOTOVIEWER_COMMENTS_SHOWING, false);
        mPhotoInfoFragmentShowing = savedInstanceState.getBoolean(
                KEY_PHOTOVIEWER_INFO_SHOWING, false);
        boolean animateTransition = true;
        Photo photo = mPhotos.get(pagerIndex);
        if (mCommentsFragmentShowing) {
            setCommentsFragmentVisibility(photo, true, animateTransition);
        } else if (mPhotoInfoFragmentShowing) {
            setPhotoInfoFragmentVisibility(photo, true, animateTransition);
        }
        if (savedInstanceState.getBoolean(
                    KEY_PHOTOVIEWER_SLIDESHOW_RUNNING, false)) {
            startSlideshow();
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
            if (mPhotoInfoFragmentShowing) {
                setPhotoInfoFragmentVisibility(
                        photo, false, animateTransition);
            }
            if (mCommentsFragmentShowing) {
                setCommentsFragmentVisibility(photo, false, animateTransition);
            } else {
                setCommentsFragmentVisibility(photo, true, animateTransition);
            }
        } else {
            CommentsFragment commentsDialogFrag =
                CommentsFragment.newInstance(photo);
            commentsDialogFrag.show(getSupportFragmentManager(),
                    "CommentsDialogFragment");
        }
    }

    public void onPhotoInfoButtonClick(Photo photo) {
        if (getResources().getBoolean(R.bool.sw600dp)) {
            boolean animateTransition = true;
            if (mCommentsFragmentShowing) {
                setCommentsFragmentVisibility(photo, false, animateTransition);
            }
            if (mPhotoInfoFragmentShowing) {
                setPhotoInfoFragmentVisibility(photo, false, animateTransition);
            } else {
                setPhotoInfoFragmentVisibility(photo, true, animateTransition);
            }
        } else {
            PhotoInfoFragment photoInfoDialogFrag =
                PhotoInfoFragment.newInstance(photo);
            photoInfoDialogFrag.show(getSupportFragmentManager(),
                    "PhotoInfoFragment");
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
        mPhotoInfoFragmentShowing = false;
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

    private void setPhotoInfoFragmentVisibility(Photo photo, boolean show,
            boolean animate) {
        FragmentTransaction ft =
            getSupportFragmentManager().beginTransaction();
        if (animate) {
            ft.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out);
        }
        if (show) {
            if (photo != null) {
                mPhotoInfoFragment = PhotoInfoFragment.newInstance(photo);
                ft.replace(R.id.photoInfoFragment, mPhotoInfoFragment);
                ft.addToBackStack(null);
            } else {
                Log.e(TAG, "setPhotoInfoFragmentVisibility: photo is null");
            }
        } else {
            ft.hide(mPhotoInfoFragment);
            getSupportFragmentManager().popBackStack();
        }
        mPhotoInfoFragmentShowing = show;
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.photoviewer_activity_menu,
                menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Photo currentlyShowing = mPhotos.get(mCurrentAdapterIndex);
        switch (item.getItemId()) {
            case R.id.menu_view_comments:
                onCommentsButtonClick(currentlyShowing);
                return true;
            case R.id.menu_view_info:
                onPhotoInfoButtonClick(currentlyShowing);
                return true;
            case R.id.menu_slideshow:
                startSlideshow();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Subscribe
    public void onVisibilityChanged(
            final PhotoViewerVisibilityChangeEvent event) {
        if (Constants.DEBUG) Log.d(TAG, "onVisibilityChanged");

        /* If overlay is being switched off and info/comments fragments are
         * showing, dismiss(hide) these and return */
        if (!event.visible) {
            boolean animateTransition = true;
            if (mPhotoInfoFragmentShowing) {
                setPhotoInfoFragmentVisibility(null, false, true);
                return;
            }
            if (mCommentsFragmentShowing) {
                setCommentsFragmentVisibility(null, false, true);
                return;
            }
        }
        if (event.sender instanceof PhotoViewerFragment && mTimer != null) {
            mTimer.cancel();
            mTimer = null;  /* ensure timer isn't wrongly restarted
                               onSaveInstanceState */
            if (Constants.DEBUG) {
                Log.d(TAG, "stopping slideshow");
            }
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    class PhotoViewerPagerAdapter extends FragmentStatePagerAdapter
            implements ViewPager.OnPageChangeListener {
        private boolean mFetchExtraInfo;

        public PhotoViewerPagerAdapter(FragmentManager fm,
                boolean fetchExtraInfo) {
            super(fm);
            mFetchExtraInfo = fetchExtraInfo;
        }

        @Override
        public Fragment getItem(int position) {
            return PhotoViewerFragment.newInstance(mPhotos.get(position),
                    mFetchExtraInfo);
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
            /* Likewise for info */
            } else if (mPhotoInfoFragment != null &&
                    mPhotoInfoFragmentShowing) {
                getSupportFragmentManager().popBackStack();
                boolean animateTransition = false;
                boolean show = true;
                setPhotoInfoFragmentVisibility(mPhotos.get(position), show,
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
            mTextUtils.setFont(mPhotoTitle, TextUtils.FONT_ROBOTOLIGHT);
            mTextUtils.setFont(mPhotoAuthor, TextUtils.FONT_ROBOTOTHIN);
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

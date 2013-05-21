package com.bourke.glimmr.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.bourke.glimmr.R;
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
import com.bourke.glimmr.tasks.LoadPhotoInfoTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.flickrjandroid.photos.Photo;
import com.squareup.otto.Subscribe;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Activity for viewing fullscreen photos in a ViewPager.
 */
public class PhotoViewerActivity extends BaseActivity
        implements IPhotoInfoReadyListener {

    private static final String TAG = "Glimmr/PhotoViewerActivity";

    public static final String KEY_PHOTOVIEWER_START_INDEX =
        "glimmr_photovieweractivity_start_index";
    private static final String KEY_PHOTOVIEWER_CURRENT_INDEX =
        "glimmr_photovieweractivity_current_index";
    private static final String KEY_PHOTOVIEWER_COMMENTS_SHOWING =
        "glimmr_photovieweractivity_comments_showing";
    private static final String KEY_PHOTOVIEWER_INFO_SHOWING =
        "glimmr_photovieweractivity_info_showing";
    private static final String KEY_PHOTOVIEWER_ACTIONBAR_SHOW =
        "glimmr_photovieweractivity_actionbar_show";
    private static final String KEY_PHOTOVIEWER_SLIDESHOW_RUNNING =
        "glimmr_photovieweractivity_slideshow_running";
    public static final String KEY_PHOTO_LIST_FILE =
        "com.bourke.glimmr.PHOTO_LIST_FILE";
    public static final String PHOTO_LIST_FILE =
        "glimmr_photovieweractivity_photolist.json";
    public static final String KEY_PHOTO_ID =
        "com.bourke.glimmr.KEY_PHOTO_ID";
    public static final String KEY_INTENT_CONSUMED =
        "com.bourke.glimmr.KEY_INTENT_CONSUMED";

    /* intent actions */
    public static final String ACTION_VIEW_PHOTO_BY_ID =
            "com.bourke.glimmr.ACTION_VIEW_PHOTO_BY_ID";
    public static final String ACTION_VIEW_PHOTOLIST =
            "com.bourke.glimmr.ACTION_VIEW_PHOTOLIST";

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
     * Start PhotoViewerActivity to view a list of photos, starting at a
     * specific index.
     * @param context
     * @param photos
     * @param index
     */
    public static void startPhotoViewer(Context context, List<Photo> photos,
            int index) {
        if (new GsonHelper(context).marshallObject(photos, PHOTO_LIST_FILE)) {
            Intent photoViewer =
                    new Intent(context, PhotoViewerActivity.class);
            photoViewer.setAction(ACTION_VIEW_PHOTOLIST);
            photoViewer.putExtra(KEY_PHOTOVIEWER_START_INDEX, index);
            photoViewer.putExtra(KEY_PHOTO_LIST_FILE, PHOTO_LIST_FILE);
            context.startActivity(photoViewer);
        } else {
            Log.e(TAG, "Error marshalling photo list, cannot start viewer");
        }
    }

    /**
     * Start PhotoViewerActivity to view a photo id.
     * @param context
     * @param photoId
     */
    public static void startPhotoViewer(Context context, String photoId) {
        Intent photoViewer = new Intent(context, PhotoViewerActivity.class);
        photoViewer.setAction(ACTION_VIEW_PHOTO_BY_ID);
        photoViewer.putExtra(KEY_PHOTO_ID, photoId);
        context.startActivity(photoViewer);
    }

    private void handleIntent(Intent intent) {
        if (intent.getBooleanExtra(KEY_INTENT_CONSUMED, false)) {
            /* prevent the intent getting executed twice on rotate */
            if (Constants.DEBUG) Log.d(TAG, "KEY_INTENT_CONSUMED true");
            return;
        }
        final int startIndex =
                intent.getIntExtra(KEY_PHOTOVIEWER_START_INDEX, 0);
        if (intent.getAction().equals(ACTION_VIEW_PHOTO_BY_ID)) {
            if (Constants.DEBUG) {
                Log.d(TAG, "Received ACTION_VIEW_PHOTO_BY_ID intent");
            }
            intent.putExtra(KEY_INTENT_CONSUMED, true);
            String photoId = intent.getStringExtra(KEY_PHOTO_ID);
            new LoadPhotoInfoTask(this, photoId).execute(mOAuth);
        } else if (intent.getAction().equals(ACTION_VIEW_PHOTOLIST)) {
            if (Constants.DEBUG) {
                Log.d(TAG, "Received ACTION_VIEW_PHOTOLIST intent");
            }
            intent.putExtra(KEY_INTENT_CONSUMED, true);
            String photoListFile = intent.getStringExtra(KEY_PHOTO_LIST_FILE);
            GsonHelper gsonHelper = new GsonHelper(this);
            String json = gsonHelper.loadJson(photoListFile);
            if (json.length() > 0) {
                Type collectionType =
                        new TypeToken<Collection<Photo>>(){}.getType();
                mPhotos = new Gson().fromJson(json, collectionType);
                initViewPager(startIndex, true);
            } else {
                Log.e(TAG, String.format("Error reading '%s'", photoListFile));
            }
        } else {
            Log.e(TAG, "Unknown intent action: " + intent.getAction());
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
        if (mPhotos != null) {
            if (!new GsonHelper(this)
                    .marshallObject(mPhotos, PHOTO_LIST_FILE)) {
                Log.e(TAG, "onSaveInstanceState: Error marshalling mPhotos");
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mPhotos.isEmpty()) {
            String json = new GsonHelper(this).loadJson(PHOTO_LIST_FILE);
            if (json.length() == 0) {
                Log.e(TAG, String.format("Error reading '%s'",
                        PHOTO_LIST_FILE));
            } else {
                Type collectionType =
                        new TypeToken<Collection<Photo>>(){}.getType();
                mPhotos = new Gson().fromJson(json, collectionType);
            }
        }
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
        initViewPager(pagerIndex, false);
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

    @Override
    public void onPhotoInfoReady(Photo photo) {
        if (Constants.DEBUG) Log.d(TAG, "onPhotoInfoReady");
        if (photo != null) {
            mPhotos.add(photo);
            initViewPager(0, false);
        } else {
            Log.e(TAG, "null result received");
            // TODO: alert user of error
        }
    }

    class PhotoViewerPagerAdapter extends FragmentStatePagerAdapter
            implements ViewPager.OnPageChangeListener {
        private final boolean mFetchExtraInfo;

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
        private final Context mContext;

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

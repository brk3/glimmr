package com.bourke.glimmrpro.fragments.viewer;

import android.annotation.SuppressLint;

import android.app.WallpaperManager;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import android.graphics.Typeface;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.event.Events.IFavoriteReadyListener;
import com.bourke.glimmrpro.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmrpro.fragments.base.BaseFragment;
import com.bourke.glimmrpro.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.tasks.LoadPhotoInfoTask;
import com.bourke.glimmrpro.tasks.SetFavoriteTask;

import com.googlecode.flickrjandroid.photos.Photo;

import com.polites.android.GestureImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.util.concurrent.atomic.AtomicBoolean;

public final class PhotoViewerFragment extends BaseFragment
        implements IPhotoInfoReadyListener, IFavoriteReadyListener {

    private final static String TAG = "Glimmr/PhotoViewerFragment";

    private Photo mBasePhoto;
    private Photo mPhotoExtendedInfo;
    private AQuery mAq;
    private MenuItem mFavoriteButton;
    private LoadPhotoInfoTask mTask;
    private AtomicBoolean mIsFavoriting = new AtomicBoolean(false);
    private IPhotoViewerCallbacks mListener;
    private ActionBarTitle mActionbarTitle;

    public static PhotoViewerFragment newInstance(Photo photo,
            IPhotoViewerCallbacks listener) {
        if (Constants.DEBUG) Log.d(TAG, "newInstance");
        PhotoViewerFragment photoFragment = new PhotoViewerFragment();
        photoFragment.mBasePhoto = photo;
        photoFragment.mListener = listener;
        return photoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreateView");
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.photoviewer_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);

        mActionbarTitle = new ActionBarTitle(mActivity);
        Configuration config = mActivity.getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            mActionbarTitle.init(mActionBar);
        }

        mAq.id(R.id.image).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Set our own visiblity and update others */
                setOverlayVisibility(mActionBar.isShowing());
                mListener.onVisibilityChanged();
            }
        });

        final GestureImageView i =
            (GestureImageView) mLayout.findViewById(R.id.image);
        i.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mListener.onZoomed(i.isZoomed());
                return false;
            }
        });

        refreshOverlayVisibility();
        displayImage();

        return mLayout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreateOptionsMenu");
        inflater.inflate(R.menu.photoviewer_menu, menu);
        mFavoriteButton = menu.findItem(R.id.menu_favorite);
        /* The task could return before this has inflated, so make sure it's up
         * to date */
        if (mPhotoExtendedInfo != null) {
            updateFavoriteButtonIcon(mPhotoExtendedInfo.isFavorite());
        }
        /* Set file with share history to the provider and set the share
         * intent. */
        MenuItem shareActionItem = menu.findItem(R.id.menu_share);
        ShareActionProvider shareActionProvider =
            (ShareActionProvider) shareActionItem.getActionProvider();
        shareActionProvider.setShareHistoryFileName(
                ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        shareActionProvider.setShareIntent(createShareIntent());
    }

    /**
     * Creates a sharing {@link Intent}.
     *
     * @return The sharing intent.
     */
    private Intent createShareIntent() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        String text = "";
        try {
            text = String.format("\"%s\" %s %s: %s", mBasePhoto.getTitle(),
                    mActivity.getString(R.string.by),
                    mBasePhoto.getOwner().getUsername(),
                    mBasePhoto.getUrl());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        intent.putExtra(Intent.EXTRA_TEXT, text);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_favorite:
                onFavoriteButtonClick();
                return true;
            case R.id.menu_set_wallpaper:
                onWallpaperButtonClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onFavoriteButtonClick() {
        if (mActivity.getUser() == null) {
            Toast.makeText(mActivity, getString(R.string.login_required),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mIsFavoriting.get()) {
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "Favorite operation currently in progress");
            }
            return;
        }
        if (mPhotoExtendedInfo != null) {
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "Starting SetFavoriteTask for photo: "
                        + mPhotoExtendedInfo.getId());
            }
            new SetFavoriteTask(this, this, mPhotoExtendedInfo)
                .execute(mOAuth);
            updateFavoriteButtonIcon(mPhotoExtendedInfo.isFavorite());
            mIsFavoriting.set(true);
        } else {
            Log.e(TAG, "onFavoriteButtonClick: mPhotoExtendedInfo is null");
        }
    }

    @Override
    public void onFavoriteComplete(Exception e) {
        if (e != null) {
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "Error setting favorite on photo");
            }
            return;
        } else {
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "Successfully favorited/unfavorited photo");
            }
            mPhotoExtendedInfo.setFavorite(!mPhotoExtendedInfo.isFavorite());
        }
        mIsFavoriting.set(false);
    }

    /**
     * Update the icon the favorites button based on the state of the current
     * photo.
     */
    public void updateFavoriteButtonIcon(boolean favorite) {
        if (Constants.DEBUG) {
            Log.d(getLogTag(), "updateFavoriteButtonIcon: " + favorite);
        }
        if (mFavoriteButton != null) {
            if (favorite) {
                mFavoriteButton.setIcon(R.drawable.ic_rating_important_dark);
            } else {
                mFavoriteButton.setIcon(
                        R.drawable.ic_rating_not_important_dark);
            }
        } else {
            if (Constants.DEBUG) Log.d(getLogTag(), "mFavoriteButton null");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Constants.DEBUG) Log.d(TAG, "onPause");
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    @Override
    protected void startTask() {
        super.startTask();
        /* Start a task to fetch more detailed info about the photo if we don't
         * already have it (required for favorite status) */
        if (mPhotoExtendedInfo == null) {
            mTask = new LoadPhotoInfoTask(this, this, mBasePhoto);
            mTask.execute(mOAuth);
        }
    }

    @Override
    public void onPhotoInfoReady(Photo photo) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onPhotoInfoReady");
        mPhotoExtendedInfo = photo;
        if (mPhotoExtendedInfo != null) {
            updateFavoriteButtonIcon(mPhotoExtendedInfo.isFavorite());
        }
    }

    private void displayImage() {
        if (Constants.DEBUG) Log.d(TAG, "displayImage()");
        if (mBasePhoto != null) {
            /* Fetch the main image */
            mAq.id(R.id.image).progress(R.id.progress).image(
                    mBasePhoto.getLargeUrl(), Constants.USE_MEMORY_CACHE,
                    Constants.USE_FILE_CACHE, 0, 0, null,
                    AQuery.FADE_IN_NETWORK);

            /* Set the photo title and author text */
            String photoTitle = mBasePhoto.getTitle();
            if (photoTitle == null || photoTitle.length() == 0) {
                photoTitle = mActivity.getString(R.string.untitled);
            }
            String authorText = String.format("%s %s",
                    mActivity.getString(R.string.by),
                    mBasePhoto.getOwner().getUsername());

            /* If sw600dp then show the title/author in the actionbar,
             * otherwise overlay them on the photo */
            Configuration config = mActivity.getResources().getConfiguration();
            if (config.smallestScreenWidthDp >= 600) {
                mActionbarTitle.setPhotoTitle(photoTitle);
                mActionbarTitle.setAuthorText(authorText);
            } else {
                mAq.id(R.id.textViewTitle).text(photoTitle);
                mAq.id(R.id.textViewAuthor).text(authorText);
            }
        } else {
            Log.e(getLogTag(), "displayImage: mBasePhoto is null");
        }
    }

    @SuppressLint("NewApi")
    public void setOverlayVisibility(boolean on) {
        boolean honeycombOrGreater =
            (android.os.Build.VERSION.SDK_INT >=
             android.os.Build.VERSION_CODES.HONEYCOMB);
        if (on) {
            mAq.id(R.id.textViewTitle).invisible();
            mAq.id(R.id.textViewAuthor).invisible();
            mActionBar.hide();
            if (honeycombOrGreater) {
                mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        } else {
            mAq.id(R.id.textViewTitle).visible();
            mAq.id(R.id.textViewAuthor).visible();
            mActionBar.show();
            if (honeycombOrGreater) {
                mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

    /**
     * The hidden actionbar persists throughout fragments but the other hidden
     * elements do not, so this is used to rehide/unhide them.
     */
    public void refreshOverlayVisibility() {
        if (mActionBar.isShowing()) {
            mAq.id(R.id.textViewTitle).visible();
            mAq.id(R.id.textViewAuthor).visible();
        } else {
            mAq.id(R.id.textViewTitle).invisible();
            mAq.id(R.id.textViewAuthor).invisible();
        }
    }

    private void onWallpaperButtonClick() {
        if (mBasePhoto != null) {
            File imageFile = mAq.getCachedFile(mBasePhoto.getLargeUrl());
            if (imageFile != null) {
                InputStream is;
                try {
                    is = new FileInputStream(imageFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
                WallpaperManager wallpaperManager =
                    WallpaperManager.getInstance(mActivity);
                try {
                    wallpaperManager.setStream(is);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                Toast.makeText(mActivity,
                        mActivity.getString(R.string.setting_wallpaper),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public interface IPhotoViewerCallbacks {
        void onVisibilityChanged();
        void onZoomed(boolean isZoomed);
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

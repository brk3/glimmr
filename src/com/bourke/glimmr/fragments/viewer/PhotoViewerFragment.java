package com.bourke.glimmrpro.fragments.viewer;

import android.annotation.SuppressLint;

import android.app.WallpaperManager;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import android.graphics.Bitmap;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.event.Events.IFavoriteReadyListener;
import com.bourke.glimmrpro.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmrpro.fragments.base.BaseFragment;
import com.bourke.glimmrpro.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.tasks.LoadPhotoInfoTask;
import com.bourke.glimmrpro.tasks.SetFavoriteTask;

import com.googlecode.flickrjandroid.photos.Photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.util.concurrent.atomic.AtomicBoolean;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

public final class PhotoViewerFragment extends BaseFragment
        implements IPhotoInfoReadyListener, IFavoriteReadyListener {

    private final static String TAG = "Glimmr/PhotoViewerFragment";

    private Photo mBasePhoto;
    private Photo mPhotoExtendedInfo;
    private MenuItem mFavoriteButton;
    private LoadPhotoInfoTask mTask;
    private AtomicBoolean mIsFavoriting = new AtomicBoolean(false);
    private IPhotoViewerCallbacks mListener;
    private Configuration mConfiguration;
    private ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private TextView mTextViewTitle;
    private TextView mTextViewAuthor;

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
        if (Constants.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mConfiguration = mActivity.getResources().getConfiguration();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreateView");
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.photoviewer_fragment, container, false);
        mImageView = (ImageView) mLayout.findViewById(R.id.image);
        mAttacher = new PhotoViewAttacher(mImageView);
        mTextViewTitle = (TextView) mLayout.findViewById(R.id.textViewTitle);
        mTextViewAuthor = (TextView) mLayout.findViewById(R.id.textViewAuthor);
        mAq = new AQuery(mActivity, mLayout);

        mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                mListener.onVisibilityChanged(!mActionBar.isShowing());
            }
        });

        /* If this fragment is new as part of a set, update it's overlay
         * visibility based on the state of the actionbar */
        setOverlayVisibility(mActionBar.isShowing());

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
            new SetFavoriteTask(this, mPhotoExtendedInfo).execute(mOAuth);
            updateFavoriteButtonIcon(!mPhotoExtendedInfo.isFavorite());
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
            mTask = new LoadPhotoInfoTask(this, mBasePhoto.getId(),
                    mBasePhoto.getSecret());
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
                    Constants.USE_FILE_CACHE, 0, 0, new BitmapAjaxCallback(){
                        @Override
                        public void callback(String url, ImageView iv,
                                Bitmap bm, AjaxStatus status) {
                            iv.setImageBitmap(bm);
                            mAttacher.update();
                        }
                    });

            /* Set the photo title and author text.  If sw600dp then the parent
             * activity will handle adding them to the actionbar instead */
            if (!mActivity.getResources().getBoolean(R.bool.sw600dp)) {
                String photoTitle = mBasePhoto.getTitle();
                if (photoTitle == null || photoTitle.length() == 0) {
                    photoTitle = mActivity.getString(R.string.untitled);
                }
                String authorText = String.format("%s %s",
                        mActivity.getString(R.string.by),
                        mBasePhoto.getOwner().getUsername());
                mTextViewTitle.setText(photoTitle);
                mTextViewAuthor.setText(authorText);
            }
        } else {
            Log.e(getLogTag(), "displayImage: mBasePhoto is null");
        }
    }

    @SuppressLint("NewApi")
    public void setOverlayVisibility(final boolean on) {
        boolean honeycombOrGreater =
            (android.os.Build.VERSION.SDK_INT >=
             android.os.Build.VERSION_CODES.HONEYCOMB);
        if (on) {
            mTextViewTitle.setVisibility(View.VISIBLE);
            mTextViewAuthor.setVisibility(View.VISIBLE);
            if (honeycombOrGreater) {
                mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            mActivity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            mActivity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mActionBar.show();
        } else {
            mTextViewTitle.setVisibility(View.INVISIBLE);
            mTextViewAuthor.setVisibility(View.INVISIBLE);
            mActivity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mActivity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            if (honeycombOrGreater) {
                mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
            mActionBar.hide();
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
        void onVisibilityChanged(final boolean on);
    }
}

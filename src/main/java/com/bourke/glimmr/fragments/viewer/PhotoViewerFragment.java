package com.bourke.glimmr.fragments.viewer;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.content.res.Configuration;

import android.graphics.Bitmap;

import android.media.MediaScannerConnection;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;

import com.bourke.glimmr.activities.ProfileActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.BusProvider;
import com.bourke.glimmr.event.Events.IFavoriteReadyListener;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmr.event.Events.IPhotoSizesReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadPhotoInfoTask;
import com.bourke.glimmr.tasks.LoadPhotoSizesTask;
import com.bourke.glimmr.tasks.SetFavoriteTask;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.Size;

import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

public final class PhotoViewerFragment extends BaseFragment
        implements IPhotoInfoReadyListener, IFavoriteReadyListener,
                   IPhotoSizesReadyListener {

    private final static String TAG = "Glimmr/PhotoViewerFragment";

    private static final String KEY_BASEPHOTO = "glimmr_photoviewer_basephoto";

    private Photo mBasePhoto;
    private Photo mPhotoExtendedInfo;
    private MenuItem mFavoriteButton;
    private LoadPhotoInfoTask mTask;
    private AtomicBoolean mIsFavoriting = new AtomicBoolean(false);
    private Configuration mConfiguration;
    private ImageView mImageView;
    private ImageView mVideoButton;
    private PhotoViewAttacher mAttacher;
    private TextView mTextViewTitle;
    private TextView mTextViewAuthor;

    /**
     * Returns a new instance of PhotoViewerFragment.
     *
     * @param photo             Basic Photo object as returned by say
     *                          flickr.people.getPhotos
     * @param fetchExtraInfo    Set to false to disable a call to
     *                          flickr.photos.getInfo if photo already has this
     *                          info.
     */
    public static PhotoViewerFragment newInstance(Photo photo,
            boolean fetchExtraInfo) {
        if (Constants.DEBUG) Log.d(TAG, "newInstance");

        PhotoViewerFragment photoFragment = new PhotoViewerFragment();
        photoFragment.mBasePhoto = photo;

        if (!fetchExtraInfo) {
            photoFragment.mPhotoExtendedInfo = photo;
        }

        return photoFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Constants.DEBUG) Log.d(TAG, "onPause");
        BusProvider.getInstance().unregister(this);
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
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
        mVideoButton = (ImageView) mLayout.findViewById(
                R.id.play_video_overlay);
        mAttacher = new PhotoViewAttacher(mImageView);
        mTextViewTitle = (TextView) mLayout.findViewById(R.id.textViewTitle);
        mTextViewAuthor = (TextView) mLayout.findViewById(R.id.textViewAuthor);
        mAq = new AQuery(mActivity, mLayout);

        mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                BusProvider.getInstance().post(
                    new PhotoViewerVisibilityChangeEvent(
                        !mActionBar.isShowing()));
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

    /* NOTE: duplicate onOptionsItemSelected here and in parent activity, see
     * comments in photoviweer_activity_menu.xml */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_favorite:
                onFavoriteButtonClick();
                return true;
            case R.id.menu_view_profile:
                ProfileActivity.startProfileViewer(
                        mActivity, mBasePhoto.getOwner());
                return true;
            case R.id.menu_save_image:
                saveImageToExternalStorage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveImageToExternalStorage() {
        String url = getLargestUrlAvailable(mBasePhoto);
        File file = mAq.makeSharedFile(url, mBasePhoto.getTitle() + ".jpg");
        if (file != null) {
            createExternalStoragePublicPicture(file);
        } else {
            Log.e(TAG, "Couldn't save image, makeSharedFile returned null");
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
    protected void startTask() {
        super.startTask();
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
        /* Start a task to fetch more detailed info about the photo if we don't
         * already have it (required for favorite status) */
        if (mPhotoExtendedInfo == null) {
            mTask = new LoadPhotoInfoTask(this, mBasePhoto.getId(),
                    mBasePhoto.getSecret());
            mTask.execute(mOAuth);
        } else {
            onPhotoInfoReady(mPhotoExtendedInfo);
        }
    }

    @Override
    public void onPhotoInfoReady(Photo photo) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onPhotoInfoReady");
        mPhotoExtendedInfo = photo;
        if (mPhotoExtendedInfo != null) {
            /* update favorite button */
            updateFavoriteButtonIcon(mPhotoExtendedInfo.isFavorite());

            /* if this photo is actually a video, show a play button overlay
             * and set up intent to play it */
            if (mPhotoExtendedInfo.getMedia().equals("video")) {
                mVideoButton.setVisibility(View.VISIBLE);
                mVideoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivity.setSupportProgressBarIndeterminateVisibility(
                            Boolean.TRUE);
                        new LoadPhotoSizesTask(PhotoViewerFragment.this,
                            mBasePhoto.getId()).execute();
                    }
                });
            }
        }
    }

    @Override
    public void onPhotoSizesReady(List<Size> sizes) {
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
        if (sizes != null && sizes.size() > 0) {
            for (Size s : sizes) {
                if (s.getLabel() == Size.MOBILE_MP4) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(s.getSource()), "video/*");
                    startActivity(intent);
                    break;
                }
            }
        } else {
            /* would like to use crouton here but the overlay actionbar makes
             * the crouton also transparent, and hard to read */
            Toast.makeText(mActivity, R.string.couldnt_load_video,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "List of sizes null or empty, cannot create play " +
                    "video intent");
        }
    }

    /**
     * Return the largest size available for a given photo.
     *
     * All should have medium, but not all have large.
     */
    private String getLargestUrlAvailable(Photo photo) {
        String url = "";
        Size size = photo.getLargeSize();
        if (size != null) {
            url = photo.getLargeUrl();
        } else {
            /* No large size available, fall back to medium */
            url = photo.getMediumUrl();
        }
        return url;
    }

    private void displayImage() {
        if (Constants.DEBUG) Log.d(TAG, "displayImage()");
        /* Fetch the main image */
        if (mBasePhoto != null) {
            String urlToFetch = getLargestUrlAvailable(mBasePhoto);
            mAq.id(R.id.image).progress(R.id.progress).image(
                    urlToFetch, Constants.USE_MEMORY_CACHE,
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

    @Subscribe
    public void onVisibilityChanged(
            final PhotoViewerVisibilityChangeEvent event) {
        if (Constants.DEBUG) Log.d(TAG, "onVisibilityChanged");
        setOverlayVisibility(event.visible);
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

    /**
     * Save an image to external storage.
     *
     * http://developer.android.com/reference/android/os/Environment.html
     */
    private void createExternalStoragePublicPicture(File image) {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, image.getName());
        try {
            path.mkdirs();

            /* copy the file from cache to external storage */
            InputStream fis = new FileInputStream(image);
            OutputStream fos = new FileOutputStream(file);
            AQUtility.copy(fis, fos);

            /* Tell the media scanner about the new file so that it is
             * immediately available to the user. */
            MediaScannerConnection.scanFile(mActivity,
                    new String[] { file.toString() }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });

            Toast.makeText(mActivity, getString(R.string.image_saved),
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            /* Unable to create file, likely because external storage is not
             * currently mounted. */
            Log.e("ExternalStorage", "Error writing " + file, e);
            Toast.makeText(mActivity, getString(R.string.storage_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    /**
     * Event published when the main photo is clicked.
     */
    public static class PhotoViewerVisibilityChangeEvent {
        public boolean visible;

        public PhotoViewerVisibilityChangeEvent(final boolean visible) {
            this.visible = visible;
        }
    }
}

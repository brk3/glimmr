package com.bourke.glimmr.fragments.viewer;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.bourke.glimmr.BuildConfig;
import com.bourke.glimmr.R;
import com.bourke.glimmr.activities.ProfileViewerActivity;
import com.bourke.glimmr.common.FlickrHelper;
import com.bourke.glimmr.event.BusProvider;
import com.bourke.glimmr.event.Events;
import com.bourke.glimmr.event.Events.IFavoriteReadyListener;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmr.event.Events.IPhotoSizesReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.tasks.DownloadPhotoTask;
import com.bourke.glimmr.tasks.LoadPhotoInfoTask;
import com.bourke.glimmr.tasks.LoadPhotoSizesTask;
import com.bourke.glimmr.tasks.SetFavoriteTask;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.Size;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

public final class PhotoViewerFragment extends BaseFragment
        implements IPhotoInfoReadyListener, IFavoriteReadyListener, IPhotoSizesReadyListener {

    private final static String TAG = "Glimmr/PhotoViewerFragment";

    private static final String KEY_BASEPHOTO = "glimmr_photoviewer_basephoto";
    private final AtomicBoolean mIsFavoriting = new AtomicBoolean(false);

    private Photo mBasePhoto;
    private Photo mPhotoExtendedInfo;

    private MenuItem mFavoriteButton;
    private MenuItem mWallpaperButton;

    private LoadPhotoInfoTask mTask;

    private ImageView mVideoButton;
    private PhotoViewAttacher mAttacher;
    private TextView mTextViewTitle;
    private TextView mTextViewAuthor;
    private ImageView mImageView;
    private ProgressBar mProgress;

    private int mNum;

    public static PhotoViewerFragment newInstance(Photo photo, boolean fetchExtraInfo, int num) {
        if (BuildConfig.DEBUG) Log.d(TAG, "newInstance");

        PhotoViewerFragment photoFragment = new PhotoViewerFragment();
        photoFragment.mBasePhoto = photo;

        if (!fetchExtraInfo) {
            photoFragment.mPhotoExtendedInfo = photo;
        }

        Bundle args = new Bundle();
        args.putInt("num", num);
        photoFragment.setArguments(args);

        return photoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mNum = savedInstanceState.getInt("mNum", 0);
            mBasePhoto = (Photo) savedInstanceState.getSerializable(mNum + "_basePhoto");
        }

        initUIVisibilityChangeListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onCreateOptionsMenu");

        inflater.inflate(R.menu.photoviewer_menu, menu);
        mFavoriteButton = menu.findItem(R.id.menu_favorite);
        mWallpaperButton = menu.findItem(R.id.menu_set_wallpaper);

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

    /* NOTE: duplicate onOptionsItemSelected here and in parent activity, see
     * comments in photoviewer_activity_menu.xml */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_favorite:
                onFavoriteButtonClick();
                return true;
            case R.id.menu_view_profile:
                Intent profileViewer = new Intent(mActivity,
                        ProfileViewerActivity.class);
                profileViewer.putExtra(ProfileViewerActivity.KEY_PROFILE_ID,
                        mBasePhoto.getOwner().getId());
                profileViewer.setAction(
                        ProfileViewerActivity.ACTION_VIEW_USER_BY_ID);
                startActivity(profileViewer);
                return true;
            case R.id.menu_save_image:
                saveImageToExternalStorage();
                return true;
            case R.id.menu_set_wallpaper:
                onWallpaperButtonClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onCreateView");
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.photoviewer_fragment, container, false);
        mVideoButton = (ImageView) mLayout.findViewById(
                R.id.play_video_overlay);
        mImageView = (ImageView) mLayout.findViewById(R.id.image);
        mAttacher = new PhotoViewAttacher(mImageView);
        mTextViewTitle = (TextView) mLayout.findViewById(R.id.textViewTitle);
        mTextViewAuthor = (TextView) mLayout.findViewById(R.id.textViewAuthor);
        mProgress = (ProgressBar) mLayout.findViewById(R.id.progress);

        mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                BusProvider.getInstance().post(
                        new PhotoViewerVisibilityChangeEvent(
                                !mActionBar.isShowing(), PhotoViewerFragment.this)
                );
            }
        });

        /* If this fragment is new as part of a set, update it's overlay
         * visibility based on the state of the actionbar */
        setOverlayVisibility(mActionBar.isShowing());

        displayImage();

        return mLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("mNum", mNum);
        savedInstanceState.putSerializable(mNum + "_basePhoto", mBasePhoto);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    public void onFavoriteButtonClick() {
        if (mOAuth == null || mOAuth.getUser() == null) {
            Toast.makeText(mActivity, getString(R.string.login_required),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mIsFavoriting.get()) {
            if (BuildConfig.DEBUG) {
                Log.d(getLogTag(), "Favorite operation currently in progress");
            }
            return;
        }
        if (mPhotoExtendedInfo != null) {
            if (BuildConfig.DEBUG) {
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
        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, e)) {
            return;
        }
        if (e != null) {
            return;
        } else {
            if (BuildConfig.DEBUG) {
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
        if (BuildConfig.DEBUG) {
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
            if (BuildConfig.DEBUG) Log.d(getLogTag(), "mFavoriteButton null");
        }
    }

    @Override
    public void onPhotoInfoReady(Photo photo, Exception e) {
        if (BuildConfig.DEBUG) Log.d(getLogTag(), "onPhotoInfoReady");
        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, e)) {
            return;
        }
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
                        mActivity.setProgressBarIndeterminateVisibility(
                                Boolean.TRUE);
                        new LoadPhotoSizesTask(PhotoViewerFragment.this,
                                mBasePhoto.getId()).execute();
                    }
                });
            }
        }
    }

    @Override
    public void onPhotoSizesReady(List<Size> sizes, Exception e) {
        mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
        if (FlickrHelper.getInstance().handleFlickrUnavailable(mActivity, e)) {
            return;
        }
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

    @Subscribe
    public void onVisibilityChanged(
            final PhotoViewerVisibilityChangeEvent event) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onVisibilityChanged");
        setOverlayVisibility(event.visible);
    }

    @SuppressLint("NewApi")
    public void setOverlayVisibility(final boolean on) {
        if (on) {
            mTextViewTitle.setVisibility(View.VISIBLE);
            mTextViewAuthor.setVisibility(View.VISIBLE);
            mLayout.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            mActionBar.show();
        } else {
            mTextViewTitle.setVisibility(View.INVISIBLE);
            mTextViewAuthor.setVisibility(View.INVISIBLE);
            mLayout.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
            );
            mActionBar.hide();
        }
    }

    @Override
    protected void startTask() {
        super.startTask();
        mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
        /* Start a task to fetch more detailed info about the photo if we don't
         * already have it (required for favorite status) */
        if (mPhotoExtendedInfo == null) {
            mTask = new LoadPhotoInfoTask(this, mBasePhoto.getId(),
                    mBasePhoto.getSecret());
            mTask.execute(mOAuth);
        } else {
            onPhotoInfoReady(mPhotoExtendedInfo, null);
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    private void initUIVisibilityChangeListener() {
    /* if user swipes down from immersive mode we need to know to reshow photo title etc. */
        View decorView = mActivity.getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            BusProvider.getInstance().post(
                                    new PhotoViewerVisibilityChangeEvent(
                                            true, PhotoViewerFragment.this)
                            );
                        }
                    }
                });
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

    private void onWallpaperButtonClick() {
        Toast.makeText(mActivity, mActivity.getString(R.string.setting_wallpaper),
                Toast.LENGTH_SHORT).show();
        if (mBasePhoto != null) {
            String url = getLargestUrlAvailable(mBasePhoto);
            new DownloadPhotoTask(mActivity, new Events.IPhotoDownloadedListener() {
                @Override
                public void onPhotoDownloaded(Bitmap bitmap, Exception e) {
                    if (e == null) {
                        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mActivity);
                        try {
                            wallpaperManager.setBitmap(bitmap);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "Error setting wallpaper");
                        e.printStackTrace();
                    }
                }
            }, url).execute();
        }
    }

    private void saveImageToExternalStorage() {
        String url = getLargestUrlAvailable(mBasePhoto);
        new DownloadPhotoTask(mActivity, new Events.IPhotoDownloadedListener() {
            @Override
            public void onPhotoDownloaded(Bitmap bitmap, Exception e) {
                String filename = mBasePhoto.getTitle() + ".jpg";
                if (e == null && createExternalStoragePublicPicture(bitmap, filename) != null) {
                    Toast.makeText(mActivity, getString(R.string.image_saved), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(mActivity, getString(R.string.storage_error), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }, url).execute();
    }

    /**
     * Return the largest size available for a given photo.
     * <p/>
     * All should have medium, but not all have large.
     */
    private String getLargestUrlAvailable(Photo photo) {
        Size size = photo.getLargeSize();
        if (size != null) {
            return photo.getLargeUrl();
        } else {
            /* No large size available, fall back to medium */
            return photo.getMediumUrl();
        }
    }

    private void displayImage() {
        if (BuildConfig.DEBUG) Log.d(TAG, "displayImage()");
        /* Fetch the main image */
        if (mBasePhoto != null) {
            String urlToFetch = getLargestUrlAvailable(mBasePhoto);
            Picasso.with(mActivity).load(urlToFetch).into(mImageView, new Callback() {
                @Override
                public void onSuccess() {
                    mAttacher.update();
                    mProgress.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError() {
                    Log.e(TAG, "displayImage -> Picasso -> onError");
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

    /**
     * Save an image to external storage.
     * Returns full path on success or null on failure.
     * <p/>
     * http://developer.android.com/reference/android/os/Environment.html
     */
    private File createExternalStoragePublicPicture(Bitmap bitmap, String filename) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, filename);
        try {
            path.mkdirs();

            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();

            /* Tell the media scanner about the new file so that it is
             * immediately available to the user. */
            MediaScannerConnection.scanFile(mActivity,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    }
            );

            return file;
        } catch (IOException e) {
            /* Unable to create file, likely because external storage is not
             * currently mounted. */
            Log.e("ExternalStorage", "Error writing " + file, e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Event published when the main photo is clicked.
     */
    public static class PhotoViewerVisibilityChangeEvent<T> {
        public final boolean visible;
        public final T sender;
        public PhotoViewerVisibilityChangeEvent(final boolean visible,
                                                final T sender) {
            this.visible = visible;
            this.sender = sender;
        }
    }
}

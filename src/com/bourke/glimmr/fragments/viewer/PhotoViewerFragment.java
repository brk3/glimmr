package com.bourke.glimmr.fragments.viewer;

import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.bourke.glimmr.activities.PhotoViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IPhotoInfoReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadPhotoInfoTask;

import com.googlecode.flickrjandroid.photos.Photo;

public final class PhotoViewerFragment extends BaseFragment
        implements IPhotoInfoReadyListener {

    protected String TAG = "Glimmr/PhotoViewerFragment";

    private Photo mBasePhoto;
    private Photo mPhoto;
    private AQuery mAq;
    private int mId;

    private LoadPhotoInfoTask mTask;

    public static PhotoViewerFragment newInstance(Photo photo, int id) {
        if (Constants.DEBUG)
            Log.d("Glimmr/PhotoViewerFragment", "newInstance");
        PhotoViewerFragment photoFragment = new PhotoViewerFragment();
        photoFragment.mBasePhoto = photo;
        photoFragment.mId = id;
        return photoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(TAG, "onCreate");
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        mTask = new LoadPhotoInfoTask(this, this, mBasePhoto);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Constants.DEBUG)
            Log.d(TAG, "onPause");
        if (mTask != null) {
            mTask.cancel(true);
            if (Constants.DEBUG)
                Log.d(TAG, "onPause: cancelling task");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (Constants.DEBUG)
            Log.d(getLogTag(), "onCreateView");
        mLayout = (RelativeLayout) inflater.inflate(R.layout
                .photoviewer_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        return mLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (Constants.DEBUG)
            Log.d(getLogTag(), "onSaveInstanceState");
        outState.putSerializable(Constants.KEY_PHOTOVIEWER_URL, mBasePhoto);
        outState.putInt("glimmr_photoviewer_id", mId);
     }

    /**
     * Fragments don't seem to have a onRestoreInstanceState so we use this
     * to restore the photo been viewed in the case of rotate instead.
     */
    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        if (Constants.DEBUG)
            Log.d(getLogTag(), "onActivityCreated");
        mPhoto = null;
    }

    @Override
    protected void startTask() {
        super.startTask();
        /* Start a task to fetch more detailed info about the photo if we don't
         * already have it (required for favorite status) */
        if (mPhoto == null) {
            mTask.execute(mOAuth);
        }
    }

    @Override
    public void onPhotoInfoReady(Photo photo) {
        if (Constants.DEBUG)
            Log.d(getLogTag(), "onPhotoInfoReady");
        mPhoto = photo;
        /* If we're currently showing, update the favorite button icon */
        if (mId == ((PhotoViewerActivity) mActivity).getSelectedFragmentId()) {
            ((PhotoViewerActivity) mActivity).updateFavoriteButtonIcon(
                mPhoto.isFavorite());
        }
        displayImage();
    }

    private void displayImage() {
        if (Constants.DEBUG)
            Log.d(TAG, "displayImage()");
        if (mPhoto != null) {
            mAq.id(R.id.image).progress(R.id.progress).image(
                    mPhoto.getLargeUrl(), Constants.USE_MEMORY_CACHE,
                    Constants.USE_FILE_CACHE, 0, 0, null,
                    AQuery.FADE_IN_NETWORK);
            String photoTitle = mPhoto.getTitle();
            if (photoTitle == null || photoTitle.isEmpty()) {
                photoTitle = mActivity.getString(R.string.untitled);
            }
            mAq.id(R.id.textViewTitle).text(photoTitle);
            mAq.id(R.id.textViewAuthor).text(mActivity.getString(R.string.by) +
                    " " + mPhoto.getOwner().getUsername());
        } else {
            if (Constants.DEBUG)
                Log.e(getLogTag(), "displayImage: mPhoto is null");
        }
    }

    public void toggleOverlayVisibility(boolean on) {
        boolean honeycombOrGreater =
            (android.os.Build.VERSION.SDK_INT >=
             android.os.Build.VERSION_CODES.HONEYCOMB);
        if (on) {
            mAq.id(R.id.textViewTitle).visible();
            mAq.id(R.id.textViewAuthor).visible();
            if (honeycombOrGreater) {
                mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        } else {
            mAq.id(R.id.textViewTitle).invisible();
            mAq.id(R.id.textViewAuthor).invisible();
            if (honeycombOrGreater) {
                mLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        }
    }

    public void refreshFavoriteIcon() {
        if (Constants.DEBUG)
            Log.d(getLogTag(), "refreshFavoriteIcon");
        if (mTask != null) {
            if (Constants.DEBUG) Log.d(getLogTag(), "mTask not null");
            if (mTask.getStatus() == AsyncTask.Status.FINISHED) {
                if (Constants.DEBUG) Log.d(getLogTag(), "mTask finished");
                ((PhotoViewerActivity) mActivity).updateFavoriteButtonIcon(
                    mPhoto.isFavorite());
            }
        } else {
            /* Do nothing, it will update when it's done */
            if (Constants.DEBUG)
                Log.d(getLogTag(), "mTask null or not finished");
        }
    }

    public Photo getPhoto() {
        return mBasePhoto;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

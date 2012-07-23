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

import com.gmail.yuyang226.flickr.photos.Photo;

public final class PhotoViewerFragment extends BaseFragment
        implements IPhotoInfoReadyListener {

    protected String TAG = "Glimmr/PhotoViewerFragment";

    private Photo mPhoto = new Photo();
    private AQuery mAq;
    private int mId;

    private LoadPhotoInfoTask mTask;

    public static PhotoViewerFragment newInstance(Photo photo, int id) {
        PhotoViewerFragment photoFragment = new PhotoViewerFragment();
        photoFragment.mPhoto = photo;
        photoFragment.mId = id;
        return photoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTask = new LoadPhotoInfoTask(mActivity, this, mPhoto);
    }

    @Override
    public void onResume() {
        mTask = new LoadPhotoInfoTask(mActivity, this, mPhoto);
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(getLogTag(), "onCreateView");
        mLayout = (RelativeLayout) inflater.inflate(R.layout
                .photoviewer_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        return mLayout;
    }

    @Override
    protected void startTask() {
        super.startTask();
        mTask.execute(mOAuth);
    }

    @Override
    public void onPhotoInfoReady(Photo photo) {
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
        if (mPhoto != null) {
            mAq.id(R.id.image).progress(R.id.progress).image(
                    mPhoto.getLargeUrl(), Constants.USE_MEMORY_CACHE,
                    Constants.USE_FILE_CACHE, 0, 0, null,
                    AQuery.FADE_IN_NETWORK);
        } else {
            Log.e(getLogTag(), "displayImage: mPhoto is null");
        }
    }

    public void refreshFavoriteIcon() {
        Log.d(getLogTag(), "refreshFavoriteIcon");
        if (mTask != null) {
            Log.d(getLogTag(), "mTask not null");
            if (mTask.getStatus() == AsyncTask.Status.FINISHED) {
                Log.d(getLogTag(), "mTask finished");
                ((PhotoViewerActivity) mActivity).updateFavoriteButtonIcon(
                    mPhoto.isFavorite());
            }
        } else {
            /* Do nothing, it will update when it's done */
            Log.d(getLogTag(), "mTask null or not finished");
        }
    }

    public Photo getPhoto() {
        return mPhoto;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

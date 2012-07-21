package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.bourke.glimmr.activities.PhotoViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;

import com.gmail.yuyang226.flickr.photos.Photo;

public final class PhotoViewerFragment extends BaseFragment {

    protected String TAG = "Glimmr/PhotoViewerFragment";

    private Photo mPhoto = new Photo();
    private AQuery mAq;

    public static PhotoViewerFragment newInstance(Photo photo) {
        PhotoViewerFragment photoFragment = new PhotoViewerFragment();
        photoFragment.mPhoto = photo;
        return photoFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(getLogTag(), "onSaveInstanceState");
        outState.putSerializable(Constants.KEY_PHOTOVIEWER_URL, mPhoto);
    }

    /**
     * Fragments don't seem to have a onRestoreInstanceState so we use this
     * to restore the photo been viewed in the case of rotate instead.
     */
    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        Log.d(getLogTag(), "onActivityCreated");
        if (state != null) {
            Photo p = (Photo) state.getSerializable(
                    Constants.KEY_PHOTOVIEWER_URL);
            if (p != null) {
                Log.d(getLogTag(), "mPhoto restored");
                mPhoto = p;
            }
        }
        displayImage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout
                .photoviewer_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        return mLayout;
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

    @Override
    protected String getLogTag() {
        return TAG;
    }
}

package com.bourke.glimmr.fragments.viewer;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.androidquery.AQuery;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(
                    Constants.KEY_PHOTOVIEWER_URL)) {
            mPhoto.setUrl(savedInstanceState.getString(
                        Constants.KEY_PHOTOVIEWER_URL));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout
                .photoviewer_fragment, container, false);
        mAq = new AQuery(mActivity, mLayout);
        if (mPhoto != null) {
            String url = mPhoto.getLargeUrl();
            mAq.id(R.id.web).progress(R.id.progress).webImage(url);
        } else {
            Log.e(TAG, "onStart, mPhoto is null");
        }
        return mLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.KEY_PHOTOVIEWER_URL, mPhoto.getUrl());
    }
}

package com.bourke.glimmr;

import android.app.Activity;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;

import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.gmail.yuyang226.flickr.photos.Photo;

public final class PhotoViewerFragment extends SherlockFragment {

    private static final String TAG = "Glimmr/PhotoViewerFragment";
    private static final String KEY_CONTENT = "PhotoViewerFragment:Content";

	private AQuery aq;
    private Photo mPhoto = new Photo();
    private Activity mActivity;
    private ViewGroup mLayout;

    public static PhotoViewerFragment newInstance(Photo photo) {
        PhotoViewerFragment photoFragment = new PhotoViewerFragment();
        photoFragment.mPhoto = photo;
        return photoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(
                    KEY_CONTENT)) {
            mPhoto.setUrl(savedInstanceState.getString(KEY_CONTENT));
        }

        mActivity = getSherlockActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (FrameLayout) inflater.inflate(R.layout.photoviewer_fragment,
                container, false);
		aq = new AQuery(mActivity, mLayout);
        if (mPhoto != null) {
            String url = mPhoto.getUrl();
            aq.id(R.id.web).progress(R.id.progress).webImage(url);
        } else {
            Log.e(TAG, "onStart, mPhoto is null");
        }
        return mLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mPhoto.getUrl());
    }
}

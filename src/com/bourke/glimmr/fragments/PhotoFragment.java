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

public final class PhotoFragment extends SherlockFragment {

    private static final String TAG = "Glimmr/PhotoFragment";

	protected AQuery aq;

    private Activity mActivity;
    private Photo mPhoto;

    public PhotoFragment(Photo photo) {
        mPhoto = photo;
    }

    public static PhotoFragment newInstance(Photo photo) {
        return new PhotoFragment(photo);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getSherlockActivity();
		aq = new AQuery(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        FrameLayout layout = (FrameLayout) inflater.inflate(R.layout
                .photoviewer_fragment, container, false);
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        if (mPhoto != null) {
            String url = mPhoto.getUrl();
            aq.id(R.id.web).progress(R.id.progress).webImage(url);
        } else {
            Log.e(TAG, "onStart, mPhoto is null");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
